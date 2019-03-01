/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.apihandler.common;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.onap.so.utils.CryptoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

public abstract class RequestClient {
	private static Logger logger = LoggerFactory.getLogger(RequestClient.class);
	protected Environment props;
	protected String url;
	protected HttpClient client;
	private int type;
	
	public RequestClient(int type){
		this.type = type;
	}
	
	public void setProps(Environment env) {
		this.props = env;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getUrl() {
		return url;
	}

	public int getType(){
		return type;
	}

	public HttpClient getClient() {
		return client;
	}

	public void setClient(HttpClient client) {
		this.client = client;
	}

	public abstract HttpResponse post(String request, String requestId, String requestTimeout, String schemaVersion, String serviceInstanceId, String action) throws IOException;

	public abstract HttpResponse post(String request) throws ClientProtocolException, IOException;
	
	public abstract HttpResponse post(RequestClientParameter parameterObject) throws ClientProtocolException, IOException;

	public abstract HttpResponse get() 
					throws IOException;
	
	protected String decryptPropValue(String prop, String defaultValue, String encryptionKey) {
		 try {
			 String result = CryptoUtils.decrypt(prop, encryptionKey);
			 return result;
		 }	
		 catch (GeneralSecurityException e) {
			 logger.debug("Security exception", e);
		 }
		 return defaultValue;
	 }
	
	protected String getEncryptedPropValue (String prop, String defaultValue, String encryptionKey) {
		 try {
			 String result = CryptoUtils.decrypt(prop, encryptionKey);
			 return result;
		 }	
		 catch (GeneralSecurityException e) {
			 logger.debug("Security exception", e);
		 }
		 return defaultValue;
	 }

	


}
