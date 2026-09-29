import os
import msal

class AzureToken(object):

    def get_azure_id_token(self):
        tenant_id = os.getenv('AZURE_TENANT_ID')
        resource_id = os.getenv('AZURE_AD_APP_RESOURCE_ID')
        client_id = os.getenv('AZURE_CLIENT_ID')
        #[SuppressMessage("Microsoft.Security", "CS002:SecretInNextLine", Justification="the variable name is getting flagged")]
        client_secret = os.getenv('AZURE_CLIENT_SECRET')
        
        if tenant_id is None:
            print('Please pass tenant Id to generate token')
            exit(1)
        if resource_id is None:
            print('Please pass resource Id to generate token')
            exit(1)
        if client_id is None:
            print('Please pass client Id to generate token')
            exit(1)
        if client_secret is None:
            print('Please pass client secret to generate token')
            exit(1)

        try:
            authority_host_uri = 'https://login.microsoftonline.com'
            authority_uri = authority_host_uri + '/' + tenant_id
            scopes = [resource_id + '/.default']
            app = msal.ConfidentialClientApplication(client_id=client_id, authority=authority_uri, client_credential=client_secret)
            result = app.acquire_token_for_client(scopes=scopes)
            token = 'Bearer ' +  result.get('access_token')
            print(token)
            return token
        except Exception as e:
            print(e)

if __name__ == '__main__':
    AzureToken().get_azure_id_token()