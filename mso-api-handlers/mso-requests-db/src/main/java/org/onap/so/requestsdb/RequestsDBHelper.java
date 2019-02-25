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

package org.onap.so.requestsdb;

import java.sql.Timestamp;

import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class RequestsDBHelper {
		
	private static final String UNKNOWN = "UNKNOWN";
	private static Logger logger = LoggerFactory.getLogger(RequestsDBHelper.class);
	private String className = this.getClass().getSimpleName() +" class\'s ";
	private String methodName = "";
	private String classMethodMessage = "";
	@Autowired
	@Qualifier("RequestsDbClient")
	private RequestsDbClient requestsDbClient;
	/**
	 * This util method is to update the InfraRequest table to Complete
	 * @param msg - string, unique message for each caller
	 * @param requestId - string
	 * @param operationalEnvironmentId - string   
	 * @return void - nothing 
	 * @throws Exception 
	 */	
	public void updateInfraSuccessCompletion(String msg, String requestId, String operationalEnvironmentId) {
		methodName = "updateInfraSuccessCompletion() method.";
		classMethodMessage = className + " " + methodName;
		logger.debug("Begin of {}", classMethodMessage);
			
		InfraActiveRequests request = requestsDbClient.getInfraActiveRequestbyRequestId(requestId);
	
		request.setRequestStatus("COMPLETE");
		request.setStatusMessage("SUCCESSFUL, operationalEnvironmentId - " + operationalEnvironmentId + "; Success Message: " + msg);
		request.setProgress(100L);
		request.setLastModifiedBy("APIH");
		request.setOperationalEnvId(operationalEnvironmentId);
		if(request.getAction() == null){
			request.setRequestAction(UNKNOWN);
		}
		if(request.getRequestScope() == null){
			request.setRequestScope(UNKNOWN);
		}
		Timestamp endTimeStamp = new Timestamp(System.currentTimeMillis());
        request.setEndTime(endTimeStamp);
		requestsDbClient.save(request);
		
		logger.debug("End of {}", classMethodMessage);
		
	}
	
	/**
	 * This util method is to update the InfraRequest table to Failure
	 * @param msg - string, unique message for each caller
	 * @param requestId - string
	 * @param operationalEnvironmentId - string   
	 * @return void - nothing 
	 * @throws Exception 
	 */	
	public void updateInfraFailureCompletion(String msg, String requestId, String operationalEnvironmentId) {
		methodName = "updateInfraFailureCompletion() method.";
		classMethodMessage = className + " " + methodName;
		logger.debug("Begin of {}", classMethodMessage);
		
		InfraActiveRequests request = requestsDbClient.getInfraActiveRequestbyRequestId(requestId);
		request.setRequestStatus("FAILED");
		request.setStatusMessage("FAILURE, operationalEnvironmentId - " + operationalEnvironmentId + "; Error message: " + msg);
		request.setProgress(100L);
		request.setLastModifiedBy("APIH");
		request.setOperationalEnvId(operationalEnvironmentId);
		if(request.getAction() == null){
			request.setRequestAction(UNKNOWN);
		}
		if(request.getRequestScope() == null){
			request.setRequestScope(UNKNOWN);
		}
		Timestamp endTimeStamp = new Timestamp(System.currentTimeMillis());
        request.setEndTime(endTimeStamp);
		requestsDbClient.save(request);
		
		logger.debug("End of {}", classMethodMessage);
		
	}	
}
