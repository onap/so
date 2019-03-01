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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.onap.so.apihandler.camundabeans.CamundaResponse;
import org.onap.so.apihandlerinfra.Constants;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.exceptions.BPMNFailureException;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;
import org.onap.so.logger.MessageEnum;
import org.onap.so.logger.MsoLogger;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseHandler {

	private CamundaResponse response;
	private int status;
	private String responseBody="";
	private HttpResponse httpResponse;
	private int type;
	private static Logger logger = LoggerFactory.getLogger(ResponseHandler.class);

	public ResponseHandler(HttpResponse httpResponse, int type) throws ApiException{
		this.httpResponse = httpResponse;
		this.type=type;
		parseResponse();
	}


	private void parseResponse() throws ApiException{
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		status = setStatus(statusCode);
		if(type==CommonConstants.CAMUNDA){
			parseCamunda();
		}else if(type==CommonConstants.CAMUNDATASK){
			parseCamundaTask();
		}else {
			parseBpel();
		}
		
	}
	

	
	private void parseCamunda() throws ApiException{
		try{
				HttpEntity entity = httpResponse.getEntity();
				responseBody = EntityUtils.toString(entity);
			} catch (IOException e) {
				ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_VALIDATION_ERROR, MsoLogger.ErrorCode.SchemaError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();


				ValidateException validateException = new ValidateException.Builder("IOException getting Camunda response body", HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).cause(e)
					.errorInfo(errorLoggerInfo).build();
				throw validateException;
			}
		
			ObjectMapper mapper = new ObjectMapper(); 
			try {
				response = mapper.readValue(responseBody, CamundaResponse.class);
			} catch (IOException e) {
				ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, MsoLogger.ErrorCode.SchemaError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();


				ValidateException validateException = new ValidateException.Builder("Cannot parse Camunda Response", HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).cause(e)
						.errorInfo(errorLoggerInfo).build();
				throw validateException;
			}
			if(response!=null){
				responseBody = response.getResponse();
			}else{
                ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_ERROR_FROM_BPEL_SERVER, MsoLogger.ErrorCode.BusinessProcesssError)
                        .targetEntity("Camunda").targetServiceName("parseCamunda").build();
                BPMNFailureException bpmnFailureException = new BPMNFailureException.Builder(String.valueOf(status), status, ErrorNumbers.ERROR_FROM_BPEL)
                        .errorInfo(errorLoggerInfo).build();
            }
	}
	
	private void parseBpel() throws ApiException{

		HttpEntity bpelEntity = httpResponse.getEntity();

		try {
			if (bpelEntity != null) {
				responseBody = EntityUtils.toString(bpelEntity);

			}
		}catch(IOException e){
			ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_GENERAL_EXCEPTION, MsoLogger.ErrorCode.DataError).build();
			ValidateException validateException = new ValidateException.Builder("Could not convert BPEL response to string", HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).cause(e)
					.errorInfo(errorLoggerInfo).build();
			throw validateException;
		}
		if(status!=HttpStatus.SC_ACCEPTED){
			ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_ERROR_FROM_BPEL_SERVER, MsoLogger.ErrorCode.BusinessProcesssError)
					.targetEntity("BPEL").targetServiceName("parseBpel").build();


			BPMNFailureException bpmnFailureException = new BPMNFailureException.Builder(String.valueOf(status), status, ErrorNumbers.ERROR_FROM_BPEL)
					.errorInfo(errorLoggerInfo).build();

			throw bpmnFailureException;
		}

	}
	
	private void parseCamundaTask() throws ApiException{

		HttpEntity camundataskEntity = httpResponse.getEntity();

		try {
			if (camundataskEntity != null) {
				responseBody = EntityUtils.toString(camundataskEntity);
			}
		}catch(IOException e) {
			ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_GENERAL_EXCEPTION, MsoLogger.ErrorCode.DataError).build();


			ValidateException validateException = new ValidateException.Builder("Could not convert CamundaTask response to string", HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).cause(e)
					.errorInfo(errorLoggerInfo).build();
			throw validateException;
		}
		if(status!=HttpStatus.SC_NO_CONTENT && status != HttpStatus.SC_ACCEPTED){
			ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_ERROR_FROM_BPEL_SERVER, MsoLogger.ErrorCode.BusinessProcesssError)
					.targetEntity("CAMUNDATASK").targetServiceName("parseCamundaTask").build();


			BPMNFailureException bpmnFailureException = new BPMNFailureException.Builder(String.valueOf(status), status, ErrorNumbers.ERROR_FROM_BPEL)
					.errorInfo(errorLoggerInfo).build();

			throw bpmnFailureException;
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
