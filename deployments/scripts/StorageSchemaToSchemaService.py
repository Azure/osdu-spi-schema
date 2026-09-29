import json
from Utility import Utility, RunEnv, Constants, StorageService, SchemaService
import requests
import argparse
import urllib.parse
import copy
import re
import os


class StorageSchemaToSchemaService(object):

    def __init__(self):
        parser = argparse.ArgumentParser(
            description="Convert a Storage Schema and convert it into JSON schema and post it to the Schema Service.")
        parser.add_argument('-f', help='The complete URL to the Storage Service.',
                            default=None)
        parser.add_argument('-s', help='The complete URL to the Schema Service.',
                            default=None)
        parser.add_argument('-e', help='The quoted, comma separated list of kinds to convert (optional); supersedes -m.',
                            default=None)
        parser.add_argument('-m', help='The quoted wildcard string to match kinds.',
                            default='*')
        arguments = parser.parse_args()
        if arguments.f is not None:
            RunEnv.STORAGE_SERVICE_URL = arguments.f
        if arguments.s is not None:
            RunEnv.SCHEMA_SERVICE_URL = arguments.s

        ok, error_mess = RunEnv().is_ok(schema_service=False, storage_service=True)
        if not ok:
            exit('Error: environment setting incomplete: {}'.format(error_mess))

        self.storage_url = RunEnv.STORAGE_SERVICE_URL
        self.schema_url = RunEnv.SCHEMA_SERVICE_URL
        self.schema_registered = None
        self.property_dictionary = None
        self.schema_payload = None
        self.storage_service = StorageService(self.storage_url)
        self.schema_service = SchemaService(self.schema_url)

        self.resource_template = Utility.load_json(os.path.join(*Constants.RESOURCE_TEMPLATE))
        if arguments.e is not None:
            kinds = arguments.e.split(',')
        else:
            print('Finding matching kinds for "{}"'.format(arguments.m))
            kinds = self.storage_service.get_kinds(arguments.m)
            print('Found {} matching kinds.'.format(len(kinds)))
        messages = list()
        for kind in kinds:
            self.property_dictionary = dict()
            self.add_structures = dict()
            ok, self.schema_registered = self.storage_service.get_schema(kind, messages)
            if ok:
                self.schema_payload = self.__sort_storage_schema(messages)
        if len(messages) > 0:
            print('Process finished with errors:\n' + '\n'.join(messages))
        else:
            print('Process finished successfully.')

    def __sort_storage_schema(self, messages: list):
        kind = self.schema_registered['kind']
        entity = kind.split(':')[-2]
        nvp = dict()
        for item in self.schema_registered['schema']:
            nvp[item['path']] = item['kind']
        self.property_dictionary = nvp
        self.sorted_keys = sorted(nvp.keys())
        proto_schema = dict()
        for key in self.sorted_keys:
            self.set_property(key, proto_schema)

        schema = {'title': self.camel_case_split(entity).title(), 'description': 'No description available.'}
        self.make_schema(proto_schema, schema)
        schema = self.merge_schema(schema)
        schema['properties']['kind']['default'] = kind  # by setting to enum [kind]
        ok, schema_info = self.schema_service.get_schema_info(kind)
        if not ok or not schema_info:
            schema_info = self.get_schema_info(kind)
        payload = {'schemaInfo': schema_info, 'schema': schema}
        self.schema_service.post_or_put_schema(kind, payload, schema_info['status'], messages=messages)
        return payload

    @staticmethod
    def get_schema_info(kind: str):
        parts = kind.split(':')
        versions = parts[-1].split('.')
        schema_info = {
            "schemaIdentity": {
                "authority": parts[0],
                "source": parts[1],
                "entityType": parts[2],
                "schemaVersionMajor": int(versions[0]),
                "schemaVersionMinor": int(versions[1]),
                "schemaVersionPatch": int(versions[2]),
                "id": kind
            },
            "createdBy": "SLB-Storage Schema Upgrade",
            "status": "DEVELOPMENT",
            "scope": "SHARED"
        }
        return schema_info

    def merge_schema(self, schema):
        final_schema = copy.deepcopy(self.resource_template)
        final_schema['title'] = schema['title']
        final_schema['description'] = 'Automatically generated JSON schema for {}.'.format(schema['title'])
        schema['title'] = 'Data block for entity {}'.format(schema['title'])
        final_schema['properties']['data'] = schema
        for definition in self.add_structures.keys():
            if definition == 'core:dl:geopoint:1.0.0':
                final_schema['definitions']['core_dl_geopoint'] = self.__geo_point()
        return final_schema

    def make_schema(self, proto_schema, schema, outer_key=None):
        if self.__is_sub_structure(proto_schema):
            if outer_key:
                schema[outer_key] = dict()
                schema = schema[outer_key]
            schema['type'] = 'object'
            schema['properties'] = dict()
            for key, value in proto_schema.items():
                self.make_schema(value, schema['properties'], key)
        elif outer_key:
            if isinstance(proto_schema, dict) and 'array' in proto_schema:
                self.__create_array_property(outer_key, proto_schema, schema)
            else:
                schema[outer_key] = proto_schema

    @staticmethod
    def __is_sub_structure(proto_schema):
        if isinstance(proto_schema, dict):
            if 'type' not in proto_schema:
                return True
            if isinstance(proto_schema['type'], dict):
                return True
        return False

    @staticmethod
    def __create_array_property(outer_key, proto_schema, schema):
        schema[outer_key] = dict()
        schema[outer_key]['type'] = 'array'
        for item in ['title', 'description', 'format']:
            if item in proto_schema:
                schema[outer_key][item] = proto_schema[item]
        schema[outer_key]['items'] = {'type': proto_schema['type']}

    def set_property(self, key, schema):
        keys = key.split('.')
        s = schema
        for k in keys:
            if k not in s:
                s[k] = dict()
            s = s[k]
        last_key = keys[-1]
        f = None
        p = None
        t = self.property_dictionary[key]
        is_array = t.startswith('[]')
        t = t.replace('[]', '')
        if t == 'int':
            t = 'integer'
        elif t == 'long':
            t = 'integer'
            f = 'int64'
        elif t == 'double' or t == 'float':  # float is not a valid JSON type
            t = 'number'
        elif t == 'datetime':
            t = 'string'
            f = 'date-time'
        elif t == 'core:dl:geopoint:1.0.0':
            self.add_structures[t] = True
            t = 'object'
            p = "#/definitions/core_dl_geopoint"
        elif t == 'link':
            f = t
            t = 'string'
        elif t == 'core:dl:geoshape:1.0.0':
            t = 'object'
            p = "https://geojson.org/schema/FeatureCollection.json"
        s['description'] = 'No description available.'
        s['title'] = self.camel_case_split(last_key).title()
        if t:
            s['type'] = t
        if p:
            s['$ref'] = p
        if f:
            s['format'] = f
        if is_array:
            s['array'] = is_array

    @staticmethod
    def camel_case_split(text):
        words = re.sub(r'((?<=[a-z])[A-Z]|(?<!\A)[A-Z](?=[a-z]))', r' \1', text)
        result = words.title()
        return result

    @staticmethod
    def __geo_point():
        return {
            "description": "A 2D point location in latitude and longitude referenced to WGS 84 if not specified otherwise.",
            "properties": {
                "latitude": {
                    "description": "The latitude value in degrees of arc (dega). Value range [-90, 90].",
                    "title": "Latitude",
                    "type": "number",
                    "minimum": -90,
                    "maximum": 90
                },
                "longitude": {
                    "description": "The longitude value in degrees of arc (dega). Value range [-180, 180]",
                    "title": "Longitude",
                    "type": "number",
                    "minimum": -180,
                    "maximum": 180
                }
            },
            "required": [
                "latitude",
                "longitude"
            ],
            "title": "2D Map Location",
            "type": "object"
        }


if __name__ == '__main__':
    c = StorageSchemaToSchemaService()
