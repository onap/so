/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.adapter.network.tasks;

import java.io.StringReader;
import java.util.Optional;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.adapters.nwrest.CreateNetworkError;
import org.onap.so.adapters.nwrest.CreateNetworkRequest;
import org.onap.so.adapters.nwrest.CreateNetworkResponse;
import org.onap.so.adapters.nwrest.DeleteNetworkError;
import org.onap.so.adapters.nwrest.DeleteNetworkRequest;
import org.onap.so.adapters.nwrest.DeleteNetworkResponse;
import org.onap.so.adapters.nwrest.UpdateNetworkError;
import org.onap.so.adapters.nwrest.UpdateNetworkRequest;
import org.onap.so.adapters.nwrest.UpdateNetworkResponse;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.orchestration.NetworkAdapterResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NetworkAdapterRestV1 {

	private static final Logger logger = LoggerFactory.getLogger(NetworkAdapterRestV1.class);
	
	private static final String NETWORK_REQUEST = "networkAdapterRequest";
	private static final String NETWORK_MESSAGE = "NetworkAResponse_MESSAGE";
	private static final String NETWORK_SYNC_CODE = "NETWORKREST_networkAdapterStatusCode";
	private static final String NETWORK_SYNC_RESPONSE = "NETWORKREST_networkAdapterResponse";
	private static final String NETWORK_CORRELATOR = "NetworkAResponse_CORRELATOR";
	
	@Autowired
	private ExceptionBuilder exceptionBuilder;

	@Autowired
	private NetworkAdapterResources networkAdapterResources;
	
	public void callNetworkAdapter (DelegateExecution execution) {
		try {
			Object networkAdapterRequest = execution.getVariable(NETWORK_REQUEST);
			if (networkAdapterRequest != null) {
				Optional<Response> response = Optional.empty();
				if (networkAdapterRequest instanceof CreateNetworkRequest) {
					CreateNetworkRequest createNetworkRequest = (CreateNetworkRequest) networkAdapterRequest;
					execution.setVariable(NETWORK_CORRELATOR, createNetworkRequest.getMessageId());
					response = networkAdapterResources.createNetworkAsync(createNetworkRequest);
				} else if (networkAdapterRequest instanceof DeleteNetworkRequest) {
					DeleteNetworkRequest deleteNetworkRequest = (DeleteNetworkRequest) networkAdapterRequest;
					execution.setVariable(NETWORK_CORRELATOR, deleteNetworkRequest.getMessageId());
					response = networkAdapterResources.deleteNetworkAsync(deleteNetworkRequest);
				}
				if(response.isPresent()) {
					String statusCode = Integer.toString(response.get().getStatus());
					String responseString = "";
					if(response.get().getEntity() != null) {
						responseString = (String) response.get().getEntity();
					}
					execution.setVariable(NETWORK_SYNC_CODE, statusCode);
					execution.setVariable(NETWORK_SYNC_RESPONSE, responseString);
				} else {
					throw new Exception("No Ack response from Openstack Adapter");
				}
			} else {
				throw new Exception("No Network Request was created. networkAdapterRequest was null.");
			}
		} catch (Exception ex) {
			exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	public void processCallback (DelegateExecution execution) {
		try {
			Object networkAdapterRequest = execution.getVariable(NETWORK_REQUEST);
			String callback = (String) execution.getVariable(NETWORK_MESSAGE);
			String logCallbackMessage = "Callback from OpenstackAdapter: " + callback;
			logger.debug(logCallbackMessage);
			if (networkAdapterRequest != null) {
				if (networkAdapterRequest instanceof CreateNetworkRequest) {
					if(callback.contains("createNetworkError")) {
						CreateNetworkError createNetworkError = (CreateNetworkError) unmarshalXml(callback, CreateNetworkError.class);
						throw new Exception(createNetworkError.getMessage());
					} else {
						CreateNetworkResponse createNetworkResponse = (CreateNetworkResponse) unmarshalXml(callback, CreateNetworkResponse.class);
						execution.setVariable("createNetworkResponse", createNetworkResponse);
					}
				} else if (networkAdapterRequest instanceof DeleteNetworkRequest) {
					if(callback.contains("deleteNetworkError")) {
						DeleteNetworkError deleteNetworkError = (DeleteNetworkError) unmarshalXml(callback, DeleteNetworkError.class);
						throw new Exception(deleteNetworkError.getMessage());
					} else {
						DeleteNetworkResponse deleteNetworkResponse = (DeleteNetworkResponse) unmarshalXml(callback, DeleteNetworkResponse.class);
						execution.setVariable("deleteNetworkResponse", deleteNetworkResponse);
					}
				} else if (networkAdapterRequest instanceof UpdateNetworkRequest) {
					if (callback.contains("updateNetworkError")) {
						UpdateNetworkError updateNetworkError = (UpdateNetworkError) unmarshalXml(callback, UpdateNetworkError.class);
						throw new Exception(updateNetworkError.getMessage());
					} else {
						UpdateNetworkResponse updateNetworkResponse = (UpdateNetworkResponse) unmarshalXml(callback, UpdateNetworkResponse.class);
						execution.setVariable("updateNetworkResponse", updateNetworkResponse);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error in Openstack Adapter callback", e);
			exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, e.getMessage());
		}
	}
	
	protected <T> Object unmarshalXml(String xmlString, Class<T> resultClass) throws JAXBException {
		StringReader reader = new StringReader(xmlString);
		JAXBContext context = JAXBContext.newInstance(resultClass);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		return unmarshaller.unmarshal(reader);
	}
	
	public void handleTimeOutException (DelegateExecution execution) {		
		exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, "Error timed out waiting on Openstack Async-Response");
	}
	
	public void handleSyncError (DelegateExecution execution) {
		String statusCode = (String) execution.getVariable(NETWORK_SYNC_CODE);
		String responseString = (String) execution.getVariable(NETWORK_SYNC_RESPONSE);
		String errorMessage = "Error with Openstack Adapter Sync Request: StatusCode = " + statusCode + " Response = " + responseString;
		exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, errorMessage);
	}
}
