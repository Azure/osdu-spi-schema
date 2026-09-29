import http
import json
import os
import time
import requests
import argparse
from Utility import Utility, RunEnv
from typing import Tuple


class DeploySharedSchemas:
    SCHEMA_AUTHORITY_TO_REPLACE = '{{schema-authority}}'
    SCHEMA_EXISTS = 'Schema Id is already present'
    PUBLISHED_SCHEMA_ERROR = ['Only schema in development stage can be updated',
                              'Only schema in developement stage can be updated']
    ALREADY_PUBLISHED = 'AlreadyPublished'
    SUCCESS = 'Success'
    TRY_AGAIN='Internal server error'

    def __init__(self):
        parser = argparse.ArgumentParser(
            description="Given a path to an load sequence file, load/update the schemas "
                        "listed in the load sequence file.")
        parser.add_argument('-u', help='The complete URL to the Schema Service.',
                            default=None)
        parser.add_argument('-e', '--early-exit', action='store_true', 
                            help='Exit from the script as soon as an unexpected error occurs. E.g., 401 response status', default=False)
        arguments = parser.parse_args()
        if arguments.u is not None:
            RunEnv.SCHEMA_SERVICE_URL = arguments.u
        if RunEnv.SCHEMA_SERVICE_URL is None:
            exit('The schema service URL is not specified')
        self._early_exit = arguments.early_exit
        self.url = RunEnv.SCHEMA_SERVICE_URL
        self.schema_registered = None
        self.schema_info_registered = None
        self.headers = {
            'Content-Type': 'application/json',
            'AppKey': RunEnv.APP_KEY,
            'Authorization': RunEnv.BEARER_TOKEN
        }
        ok, error_mess = RunEnv().is_ok()
        if not ok:
            exit('Error: environment setting incomplete: {}'.format(error_mess))

    def create_schema(self):
        messages = list()
        deployments = Utility.path_to_deployments()
        start = time.time()

        bootstrap_options = json.loads(RunEnv.BOOTSTRAP_OPTIONS)
        for option in bootstrap_options:
            try:
                schema_path = option['folder']
                schema_authority = option['authority']
                load_sequence = option['load-sequence']
            except KeyError as e:
                exit('Key missing in bootstrap-options::{}'.format(str(e)))

            sequence = Utility.load_json(os.path.join(deployments, RunEnv.SCHEMAS_FOLDER, schema_path, load_sequence))
            for item in sequence:
                self.schema_registered = None
                schema_file = os.path.join(deployments, item['relativePath'])
                schema = open(schema_file, 'r').read()
                schema = schema.replace(self.SCHEMA_AUTHORITY_TO_REPLACE, schema_authority)
                kind = self.__kind_from_schema_info(schema)
                self.__register_one(kind, schema, messages)

            elapsed = time.time() - start
            print('This update took {:.2f} seconds.'.format(elapsed))
            if len(messages) != 0:
                print('Following schemas failed:')
                print('\n'.join(messages))
                exit(1)
            else:
                print('All {} schemas registered, updated or '
                      'left unchanged because of status PUBLISHED.'.format(str(len(sequence))))

    @staticmethod
    def __kind_from_schema_info(schema_as_str: str) -> str:
        kind = 'Error'
        try:
            schema = json.loads(schema_as_str)
            si = schema.get('schemaInfo', dict()).get('schemaIdentity', dict())
            authority = si.get('authority', '')
            source = si.get('source', '')
            entity = si.get('entityType', '')
            major = str(si.get('schemaVersionMajor', 0))
            minor = str(si.get('schemaVersionMinor', 0))
            patch = str(si.get('schemaVersionPatch', 0))
            kind = '{}:{}:{}:{}.{}.{}'.format(authority, source, entity, major, minor, patch)
        except Exception as e:
            exit('Invalid JSON in payload {}'.format(str(e)))
        return kind

    def __register_one(self, kind, schema, messages):
        method = 'PUT'
        try_it = 'Try {} for id: {}'
        print(try_it.format(method, kind))
        response = requests.request(method, self.url, headers=self.headers, data=schema)
        is_error, message, method = self.__evaluate_response(response)
        if method == 'PUT':  # try again
            print(try_it.format(method, kind))
            response = requests.request(method, self.url, headers=self.headers, data=schema)
            is_error, message, method = self.__evaluate_response(response)

        if is_error:
            message = 'Error with kind {}: Message: {}'.format(kind, message)
            print(message)
            messages.append(message)
        elif method == self.SUCCESS:
            print('The kind {} was registered successfully.'.format(kind))
        elif method == self.ALREADY_PUBLISHED:
            print('The kind {} was already registered with status PUBLISHED '
                  'and was not updated.'.format(kind))

    def __evaluate_response(self, response: requests.Response) -> Tuple[bool, str, str]:
        code = response.status_code
        message = ''
        method = 'Give up'
        error = code not in range(200, 300)
        if error:
            print(response.text)
            print(response.url)
            print(code)
            # further test:
            try:
                js_err = json.loads(response.text)
                message = js_err.get('error', dict()).get('message', '')
                if message == self.SCHEMA_EXISTS:
                    method = 'PUT'  # try PUT, it might have been DEVELOPMENT, than we can overwrite
                elif message in self.PUBLISHED_SCHEMA_ERROR:  # already PUBLISHED, no bootstrap required
                    method = self.ALREADY_PUBLISHED
                    error = False  # this is not considered an error
                elif message in self.TRY_AGAIN:  #try again
                    method = 'PUT'
                    error = False  # this is not considered an error
                    if self._early_exit:
                        response.raise_for_status()
                # everything else is an error
            except Exception as e:
                message = str(e)
                if self._early_exit:
                    raise e
        else:
            method = self.SUCCESS
        return error, message, method


if __name__ == '__main__':
    DeploySharedSchemas().create_schema()
