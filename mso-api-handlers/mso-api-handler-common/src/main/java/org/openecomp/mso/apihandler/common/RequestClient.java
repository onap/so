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

package org.openecomp.mso.apihandler.common;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.utils.CryptoUtils;

public abstract class RequestClient {
	private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.APIH);
	protected MsoJavaProperties props;
	protected String url;
	protected HttpClient client;
	private int type;
	
	public RequestClient(int type){
		this.type = type;
	}
	
	public void setProps(MsoJavaProperties props) {
		this.props = props;
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

	public abstract HttpResponse post(String request, String requestId, String requestTimeout, String schemaVersion, String serviceInstanceId, String action) throws ClientProtocolException, IOException;

	public abstract HttpResponse post(String request) throws ClientProtocolException, IOException;
	
	public abstract HttpResponse post(String requestId, boolean isBaseVfModule,
			int recipeTimeout, String requestAction, String serviceInstanceId,
			String vnfId, String vfModuleId, String volumeGroupId, String networkId, String configurationId,
			String serviceType, String vnfType, String vfModuleType, String networkType,
			String requestDetails, String recipeParamXsd)
					throws ClientProtocolException, IOException;
	
	public abstract HttpResponse get() 
					throws ClientProtocolException, IOException;
	
	protected String getEncryptedPropValue (String prop, String defaultValue, String encryptionKey) {
		 try {
			 return CryptoUtils.decrypt(prop, encryptionKey);
		 }	
		 catch (GeneralSecurityException e) {
			 msoLogger.debug("Security exception", e);
		 }
		 return defaultValue;
	 }
}
