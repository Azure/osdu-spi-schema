import requests
import google
from google.auth import jwt
from google.auth import crypt
import json
import base64
import os
import time

class GoogleToken(object):

    JSON_KEY = os.environ.get('JSON_KEY')

    def get_google_id_token(self):

        if self.JSON_KEY is None:
            print('Please pass JSON key to generate token')
            exit(1)

        svc_info = json.loads(base64.b64decode(self.JSON_KEY))
        signer = google.auth.crypt.RSASigner.from_service_account_info(svc_info)

        now = int(time.time())
        expires = now + 3600

        payload = {
            'iat': now,
            'exp': expires,
            'aud': 'https://www.googleapis.com/oauth2/v4/token',
            'target_audience': 'osdu',
            'iss': svc_info.get('client_email', '')
        }

        jwt = google.auth.jwt.encode(signer, payload)

        payload = {'grant_type': 'urn:ietf:params:oauth:grant-type:jwt-bearer',
                   'assertion': jwt.decode('utf-8')
                   }

        response = requests.post('https://www.googleapis.com/oauth2/v4/token', json=payload)
        token = 'Bearer ' +  response.json().get('id_token', '')
        print(token)
        return token

if __name__ == '__main__':
    GoogleToken().get_google_id_token()