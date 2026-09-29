import json
import requests
import http.client
import os;


class IBMToken(object):
    def get_ibm_id_token(self):
        url = 'https://' + os.getenv('IBM_KEYCLOAK_URL') + '/auth/realms/OSDU/protocol/openid-connect/token'
        clientId = os.getenv('IBM_KEYCLOAK_CLIENT_ID')
        clientSecret = os.getenv('IBM_KEYCLOAK_CLIENT_SECRET')
        grantType = 'password'
        userName = os.getenv('IBM_AUTH_USER_ACCESS')
        password = os.getenv('IBM_AUTH_USER_ACCESS_PASSWORD')
        verifyVal = os.getenv('IBM_KEYCLOAK_VERIFY_VALUE')
        if verifyVal=="False":
           verify_Val=False 
        else:
           verify_Val=True
        scope = 'openid'
        
        
        if url is None:
            print('Please pass url to generate token')
            exit(1)
        if clientId is None:
            print('Please pass client id to generate token')
            exit(1)
        if clientSecret is None:
            print('Please pass client secret to generate token')
            exit(1)
        if grantType is None:
            print('Please pass client secret to generate token')
            exit(1)
        if userName is None:
            print('Please pass client secret to generate token')
            exit(1)
        if password is None:
            print('Please pass client secret to generate token')
            exit(1)
        if scope is None:
            print('Please pass client secret to generate token')
            exit(1)
        
        try:
        
            response = requests.post(url, verify=verify_Val, data={"grant_type": grantType,        
                 "client_id": clientId,
                 "client_secret" : clientSecret,
                 "username": userName,
                 "password": password, "scope": scope})
        
            result = response.json()
            token = 'Bearer ' + result['access_token']
            print(token)
            
        
        except Exception as e:
            print(e)
            
            
if __name__ == '__main__':
    IBMToken().get_ibm_id_token()
