import os
import json
import fnmatch
import requests
import pathlib
import urllib.parse


class RunEnv(object):

    BEARER_TOKEN = os.environ.get('BEARER_TOKEN')
    APP_KEY = os.environ.get('APP_KEY')
    SCHEMA_SERVICE_URL = None
    STORAGE_SERVICE_URL = None
    SCHEMA_AUTHORITY = os.environ.get('SCHEMA_AUTHORITY')
    SCHEMAS_FOLDER = 'shared-schemas'
    DEFAULT_BOOTSTRAP_OPTIONS = '[{"authority": "osdu", "folder": "osdu", "load-sequence": "load_sequence.1.0.0.json"}]'
    BOOTSTRAP_OPTIONS = os.environ.get('BOOTSTRAP_OPTIONS', DEFAULT_BOOTSTRAP_OPTIONS)

    def __init__(self):
        """Empty constructor"""
        pass

    def is_ok(self, schema_service=True, storage_service=False):
        message = ''
        ok = True
        if storage_service: ok = ok and self.STORAGE_SERVICE_URL is not None
        if schema_service: ok = ok and self.SCHEMA_SERVICE_URL is not None
        ok = ok and self.BEARER_TOKEN is not None
        message = self.__create_message(message, ok)
        return ok, message

    def __create_message(self, message, ok):
        if not ok:
            parts = list()
            if self.BEARER_TOKEN is None:
                parts.append('BEARER TOKEN')
            if self.SCHEMA_SERVICE_URL is None:
                parts.append('Schema service URL')
            message = ', '.join(parts) + ' missing.'
        return message

    @staticmethod
    def get_headers():
        return {
            'Content-Type': 'application/json',
            'AppKey': RunEnv.APP_KEY,
            'Authorization': RunEnv.BEARER_TOKEN
        }


class Utility(object):
    def __init__(self):
        """Empty constructor"""
        pass

    @staticmethod
    def find_file(file_name, directory_parts=None, root=os.path.abspath(__file__)):
        """Find a file with a given name in optional sub-path components and root"""
        if directory_parts is None:
            directory_parts = []
        path = Utility.__get_root_path(root)
        for part in directory_parts:
            path = os.path.join(path, part)
        for root, dirs, files in os.walk(path):
            for one_file in fnmatch.filter(files, file_name):
                return os.path.join(root, one_file)
        return None

    @staticmethod
    def find_files(directory_parts=None, root=os.path.abspath(__file__), search_expression='*.json'):
        """Find all JSON files in optional sub-path components and root"""
        found = list()
        if directory_parts is None:
            directory_parts = []
        path = Utility.__get_root_path(root)
        for part in directory_parts:
            path = os.path.join(path, part)
        for root, dirs, files in os.walk(path):
            for one_file in fnmatch.filter(files, search_expression):
                found.append(os.path.join(root, one_file))
        return found

    @staticmethod
    def get_entity_folder_from_file(file, folder_parts):
        version = None  # OSDU R2 has no version in the file name
        top_level = folder_parts[-1]
        parts = os.path.split(file)
        entity = parts[1].replace('.json', '')
        if '.' in entity:  # OSDU R3 contains version in file name
            vps = entity.split('.') # filename: <entityType>.major.minor.patch - 4 parts
            if len(vps) >= 4:
                version = '.'.join([vps[-3], vps[-2], vps[-1]])
                entity = entity.replace('.'+version, '')
            else:
                exit('Error in entity name/version: {} expected <entityType>.major.minor.patch.json'.format(entity))
        parts = parts[0].split(os.sep)
        group_type = parts[-1]
        folders = list()
        collect = False
        for part in parts:
            if part == top_level:
                collect = True
            elif collect:
                folders.append(part)
        return group_type, entity, version, folders

    @staticmethod
    def __get_root_path(root):
        if os.path.isfile(root):
            path = os.path.join(os.path.dirname(root))
        else:
            path = root
        return path

    @staticmethod
    def load_json(path):
        """Load a JSON file"""
        try:
            with open(path, "r", encoding='utf-8') as text_file:
                j_obj = json.load(text_file)
            return j_obj
        except FileNotFoundError as e:
            exit("Given File path not found::{}".format(str(e)))

    @staticmethod
    def save_json(schema, path, sort_keys=False):
        "Save a JSON schema to a file given as path"
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path, "w") as text_file:
            json.dump(schema, text_file, sort_keys=sort_keys, indent=2)

    @staticmethod
    def get_relative_path(base_path, path):
        return pathlib.Path(os.path.relpath(path, base_path)).as_posix()

    @staticmethod
    def path_to_deployments():
        return os.path.dirname(os.path.dirname(__file__))


