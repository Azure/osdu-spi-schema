/*
 * Copyright © 2020 Amazon Web Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.schema.util;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;
import org.opengroup.osdu.core.aws.entitlements.ServicePrincipal;
import org.opengroup.osdu.core.aws.iam.IAMConfig;
import org.opengroup.osdu.core.aws.secrets.SecretsManager;


public class AwsServicePrincipalUtil {

    private static final String idpName = System.getProperty("IDP_NAME", System.getenv("IDP_NAME"));
    private static final String amazonRegion = System.getProperty("AWS_REGION", System.getenv("AWS_REGION"));

    private static final AWSCredentialsProvider amazonAWSCredentials = IAMConfig.amazonAWSCredentials();
    private static final AWSSimpleSystemsManagement ssmManager = AWSSimpleSystemsManagementClientBuilder.standard()
            .withCredentials(amazonAWSCredentials)
            .withRegion(amazonRegion)
            .build();
    private static final SecretsManager sm = new SecretsManager();

    private static final String oauth_token_url = "/osdu/idp/" + idpName + "/oauth/token-uri";
    private static final String oauth_custom_scope = "/osdu/idp/" + idpName + "/oauth/custom-scope";
    private static final String client_credentials_client_id = "/osdu/idp/" + idpName + "/client/client-credentials/id";
    private static final String client_secret_key = "client_credentials_client_secret";
    private static final String client_secret_secretName = "/osdu/idp/" + idpName + "/client-credentials-secret";
    private static final String client_credentials_clientid = getSsmParameter(client_credentials_client_id);
    private static final String client_credentials_secret = sm.getSecret(client_secret_secretName, amazonRegion, client_secret_key);
    private static final String tokenUrl = getSsmParameter(oauth_token_url);
    private static final String awsOauthCustomScope = getSsmParameter(oauth_custom_scope);

    private static final ServicePrincipal sp = new ServicePrincipal(amazonRegion, tokenUrl, awsOauthCustomScope);

    public static String getAccessToken() throws Exception {
        return sp.getServicePrincipalAccessToken(client_credentials_clientid, client_credentials_secret).replace("Bearer ", "");
    }

    private static String getSsmParameter(String parameterKey) {
        GetParameterRequest paramRequest = (new GetParameterRequest()).withName(parameterKey).withWithDecryption(true);
        GetParameterResult paramResult = ssmManager.getParameter(paramRequest);
        return paramResult.getParameter().getValue();
    }

}