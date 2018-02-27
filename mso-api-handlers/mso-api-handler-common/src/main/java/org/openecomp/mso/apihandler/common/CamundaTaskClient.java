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

import org.openecomp.mso.logger.MsoLogger;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;

public class CamundaTaskClient extends RequestClient{
	private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.APIH);

	public CamundaTaskClient() {
		super(CommonConstants.CAMUNDATASK);
	}
	
	@Override
	public HttpResponse post(String jsonReq)
					throws ClientProtocolException, IOException{
		HttpPost post = new HttpPost(url);
		msoLogger.debug("Camunda Task url is: "+ url);		

		StringEntity input = new StringEntity(jsonReq);
		input.setContentType(CommonConstants.CONTENT_TYPE_JSON);

		String encryptedCredentials;
		if(props!=null){
			encryptedCredentials = props.getProperty(CommonConstants.CAMUNDA_AUTH,null);
			if(encryptedCredentials != null){
				String userCredentials = getEncryptedPropValue(encryptedCredentials, CommonConstants.DEFAULT_BPEL_AUTH, CommonConstants.ENCRYPTION_KEY);
				if(userCredentials != null){
					post.addHeader("Authorization", "Basic " + new String(DatatypeConverter.printBase64Binary(userCredentials.getBytes())));
				}
			}
		}

		post.setEntity(input);
		return client.execute(post);
	}
	
	@Override
	public HttpResponse post(String camundaReqXML, String requestId,
			String requestTimeout, String schemaVersion, String serviceInstanceId, String action)
					throws ClientProtocolException, IOException{
		msoLogger.debug("Method not supported");
		return null;
	}
	
	@Override
	public HttpResponse post(String requestId, boolean isBaseVfModule,
			int recipeTimeout, String requestAction, String serviceInstanceId,
			String vnfId, String vfModuleId, String volumeGroupId, String networkId,
			String serviceType, String vnfType, String vfModuleType, String networkType,
			String requestDetails, String recipeParamXsd)
					throws ClientProtocolException, IOException{
		msoLogger.debug("Method not supported");
		return null;
	}
	
	@Override
	public HttpResponse get() throws ClientProtocolException, IOException{
		HttpGet get = new HttpGet(url);
		msoLogger.debug("Camunda Task url is: "+ url);	
		String encryptedCredentials;
		if(props!=null){
			encryptedCredentials = props.getProperty(CommonConstants.CAMUNDA_AUTH,null);
			if(encryptedCredentials != null){
				String userCredentials = getEncryptedPropValue(encryptedCredentials, CommonConstants.DEFAULT_BPEL_AUTH, CommonConstants.ENCRYPTION_KEY);
				if(userCredentials != null){
					get.addHeader("Authorization", "Basic " + new String(DatatypeConverter.printBase64Binary(userCredentials.getBytes())));
				}
			}
		}
		
		return client.execute(get);
	}

}
