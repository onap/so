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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.openecomp.mso.apihandler.camundabeans.CamundaResponse;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.logger.MessageEnum;

public class ResponseHandler {

	private CamundaResponse response;
	private int status;
	private String responseBody="";
	private HttpResponse httpResponse;
	private int type;
	private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.APIH);
    private static final String RESPONSE_BODY_MSG = "response body is: ";

	public ResponseHandler(HttpResponse httpResponse, int type) {
		this.httpResponse = httpResponse;
		this.type=type;
		parseResponse();
	}


	private void parseResponse() {
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		msoLogger.debug("Returned status  is: " + statusCode);		
		status = setStatus(statusCode);
		msoLogger.debug("Parsed status  is: " + status);
		if(type==CommonConstants.CAMUNDA){
			parseCamunda();
		}else if(type==CommonConstants.CAMUNDATASK){
			parseCamundaTask();
		}else {
			parseBpel();
		}
		
	}
	

	
	private void parseCamunda(){
		try{
				HttpEntity entity = httpResponse.getEntity();
				responseBody = EntityUtils.toString(entity);
			} catch (IOException e) {
				msoLogger.debug("IOException getting Camunda response body", e);
			}
		
			ObjectMapper mapper = new ObjectMapper(); 
			try {
				response = mapper.readValue(responseBody, CamundaResponse.class);
			} catch (IOException e) {
				msoLogger.debug("IOException getting Camunda response body", e);
			}
			msoLogger.debug("json response is: " + responseBody);
			if(response!=null){
				responseBody = response.getResponse();
			}
			msoLogger.debug(RESPONSE_BODY_MSG + responseBody);
			
		
		if(status!=HttpStatus.SC_ACCEPTED){
			msoLogger.error(MessageEnum.APIH_ERROR_FROM_BPEL_SERVER, "Camunda", String.valueOf(status), responseBody, "Camunda", "parseCamunda", MsoLogger.ErrorCode.BusinessProcesssError, "Error in APIH from Camunda");
		}
	}
	
	private void parseBpel(){

		HttpEntity bpelEntity = httpResponse.getEntity();

		try {
			if (bpelEntity!=null) {
				responseBody = EntityUtils.toString(bpelEntity);
				msoLogger.debug(RESPONSE_BODY_MSG + responseBody);

			}
			if(status!=HttpStatus.SC_ACCEPTED){
				msoLogger.error(MessageEnum.APIH_ERROR_FROM_BPEL_SERVER, "BPEL", String.valueOf(status), responseBody, "BPEL", "parseBpel", MsoLogger.ErrorCode.BusinessProcesssError, "Error in APIH from BPEL");
			}
		} 
		catch (IOException e) {
			msoLogger.debug("IOException getting BPEL response body", e);
		}
	}
	
	private void parseCamundaTask(){

		HttpEntity camundataskEntity = httpResponse.getEntity();

		try {
			if (camundataskEntity!=null) {
				responseBody = EntityUtils.toString(camundataskEntity);
				msoLogger.debug(RESPONSE_BODY_MSG + responseBody);

			}
			if(status!=HttpStatus.SC_NO_CONTENT && status != HttpStatus.SC_ACCEPTED){
				msoLogger.error(MessageEnum.APIH_ERROR_FROM_BPEL_SERVER, "CAMUNDATASK", String.valueOf(status), responseBody, "CAMUNDATASK", "parseCamundaTask", MsoLogger.ErrorCode.BusinessProcesssError, "Error in APIH from Camunda Task");
			}
		} 
		catch (IOException e) {
			msoLogger.debug("IOException getting Camunda Task response body", e);
		}
	}

	private int setStatus(int statusCode){
		int httpStatus;
		switch(statusCode) {
		case HttpStatus.SC_ACCEPTED:
		case HttpStatus.SC_OK:		
			httpStatus = HttpStatus.SC_ACCEPTED;
			break;
		case HttpStatus.SC_BAD_REQUEST:
			httpStatus = HttpStatus.SC_BAD_REQUEST;
			break;
		case HttpStatus.SC_UNAUTHORIZED:
		case HttpStatus.SC_FORBIDDEN:
			httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
			break;
		case HttpStatus.SC_NOT_FOUND:
			httpStatus = HttpStatus.SC_NOT_IMPLEMENTED;
			break;
		case HttpStatus.SC_INTERNAL_SERVER_ERROR:
			httpStatus = HttpStatus.SC_BAD_GATEWAY;
			break;
		case HttpStatus.SC_SERVICE_UNAVAILABLE:
			httpStatus = HttpStatus.SC_SERVICE_UNAVAILABLE;
			break;
		case HttpStatus.SC_NO_CONTENT:
			httpStatus = HttpStatus.SC_NO_CONTENT;
			break;
		default:
			httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
			break;
		}
		return httpStatus;
	}


	public CamundaResponse getResponse() {
		return response;
	}


	public void setResponse(CamundaResponse response) {
		this.response = response;
	}


	public String getResponseBody() {
		return responseBody;
	}


	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}


	public int getStatus() {
		return status;
	}

}
