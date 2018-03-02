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

package org.openecomp.mso.apihandlerinfra.tenantisolation;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.openecomp.mso.apihandler.common.ValidationException;
import org.openecomp.mso.apihandlerinfra.Constants;
import org.openecomp.mso.apihandlerinfra.MsoException;
import org.openecomp.mso.apihandlerinfra.Status;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.Action;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.Manifest;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.OperationalEnvironment;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.RelatedInstance;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.RelatedInstanceList;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.RequestDetails;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.RequestInfo;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.RequestParameters;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.ResourceType;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.ServiceModelList;
import org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType;
import org.openecomp.mso.db.AbstractSessionFactoryManager;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.requestsdb.InfraActiveRequests;
import org.openecomp.mso.requestsdb.RequestsDatabase;
import org.openecomp.mso.requestsdb.RequestsDbSessionFactoryManager;
import org.openecomp.mso.serviceinstancebeans.PolicyException;
import org.openecomp.mso.serviceinstancebeans.RequestError;
import org.openecomp.mso.serviceinstancebeans.ServiceException;
import org.openecomp.mso.utils.UUIDChecker;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TenantIsolationRequest {

    private String requestId;
    private String requestJSON;
    private RequestInfo requestInfo;

    private String errorMessage;
    private String errorCode;
    private String httpResponse;
    private String responseBody;
    private RequestStatusType status;
    private CloudOrchestrationRequest cor;
    private String operationalEnvironmentId;
    private long progress = Constants.PROGRESS_REQUEST_RECEIVED;
    private String requestScope;

    
    
    private static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.APIH);
    protected AbstractSessionFactoryManager requestsDbSessionFactoryManager = new RequestsDbSessionFactoryManager ();

	TenantIsolationRequest (String requestId) {
        this.requestId = requestId;
        MsoLogger.setLogContext (requestId, null);
    }

	TenantIsolationRequest () {
        MsoLogger.setLogContext (requestId, null);
    }
    
	void parse(CloudOrchestrationRequest request, HashMap<String,String> instanceIdMap, Action action) throws ValidationException {
		msoLogger.debug ("Validating the Cloud Orchestration request");
		this.cor = request;
		this.requestInfo = request.getRequestDetails().getRequestInfo();
		
		try{
        	ObjectMapper mapper = new ObjectMapper();
        	requestJSON = mapper.writeValueAsString(request.getRequestDetails());

        } catch(Exception e){
        	throw new ValidationException ("Parse ServiceInstanceRequest to JSON string");
        }
		
		String envId = null;
		if(instanceIdMap != null) {
			envId = instanceIdMap.get("operationalEnvironmentId");
			if(envId != null && !UUIDChecker.isValidUUID (envId)){
				throw new ValidationException ("operationalEnvironmentId");
			}
			cor.setOperationalEnvironmentId(envId);
		}
		
		this.operationalEnvironmentId = envId;
		 
		RequestDetails requestDetails = request.getRequestDetails();
		RequestParameters requestParameters = requestDetails.getRequestParameters();
		
		requestInfoValidation(action, requestInfo);
		
		requestParamsValidation(action, requestParameters);
		
		relatedInstanceValidation(action, requestDetails, requestParameters);
		
	}

	private void relatedInstanceValidation(Action action, RequestDetails requestDetails, RequestParameters requestParameters) throws ValidationException {
		RelatedInstanceList[] instanceList = requestDetails.getRelatedInstanceList();
		
		if((Action.activate.equals(action) || Action.deactivate.equals(action)) && OperationalEnvironment.ECOMP.equals(requestParameters.getOperationalEnvironmentType())) {
			throw new ValidationException("operationalEnvironmentType in requestParameters");
		}
		
		if(!Action.deactivate.equals(action) && OperationalEnvironment.VNF.equals(requestParameters.getOperationalEnvironmentType())) {
			if(instanceList != null && instanceList.length > 0) {
			 	for(RelatedInstanceList relatedInstanceList : instanceList){
			 		RelatedInstance relatedInstance = relatedInstanceList.getRelatedInstance();
			 		
			 		if(relatedInstance.getResourceType() == null) {
			 			throw new ValidationException("ResourceType in relatedInstance");
			 		}
			 		
			 		if(!empty(relatedInstance.getInstanceName()) && !relatedInstance.getInstanceName().matches(Constants.VALID_INSTANCE_NAME_FORMAT)) {
						throw new ValidationException ("instanceName format");
					} 
			 		
			 		if (empty (relatedInstance.getInstanceId ())) {
			 			throw new ValidationException ("instanceId in relatedInstance");
			 		}
			 		
			 		if (!UUIDChecker.isValidUUID (relatedInstance.getInstanceId ())) {
			 			throw new ValidationException ("instanceId format in relatedInstance");
			 		}
			 	}
			} else {
				throw new ValidationException ("relatedInstanceList");
			}
		}
	}

	private void requestParamsValidation(Action action, RequestParameters requestParameters) throws ValidationException {
		
		if(requestParameters != null) {
			if(!Action.deactivate.equals(action) && requestParameters.getOperationalEnvironmentType() == null) {
				throw new ValidationException ("OperationalEnvironmentType");
			}
			
			if (Action.create.equals(action) && empty(requestParameters.getTenantContext())) {
				throw new ValidationException ("Tenant Context");
			}
			if (!Action.deactivate.equals(action) && empty(requestParameters.getWorkloadContext())) {
				throw new ValidationException ("Workload Context");
			}
			
			Manifest manifest = requestParameters.getManifest();
			
			if(Action.activate.equals(action)) {
				if(manifest == null) {
					throw new ValidationException ("Manifest on Activate");
				} else {
					List<ServiceModelList> serviceModelList = manifest.getServiceModelList();
					
					if(serviceModelList.size() == 0) {
						throw new ValidationException (" empty ServiceModelList");
					}
					
					for(ServiceModelList list : serviceModelList) {
						if(empty(list.getServiceModelVersionId())) {
							throw new ValidationException ("ServiceModelVersionId");
						}
						
						if (!UUIDChecker.isValidUUID (list.getServiceModelVersionId())) {
				 			throw new ValidationException ("ServiceModelVersionId format");
				 		}
						
						if(list.getRecoveryAction() == null) {
							throw new ValidationException ("RecoveryAction");
						}
					}
				}
			}
		} else if(!Action.deactivate.equals(action)) {
			throw new ValidationException("request Parameters");
		}
	}

	private void requestInfoValidation(Action action, RequestInfo requestInfo) throws ValidationException {
		 
		if(Action.create.equals(action) && empty(requestInfo.getInstanceName())) {
			throw new ValidationException ("instanceName");
		} 
		
		if(!empty(requestInfo.getInstanceName()) && !requestInfo.getInstanceName().matches(Constants.VALID_INSTANCE_NAME_FORMAT)) {
			throw new ValidationException ("instanceName format");
		} 
		
		if (empty(requestInfo.getSource())) {
        	throw new ValidationException ("source");
        }
		
		if(empty(requestInfo.getRequestorId())) {
        	throw new ValidationException ("requestorId");
        }
		
		ResourceType resourceType = requestInfo.getResourceType();
		if(resourceType == null) {
			throw new ValidationException ("resourceType");
		}
		
		this.requestScope = resourceType.name();
	}
	
	void parseOrchestration (CloudOrchestrationRequest cor) throws ValidationException {

        msoLogger.debug ("Validating the Orchestration request");

        this.cor = cor;

        try{
        	ObjectMapper mapper = new ObjectMapper();
        	//mapper.configure(Feature.WRAP_ROOT_VALUE, true);
        	requestJSON = mapper.writeValueAsString(cor.getRequestDetails());

        } catch(Exception e){
        	throw new ValidationException ("Parse CloudOrchestrationRequest to JSON string", e);
        }

        this.requestInfo = cor.getRequestDetails().getRequestInfo();

        if (this.requestInfo == null) {
            throw new ValidationException ("requestInfo");
        }

        if (empty (requestInfo.getSource ())) {
        	throw new ValidationException ("source");
        }
        if (empty (requestInfo.getRequestorId ())) {
        	throw new ValidationException ("requestorId");
        }
    }
	
    public void createRequestRecord (Status status, Action action) {
    	 Session session = null;
         try {

             session = requestsDbSessionFactoryManager.getSessionFactory ().openSession ();
             session.beginTransaction ();

             if (null == cor) {
                 cor = new CloudOrchestrationRequest();
             }

             InfraActiveRequests aq = new InfraActiveRequests ();
             aq.setRequestId (requestId);

             aq.setRequestAction(action.name());
             aq.setAction(action.name());

             Timestamp startTimeStamp = new Timestamp (System.currentTimeMillis());

             aq.setStartTime (startTimeStamp);

             if (requestInfo != null) {

             	if(requestInfo.getSource() != null){
             		aq.setSource(requestInfo.getSource());
             	}
             	if(requestInfo.getRequestorId() != null) {
             		aq.setRequestorId(requestInfo.getRequestorId());
             	}
             	if(requestInfo.getResourceType() != null) {
             		aq.setRequestScope(requestInfo.getResourceType().name());
             	}
             }
             
             if(ResourceType.operationalEnvironment.name().equalsIgnoreCase(requestScope)) {
             	aq.setOperationalEnvId(operationalEnvironmentId);
             	aq.setOperationalEnvName(requestInfo.getInstanceName());
             }

             aq.setRequestBody (this.requestJSON);

             aq.setRequestStatus (status.toString ());
             aq.setLastModifiedBy (Constants.MODIFIED_BY_APIHANDLER);

             if ((status == Status.FAILED) || (status == Status.COMPLETE)) {
                 aq.setStatusMessage (this.errorMessage);
                 aq.setResponseBody (this.responseBody);
                 aq.setProgress(new Long(100));

                 Timestamp endTimeStamp = new Timestamp (System.currentTimeMillis());
                 aq.setEndTime (endTimeStamp);
             } else if(status == Status.IN_PROGRESS) {
            	 aq.setProgress(Constants.PROGRESS_REQUEST_IN_PROGRESS);
             }

             msoLogger.debug ("About to insert a record");

             session.save (aq);
             session.getTransaction ().commit ();
             session.close ();
         } catch (Exception e) {
         	msoLogger.error (MessageEnum.APIH_DB_INSERT_EXC, "", "", MsoLogger.ErrorCode.DataError, "Exception when creation record request", e);
             if (session != null) {
                 session.close ();
             }
             if (!status.equals (Status.FAILED)) {
                 throw e;
             }
         }
    }
	
    
    public Map<String, String> getOrchestrationFilters (MultivaluedMap<String, String> queryParams) throws ValidationException {
        String queryParam = null;
        Map<String, String> orchestrationFilterParams = new HashMap<String, String>();

        for (Entry<String,List<String>> entry : queryParams.entrySet()) {
            queryParam = entry.getKey();
            try{
          		  for(String value : entry.getValue()) {
          			  if(StringUtils.isBlank(value)) {
          				  throw new Exception(queryParam + " value");
          			  }
          			  orchestrationFilterParams.put(queryParam, value);
          		  }
            }catch(Exception e){
                throw new ValidationException (e.getMessage());
        	}
        }

        return orchestrationFilterParams;
  }
    
    /**
     * Build Error Response for Exception handling.
     * 
     * @param int
     * @param httpResponseCode the HTTP response code
     * @param exceptionType.
     * @param text the error description
     * @param messageId
     * @return the web service response
     *     
     */
    public Response buildServiceErrorResponse (int httpResponseCode,
									            MsoException exceptionType,
									            String text,
									            String messageId,
									            List<String> variables) {

    	this.errorCode = messageId;

    	if (text != null) {
    		this.errorMessage = text;
    	}
    	else {
    		this.errorMessage = "";
    	}
    	this.httpResponse = Integer.toString(httpResponseCode);
    	if(errorMessage.length() > 1999){
    	    errorMessage = errorMessage.substring(0, 1999);
    	}

    	RequestError re = new RequestError();

    	if(exceptionType.name().equals("PolicyException")){

    		PolicyException pe = new PolicyException();
    		pe.setMessageId(messageId);
    		pe.setText(text);
    		if(variables != null){
    			for(String variable: variables){
    				pe.getVariables().add(variable);
    			}
    		}
    		re.setPolicyException(pe);

    	} else {

    		ServiceException se = new ServiceException();
    		se.setMessageId(messageId);
    		se.setText(text);
    		if(variables != null){
        		if(variables != null){
        			for(String variable: variables){
        				se.getVariables().add(variable);
        			}
        		}
    		}
    		re.setServiceException(se);
     	}

        String requestErrorStr = null;

        try{
        	ObjectMapper mapper = new ObjectMapper();
        	mapper.setSerializationInclusion(Include.NON_DEFAULT);
        	requestErrorStr = mapper.writeValueAsString(re);
        }catch(Exception e){
        	msoLogger.error (MessageEnum.APIH_VALIDATION_ERROR, "", "", MsoLogger.ErrorCode.DataError, "Exception in buildServiceErrorResponse writing exceptionType to string ", e);
        }


        return Response.status (httpResponseCode).entity(requestErrorStr).build ();

    }
    
	private static boolean empty(String s) {
		return (s == null || s.trim().isEmpty());
	}
	
    public String getRequestId () {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
    	this.requestId = requestId;
    }

	public void updateFinalStatus(Status failed) {
		try {
			(RequestsDatabase.getInstance()).updateInfraFinalStatus (requestId,
																	status.toString (),
																	this.errorMessage,
																	this.progress,
																	this.responseBody,
																	Constants.MODIFIED_BY_APIHANDLER);
		} catch (Exception e) {
			msoLogger.error(MessageEnum.APIH_DB_UPDATE_EXC, e.getMessage(), "", "", MsoLogger.ErrorCode.DataError, "Exception when updating record in DB");
			msoLogger.debug ("Exception: ", e);
		}
	}
	
	public void setStatus (RequestStatusType status) {
        this.status = status;
        switch (status) {
        case FAILED:
        case COMPLETE:
        	this.progress = Constants.PROGRESS_REQUEST_COMPLETED;
        	break;
        case IN_PROGRESS:
        	this.progress = Constants.PROGRESS_REQUEST_IN_PROGRESS;
        	break;
        }
    }

	public String getOperationalEnvironmentId() {
		return operationalEnvironmentId;
	}

	public void setOperationalEnvironmentId(String operationalEnvironmentId) {
		this.operationalEnvironmentId = operationalEnvironmentId;
	}
}
