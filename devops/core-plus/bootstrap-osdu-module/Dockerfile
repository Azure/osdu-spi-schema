# Where the bootstrap bundle (bootstrap code + libraries + schemas) comes from:
#   local     (default) built here from deployments/shared-schemas; needs access to PyPI for the pinned libraries
#   prebuilt  the file the schema-package CI job put in dist/schema-package, so the image holds exactly
#             what is published to the GitLab package registry (CI passes --build-arg BUNDLE=prebuilt)
ARG BUNDLE=local

# ---------------------------------------------------------------------------
# Bundle built from the sources: the same steps as the schema-package job in
# devops/core-plus/pipeline/override-stages.yml (version "local", no CI information).
# ---------------------------------------------------------------------------
FROM python:3.10-alpine3.24 AS bundle-local
ENV TOP=schema-bootstrap-local
COPY ./devops/core-plus/bootstrap-osdu-module/*.py /w/$TOP/
COPY ./devops/core-plus/bootstrap-osdu-module/requirements.txt /src/
COPY ./deployments/shared-schemas /src/shared-schemas
RUN pip install --quiet --no-compile --only-binary=:all: --platform any --target /w/$TOP/libs -r /src/requirements.txt
RUN mkdir -p /w/$TOP/shared-schemas && cd /src/shared-schemas && { find . -name '*.json'; echo ./VERSION; } | tar -cf - -T - | tar -xf - -C /w/$TOP/shared-schemas
RUN printf '{"version": "local", "commit": "", "registryUrl": ""}\n' > /w/$TOP/shared-schemas/manifest.json
RUN mkdir /out && tar -czf /out/schema-bootstrap-bundle.tar.gz -C /w $TOP

# ---------------------------------------------------------------------------
# Bundle built by CI
# ---------------------------------------------------------------------------
FROM scratch AS bundle-prebuilt
COPY ./dist/schema-package/schema-bootstrap-bundle.tar.gz /out/

FROM bundle-${BUNDLE} AS bundle

# ---------------------------------------------------------------------------
# Bootstrap image. The libraries come from the bundle, so nothing is installed with
# pip here and pip itself is removed.
# ---------------------------------------------------------------------------
FROM python:3.10-alpine3.24

WORKDIR /opt
ENV PIP_BREAK_SYSTEM_PACKAGES=1

# Only /opt itself is chowned (not recursively) so that the bundle is written once,
# by the non-root user, instead of being copied into a second layer.
RUN apk upgrade --no-cache && \
  addgroup -g 10001 -S nonroot && \
  adduser -h /opt -G nonroot -S -u 10001 nonroot && \
  chown 10001:10001 /opt
RUN pip3 install --no-cache-dir --upgrade pip setuptools wheel && \
  pip3 uninstall -y pip wheel && \
  rm -rf /usr/local/lib/python3*/ensurepip

USER 10001:10001

# The bundle is mounted from the stage above, so it does not stay in an image layer.
# It extracts into /opt: bootstrap_schema.py, libs/ (found automatically by bootstrap_schema.py) and
# shared-schemas/.
RUN --mount=type=bind,from=bundle,source=/out,target=/tmp/bundle \
  tar -xzf /tmp/bundle/schema-bootstrap-bundle.tar.gz --strip-components=1 -C /opt

CMD ["python3", "/opt/bootstrap_schema.py"]
