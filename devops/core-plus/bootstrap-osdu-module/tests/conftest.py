import json
import sys
import threading
from pathlib import Path

import pytest

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))


class FakeResponse:
    def __init__(self, status_code=200, body="", url="http://test/"):
        self.status_code = status_code
        self.text = body if isinstance(body, str) else json.dumps(body)
        self.content = self.text.encode()
        self.url = url

    def json(self):
        return json.loads(self.text)

    def raise_for_status(self):
        if self.status_code >= 400:
            import requests

            raise requests.HTTPError("HTTP {}".format(self.status_code))

    def iter_content(self, chunk_size=1):
        yield self.content

    def __enter__(self):
        return self

    def __exit__(self, *exc):
        return False


class FakeSession:
    """Returns scripted responses per (method, url-substring), recording every call."""

    def __init__(self, routes=None):
        self.routes = list(routes or [])
        self.calls = []
        self._lock = threading.Lock()

    def add(self, method, fragment, *responses):
        self.routes.append([method, fragment, list(responses)])

    def _respond(self, method, url, **kwargs):
        with self._lock:
            self.calls.append((method, url, kwargs))
            for route in self.routes:
                if route[0] == method and route[1] in url:
                    responses = route[2]
                    response = responses.pop(0) if len(responses) > 1 else responses[0]
                    break
            else:
                raise AssertionError("Unexpected request {} {}".format(method, url))
        if isinstance(response, Exception):
            raise response
        return response

    def request(self, method, url, **kwargs):
        return self._respond(method, url, **kwargs)

    def get(self, url, **kwargs):
        return self._respond("GET", url, **kwargs)

    def post(self, url, **kwargs):
        return self._respond("POST", url, **kwargs)

    def put(self, url, **kwargs):
        if hasattr(kwargs.get("data"), "read"):
            kwargs["data"] = kwargs["data"].read()
        return self._respond("PUT", url, **kwargs)


@pytest.fixture
def fake_session():
    return FakeSession()


def make_schema(entity, version="1.0.0"):
    major, minor, patch = (int(part) for part in version.split("."))
    return {
        "schemaInfo": {
            "schemaIdentity": {
                "authority": "{{schema-authority}}",
                "source": "wks",
                "entityType": entity,
                "schemaVersionMajor": major,
                "schemaVersionMinor": minor,
                "schemaVersionPatch": patch,
            }
        },
        "schema": {},
    }


@pytest.fixture
def schemas_tree(tmp_path):
    """A tiny ``deployments``-like tree: <root>/shared-schemas/osdu/... plus a load sequence."""
    root = tmp_path / "deployments"
    osdu = root / "shared-schemas" / "osdu"
    (osdu / "abstract").mkdir(parents=True)
    sequence = []
    for entity in ("AbstractA", "AbstractB"):
        path = osdu / "abstract" / "{}.1.0.0.json".format(entity)
        path.write_text(json.dumps(make_schema(entity)), encoding="utf-8")
        sequence.append(
            {
                "kind": "{{schema-authority}}:wks:%s:1.0.0" % entity,
                "relativePath": "shared-schemas/osdu/abstract/{}.1.0.0.json".format(entity),
            }
        )
    (osdu / "load_sequence.1.0.0.json").write_text(json.dumps(sequence), encoding="utf-8")
    (root / "shared-schemas" / "README.md").write_text("not packaged", encoding="utf-8")
    return root
