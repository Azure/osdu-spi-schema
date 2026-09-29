package org.opengroup.osdu.schema.util;

import com.google.common.base.Strings;
import org.opengroup.osdu.azure.util.AzureServicePrincipal;
import org.opengroup.osdu.core.ibm.util.IdentityClient;
import org.opengroup.osdu.schema.constants.TestConstants;
import org.opengroup.osdu.schema.util.gcp.GoogleServiceAccount;
import org.opengroup.osdu.schema.util.gcp.OpenIDTokenProvider;
import org.springframework.retry.support.RetryTemplate;


public class AuthUtil {

    private final RetryTemplate retrytemp = RetryTemplate.builder()
        .maxAttempts(TestConstants.HTTP_RETRY_COUNT)
        .exponentialBackoff(TestConstants.EXPONENTIALBACKOFF_INITIALINTERVAL,TestConstants.EXPONENTIALBACKOFF_MULTIPLIER,TestConstants.EXPONENTIALBACKOFF_MAXINTERVAL)
        .retryOn(Exception.class)
        .build();

    public synchronized String getToken() throws Exception {
        String token = null;
        String vendor = System.getProperty("VENDOR", System.getenv("VENDOR"));
        if (Strings.isNullOrEmpty(token) && "gcp".equals(vendor)) {
            String serviceAccountFile = System.getProperty("INTEGRATION_TESTER", System.getenv("INTEGRATION_TESTER"));
            token = new GoogleServiceAccount(serviceAccountFile).getAuthToken();
        }else if (Strings.isNullOrEmpty(token) && "aws".equals(vendor)) {
            token = AwsServicePrincipalUtil.getAccessToken();
        } else if (Strings.isNullOrEmpty(token) && "azure".equals(vendor)) {
            String bearerToken = System.getProperty("INTEGRATION_TESTER_ACCESS_TOKEN", System.getenv("INTEGRATION_TESTER_ACCESS_TOKEN"));
            if(!Strings.isNullOrEmpty(bearerToken) && Strings.isNullOrEmpty(token)) {
                System.out.println("Using INTEGRATION_TESTER_ACCESS_TOKEN bearer token from environment variable");
                token = bearerToken;
            }
            else if (Strings.isNullOrEmpty(token)) {
                String sp_id = System.getProperty("INTEGRATION_TESTER", System.getenv("INTEGRATION_TESTER"));
                String sp_secret = System.getProperty("TESTER_SERVICEPRINCIPAL_SECRET", System.getenv("TESTER_SERVICEPRINCIPAL_SECRET"));
                String tenant_id = System.getProperty("AZURE_AD_TENANT_ID", System.getenv("AZURE_AD_TENANT_ID"));
                String app_resource_id = System.getProperty("AZURE_AD_APP_RESOURCE_ID", System.getenv("AZURE_AD_APP_RESOURCE_ID"));
                try{
                    //Retries are invoked in case of an Exception
                    return retrytemp.execute( context -> {
                        String retrieveToken = new AzureServicePrincipal().getIdToken(sp_id, sp_secret, tenant_id, app_resource_id);
                        return "Bearer " + retrieveToken;
                    });
                }
                catch (Exception e){
                    //This block throws an error when all the retries have been used
                    e.printStackTrace();
                    throw new Exception("Error generating the user token",e);
                }
            }
        } else if (Strings.isNullOrEmpty(token) && "ibm".equals(vendor)) {
            token = IdentityClient.getTokenForUserWithAccess();
        } else if (Strings.isNullOrEmpty(token) && "anthos".equals(vendor)){
            token = new OpenIDTokenProvider().getToken();
        }
        return "Bearer " + token;
    }
}