class Constants(object):
    RESOURCE_TEMPLATE = ['templates', 'delfi__standard_resources_v1.json']


class StorageService(object):
    def __init__(self, url: str):
        self.headers = RunEnv.get_headers()
        self.url = url

    def get_schema(self, kind: str, messages: list):
        schema = None
        url = '{}/schemas/{}'.format(self.url, urllib.parse.quote_plus(kind))
        response = requests.request("GET", url, headers=self.headers)
        if response.status_code in range(200, 300):
            schema = json.loads(response.text)
        else:
            messages.append('Error: Storage Service GET schema {}, {}'.format(response.status_code, response.text))
        return response.status_code in range(200, 300), schema

    def get_kinds(self, match: str):
        result = list()
        carry_on = True
        cursor = None
        while carry_on:
            url = '{}/query/kinds?limit={}'.format(self.url, 1000)
            if cursor:
                url += '&cursor={}'.format(cursor)
            response = requests.request("GET", url, headers=self.headers)
            if response.status_code in range(200, 300):
                rs = json.loads(response.text)
                cursor = rs['cursor']
                carry_on = len(rs['results']) > 0
                for kind in rs['results']:
                    if fnmatch.fnmatch(kind, match):
                        result.append(kind)
            else:
                exit('Error: Storage Service GET kinds {}, {}'.format(response.status_code, response.text))
        return result


class SchemaService(object):
    def __init__(self, url: str):
        self.headers = RunEnv.get_headers()
        self.url = url

    def does_kind_exist(self, kind):
        schema = None
        url = '{}/{}'.format(self.url, kind)
        response = requests.request("GET", url, headers=self.headers)
        if response.status_code in range(200,300):
            schema = json.loads(response.text)
        return response.status_code in range(200,300), schema

    def post_or_put_schema(self, kind: str, schema: dict, schema_status: str, messages: list):
        # attempt to load this kind
        exists, dummy = self.does_kind_exist(kind)
        payload = json.dumps(schema)
        method = 'POST'
        if exists:
            if schema_status == 'DEVELOPMENT':
                method = 'PUT'
                response = requests.request(method, self.url, headers=self.headers, data=payload)
            else:
                message = 'Error: The published kind {} cannot be updated; it already exists.'.format(kind)
                print(message)
                messages.append(message)
                response = None
        else:
            response = requests.request(method, self.url, headers=self.headers, data=payload)

        if response is not None:
            code = response.status_code
            if code not in range(200, 300):
                messages.append(
                    'Error with kind {}: Message: {}'.format(kind, response.text))
            else:
                print('Success: kind {} submitted with method {} schema.'.format(kind, method))

    @staticmethod
    def split_kind(kind: str):
        parts = kind.split(':')
        versions = parts[-1].split('.')
        return {
            "authority": parts[0],
            "source": parts[1],
            "entityType": parts[2],
            "schemaVersionMajor": int(versions[0]),
            "schemaVersionMinor": int(versions[1]),
            "schemaVersionPatch": int(versions[2])}

    @staticmethod
    def __match(schema_info: dict, info: dict):
        same = True
        for key in ['authority', 'source', 'entity', 'schemaVersionMajor', 'schemaVersionMinor', 'schemaVersionPatch']:
            same = same and schema_info[key] == info[key]
        return same

    def get_schema_info(self, kind: str):
        si = self.split_kind(kind)
        url = '{}/schema?authority={}&source={}&entity={}&schemaVersionMajor={}&schemaVersionMinor={}&limit=100000'.format(
            self.url, si['authority'], si['source'], si['entity'], si['schemaVersionMajor'], si['schemaVersionMinor'])
        response = requests.request("GET", url, headers=self.headers)
        schema_infos = list()
        if response.status_code in range(200,300):
            r = json.loads(response.text)
            schema_infos = r['schemaInfos']
        for info in schema_infos:
            if self.__match(info['schemaIdentity'], si):
                return response.status_code in range(200,300), info
        return response.status_code in range(200,300), None