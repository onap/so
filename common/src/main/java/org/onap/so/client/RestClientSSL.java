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

import java.io.FileInputStream;
import java.net.URI;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

public abstract class RestClientSSL extends RestClient {

    private static final String TRUE = "true";
    public static final String SSL_KEY_STORE_KEY = "javax.net.ssl.keyStore";
    public static final String SSL_KEY_STORE_PASSWORD_KEY = "javax.net.ssl.keyStorePassword";
    public static final String MSO_LOAD_SSL_CLIENT_KEYSTORE_KEY = "mso.load.ssl.client.keystore";


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
                KeyStore ks = getKeyStore();
                if (ks != null) {
                    client = ClientBuilder.newBuilder()
                            .keyStore(ks, System.getProperty(RestClientSSL.SSL_KEY_STORE_PASSWORD_KEY)).build();
                    logger.info("RestClientSSL not using default SSL context - setting keystore here.");
                    return client;
                }
            }
            // Use default SSL context
            client = ClientBuilder.newBuilder().sslContext(SSLContext.getDefault()).build();
            logger.info("RestClientSSL using default SSL context!");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return client;
    }

    private KeyStore getKeyStore() {
        KeyStore ks = null;
        char[] password = System.getProperty(RestClientSSL.SSL_KEY_STORE_PASSWORD_KEY).toCharArray();
        try (FileInputStream fis = new FileInputStream(
                Paths.get(System.getProperty(RestClientSSL.SSL_KEY_STORE_KEY)).normalize().toString())) {
            ks = KeyStore.getInstance(KeyStore.getDefaultType());

            ks.load(fis, password);
        } catch (Exception e) {
            return null;
        }

        return ks;
    }
}
