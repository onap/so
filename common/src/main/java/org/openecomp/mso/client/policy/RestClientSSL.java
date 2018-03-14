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

package org.openecomp.mso.client.policy;

import java.io.FileInputStream;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.security.KeyStore;
import java.util.Optional;
import java.util.UUID;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.openecomp.mso.client.RestProperties;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;

public abstract class RestClientSSL extends RestClient {
	
	public static final String SSL_KEY_STORE_KEY = "javax.net.ssl.keyStore";
	public static final String SSL_KEY_STORE_PASSWORD_KEY = "javax.net.ssl.keyStorePassword";
	public static final String MSO_LOAD_SSL_CLIENT_KEYSTORE_KEY = "mso.load.ssl.client.keystore";
	

	protected RestClientSSL(RestProperties props, UUID requestId, Optional<URI> path) {
		super(props, requestId, path);
	}

	protected RestClientSSL(RestProperties props, UUID requestId, Optional<URI> path, String accept, String contentType) {
		super(props, requestId, path, accept, contentType);
	}

	@Override
	protected Client getClient() {
		
		Client client = null;
		try {
			String loadSSLKeyStore = System.getProperty(RestClientSSL.MSO_LOAD_SSL_CLIENT_KEYSTORE_KEY);
			if(loadSSLKeyStore != null && loadSSLKeyStore.equalsIgnoreCase("true")) {
				KeyStore ks = getKeyStore();
				if(ks != null) {
					client = ClientBuilder.newBuilder().keyStore(ks, System.getProperty(RestClientSSL.SSL_KEY_STORE_PASSWORD_KEY)).build();
					this.msoLogger.debug("RestClientSSL not using default SSL context - setting keystore here.");
					return client;
				}
			}
			//Use default SSL context 
			client = ClientBuilder.newBuilder().sslContext(SSLContext.getDefault()).build();
			this.msoLogger.debug("RestClientSSL using default SSL context!");
		} catch (NoSuchAlgorithmException e) {
			this.msoLogger.error(MessageEnum.APIH_GENERAL_EXCEPTION, "AAI", "Client init", MsoLogger.ErrorCode.UnknownError, "could not create SSL client", e);
			throw new RuntimeException(e);
		}
		return client;
	}
	
	private KeyStore getKeyStore() {
		KeyStore ks = null;
	    char[] password = System.getProperty(RestClientSSL.SSL_KEY_STORE_PASSWORD_KEY).toCharArray();
	    FileInputStream fis = null;
	    try {
	    	ks = KeyStore.getInstance(KeyStore.getDefaultType());
	        fis = new FileInputStream(System.getProperty(RestClientSSL.SSL_KEY_STORE_KEY));
	        ks.load(fis, password);
	    }
	    catch(Exception e) {
	    	return null;
	    }
	    finally {
	        if (fis != null) {
	            try { 
	            	fis.close();
	            }
	            catch(Exception e) {}
	        }
	    }
	    return ks;
	}
}
