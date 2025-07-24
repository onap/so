/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.onap.so.client;

import java.net.URI;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

public abstract class RestClientSSL extends RestClient {

    private static final String TRUE = "true";
    private static final String MSO_LOAD_SSL_CLIENT_KEYSTORE_KEY = "mso.load.ssl.client.keystore";

    protected RestClientSSL(RestProperties props, Optional<URI> path) {
        super(props, path);
    }

    protected RestClientSSL(RestProperties props, Optional<URI> path, String accept, String contentType) {
        super(props, path, accept, contentType);
    }

    @Override
    protected Client getClient() {

        Client client = null;
        try {
            String loadSSLKeyStore = System.getProperty(RestClientSSL.MSO_LOAD_SSL_CLIENT_KEYSTORE_KEY);
            if (loadSSLKeyStore != null && loadSSLKeyStore.equalsIgnoreCase(TRUE)) {
                KeyStore ks = KeyStoreLoader.getKeyStore();
                if (ks != null) {
                    client = ClientBuilder.newBuilder().keyStore(ks, KeyStoreLoader.getSSlKeyStorePassword()).build();
                    logger.info("RestClientSSL not using default SSL context - setting keystore here.");
                    return client;
                }
            }
            // Use default SSL context
            client = getClientBuilder().sslContext(SSLContext.getDefault()).build();
            logger.info("RestClientSSL using default SSL context!");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return client;
    }
}
