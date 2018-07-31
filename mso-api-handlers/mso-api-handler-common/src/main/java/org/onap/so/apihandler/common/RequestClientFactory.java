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

package org.onap.so.apihandler.common;




import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class RequestClientFactory {
	
	@Autowired
	private Environment env;
	
	//based on URI, returns BPEL, CamundaTask or Camunda client
	public RequestClient getRequestClient(String orchestrationURI) throws IllegalStateException{
		RequestClient retClient;

		String url;
		if(orchestrationURI.contains(CommonConstants.TASK_SEARCH_STR)){
			url = env.getProperty(CommonConstants.CAMUNDA_URL) + orchestrationURI;
			retClient = new CamundaTaskClient();
		}
		else{
			url = env.getProperty(CommonConstants.CAMUNDA_URL) + orchestrationURI;
			retClient = new CamundaClient();
		}
		retClient.setClient(new DefaultHttpClient());
		retClient.setProps(env);
		retClient.setUrl(url);
		return retClient;
		
	}

	public Environment getEnv() {
		return env;
	}

	public void setEnv(Environment env) {
		this.env = env;
	}
	
	


}
