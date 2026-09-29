// Copyright © Microsoft Corporation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.schema.provider.azure.di;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import org.junit.Test;
import org.mockito.Mockito;
import org.opengroup.osdu.schema.azure.di.AzureBootstrapConfig;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

public class AzureBootstrapConfigTest {
    private AzureBootstrapConfig bootstrapConfig = new AzureBootstrapConfig();
    private SecretClient kv = Mockito.mock(SecretClient.class);

    @Test(expected = IllegalStateException.class)
    public void kvSecret_ChecksForNullResponse() {
        //Verify that secret being null is captured correctly
        doReturn(null).when(kv).getSecret("secret-name");
        bootstrapConfig.getKeyVaultSecret(kv, "secret-name");
    }

    @Test(expected = IllegalStateException.class)
    public void kvSecret_checksForNullValueWithinResponse() {
        KeyVaultSecret secret = Mockito.mock(KeyVaultSecret.class);
        // key-vault had non-null response, but the secret
        // contained within was null.
        doReturn(null).when(secret).getValue();
        doReturn(secret).when(kv).getSecret("secret-name");

        bootstrapConfig.getKeyVaultSecret(kv, "secret-name");
    }

}
