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


import org.apache.http.impl.client.DefaultHttpClient;

import org.openecomp.mso.properties.MsoJavaProperties;

public class RequestClientFactory {

    private RequestClientFactory() {
    }

	//based on URI, returns BPEL, CamundaTask or Camunda client
	public static RequestClient getRequestClient(String orchestrationURI, MsoJavaProperties props) throws IllegalStateException{
		RequestClient retClient;
		if(props ==null){
			throw new IllegalStateException("properties is null");
		}
		String url;
		if(orchestrationURI.contains(CommonConstants.BPEL_SEARCH_STR)){
			url = props.getProperty(CommonConstants.BPEL_URL,null) + orchestrationURI;
			retClient= new BPELRestClient();
			
		}else if(orchestrationURI.contains(CommonConstants.TASK_SEARCH_STR)){
			url = props.getProperty(CommonConstants.CAMUNDA_URL,null) + orchestrationURI;
			retClient = new CamundaTaskClient();
		}
		else{
			url = props.getProperty(CommonConstants.CAMUNDA_URL,null) + orchestrationURI;
			retClient = new CamundaClient();
		}
		retClient.setClient(new DefaultHttpClient());
		retClient.setProps(props);
		retClient.setUrl(url);
		return retClient;
		
	}
	


}
