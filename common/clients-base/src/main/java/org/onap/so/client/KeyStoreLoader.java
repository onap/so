/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Deutsche Telekom.
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
import java.nio.file.Paths;
import java.security.KeyStore;

public abstract class KeyStoreLoader {

    static final String SSL_KEY_STORE_KEY = "javax.net.ssl.keyStore";

    static public KeyStore getKeyStore() {
        KeyStore ks = null;
        final char[] password = getSSlKeyStorePassword().toCharArray();
        try (FileInputStream fis =
                new FileInputStream(Paths.get(System.getProperty(SSL_KEY_STORE_KEY)).normalize().toString())) {
            ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(fis, password);
        } catch (final Exception e) {
            return null;
        }

        return ks;
    }

    static public String getSSlKeyStorePassword() {
        return System.getProperty("javax.net.ssl.keyStorePassword");
    }
}
