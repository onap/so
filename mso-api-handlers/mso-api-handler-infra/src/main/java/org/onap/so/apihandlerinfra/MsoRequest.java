/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.apihandlerinfra;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.onap.so.apihandler.common.ResponseBuilder;
import org.onap.so.apihandlerinfra.tasksbeans.TasksRequest;
import org.onap.so.apihandlerinfra.validation.ApplyUpdatedConfigValidation;
import org.onap.so.apihandlerinfra.validation.CloudConfigurationValidation;
import org.onap.so.apihandlerinfra.validation.ConfigurationParametersValidation;
import org.onap.so.apihandlerinfra.validation.InPlaceSoftwareUpdateValidation;
import org.onap.so.apihandlerinfra.validation.InstanceIdMapValidation;
import org.onap.so.apihandlerinfra.validation.ModelInfoValidation;
import org.onap.so.apihandlerinfra.validation.PlatformLOBValidation;
import org.onap.so.apihandlerinfra.validation.ProjectOwningEntityValidation;
import org.onap.so.apihandlerinfra.validation.RelatedInstancesValidation;
import org.onap.so.apihandlerinfra.validation.RequestInfoValidation;
import org.onap.so.apihandlerinfra.validation.RequestParametersValidation;
import org.onap.so.apihandlerinfra.validation.RequestScopeValidation;
import org.onap.so.apihandlerinfra.validation.SubscriberInfoValidation;
import org.onap.so.apihandlerinfra.validation.UserParamsValidation;
import org.onap.so.apihandlerinfra.validation.ValidationInformation;
import org.onap.so.apihandlerinfra.validation.ValidationRule;
import org.onap.so.apihandlerinfra.vnfbeans.RequestStatusType;
import org.onap.so.apihandlerinfra.vnfbeans.VnfInputs;
import org.onap.so.apihandlerinfra.vnfbeans.VnfRequest;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.db.request.data.repository.InfraActiveRequestsRepository;
import org.onap.so.exceptions.ValidationException;
import org.onap.so.logger.MessageEnum;
import org.onap.so.logger.MsoLogger;
import org.onap.so.serviceinstancebeans.CloudConfiguration;
import org.onap.so.serviceinstancebeans.InstanceDirection;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.ModelType;
import org.onap.so.serviceinstancebeans.PolicyException;
import org.onap.so.serviceinstancebeans.RelatedInstance;
import org.onap.so.serviceinstancebeans.RelatedInstanceList;
import org.onap.so.serviceinstancebeans.RequestError;
import org.onap.so.serviceinstancebeans.RequestInfo;
import org.onap.so.serviceinstancebeans.RequestParameters;
import org.onap.so.serviceinstancebeans.Service;
import org.onap.so.serviceinstancebeans.ServiceException;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


@Component
public class MsoRequest {
      
	@Autowired
	private RequestsDbClient requestsDbClient;
	
	@Autowired
	private ResponseBuilder builder;
    
    private static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.APIH,MsoRequest.class);
    
    public Response buildServiceErrorResponse (int httpResponseCode, MsoException exceptionType, 
    		String errorText, String messageId, List<String> variables, String version) {
    	
    	if(errorText.length() > 1999){
    		errorText = errorText.substring(0, 1999);
    	}

    	RequestError re = new RequestError();

    	if("PolicyException".equals(exceptionType.name())){

    		PolicyException pe = new PolicyException();
    		pe.setMessageId(messageId);
    		pe.setText(errorText);
    		if(variables != null){
    			for(String variable: variables){
    				pe.getVariables().add(variable);
    			}
    		}
    		re.setPolicyException(pe);

    	} else {

    		ServiceException se = new ServiceException();
    		se.setMessageId(messageId);
    		se.setText(errorText);
    		if(variables != null){
        			for(String variable: variables){
        				se.getVariables().add(variable);
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

        return builder.buildResponse(httpResponseCode, null, requestErrorStr, version);
    }

   

    // Parse request JSON
    public void parse (ServiceInstancesRequest sir, HashMap<String,String> instanceIdMap, Actions action, String version,
    		String originalRequestJSON, int reqVersion, Boolean aLaCarteFlag) throws ValidationException, IOException {
    	
        msoLogger.debug ("Validating the Service Instance request");       
        List<ValidationRule> rules = new ArrayList<>();
        msoLogger.debug ("Incoming version is: " + version + " coverting to int: " + reqVersion);
	    RequestParameters requestParameters = sir.getRequestDetails().getRequestParameters();
        ValidationInformation info = new ValidationInformation(sir, instanceIdMap, action,
        		reqVersion, aLaCarteFlag, requestParameters);
        
        rules.add(new InstanceIdMapValidation());
        
        if(reqVersion >= 6 && action == Action.inPlaceSoftwareUpdate){
        	rules.add(new InPlaceSoftwareUpdateValidation());
        }else if(reqVersion >= 6 && action == Action.applyUpdatedConfig){
        	rules.add(new ApplyUpdatedConfigValidation());
        }else{
	        rules.add(new RequestScopeValidation());
	        rules.add(new RequestParametersValidation());
	        rules.add(new RequestInfoValidation());
	        rules.add(new ModelInfoValidation());
	        rules.add(new CloudConfigurationValidation());
	        rules.add(new SubscriberInfoValidation());
	        rules.add(new PlatformLOBValidation());
	        rules.add(new ProjectOwningEntityValidation());
	        rules.add(new RelatedInstancesValidation());
	        rules.add(new ConfigurationParametersValidation());
        } 
	    if(reqVersion >= 7 && requestParameters != null && requestParameters.getUserParams() != null){
	    	for(Map<String, Object> params : requestParameters.getUserParams()){
        		if(params.containsKey("service")){
        			ObjectMapper obj = new ObjectMapper();
					String input = obj.writeValueAsString(params.get("service"));
					Service validate = obj.readValue(input, Service.class);
					info.setUserParams(validate);
					rules.add(new UserParamsValidation());
					break;
        		}
        	}
	    }
	    for(ValidationRule rule : rules){
        	rule.validate(info);
    	}
    }
    void parseOrchestration (ServiceInstancesRequest sir) throws ValidationException {
        RequestInfo requestInfo = sir.getRequestDetails().getRequestInfo();

        if (requestInfo == null) {
            throw new ValidationException ("requestInfo");
        }

        if (empty (requestInfo.getSource ())) {
        	throw new ValidationException ("source");
        }
        if (empty (requestInfo.getRequestorId ())) {
        	throw new ValidationException ("requestorId");
        }
    }
    public Map<String, List<String>> getOrchestrationFilters (MultivaluedMap<String, String> queryParams) throws ValidationException {

        String queryParam = null;
        Map<String, List<String>> orchestrationFilterParams = new HashMap<>();


        for (Entry<String,List<String>> entry : queryParams.entrySet()) {
            queryParam = entry.getKey();

            try{
          	  if("filter".equalsIgnoreCase(queryParam)){
          		  for(String value : entry.getValue()) {
	          		  StringTokenizer st = new StringTokenizer(value, ":");
	
	          		  int counter=0;
	          		  String mapKey=null;
	          		  List<String> orchestrationList = new ArrayList<>();
	          		  while (st.hasMoreElements()) {
	          			  if(counter == 0){
	          				  mapKey = st.nextElement() + "";
	          			  } else{
	          				  orchestrationList.add(st.nextElement() + "");
	          			  }
	          			 counter++;
	        		  }
	          		  orchestrationFilterParams.put(mapKey, orchestrationList);
          		  }
          	  }

            }catch(Exception e){
                //msoLogger.error (MessageEnum.APIH_VALIDATION_ERROR, e);
                throw new ValidationException ("QueryParam ServiceInfo", e);

        	}

        }


        return orchestrationFilterParams;
  }

    public InfraActiveRequests createRequestObject (ServiceInstancesRequest servInsReq, Actions action, String requestId,
    		 Status status, String originalRequestJSON, String requestScope) {
    	InfraActiveRequests aq = new InfraActiveRequests ();
        try {
            if (null == servInsReq) {
            	servInsReq = new ServiceInstancesRequest ();
            }
           
            String networkType = "";
            String vnfType = "";
            aq.setRequestId (requestId);
            aq.setRequestAction(action.toString());
            aq.setAction(action.toString());

            Timestamp startTimeStamp = new Timestamp (System.currentTimeMillis());

            aq.setStartTime (startTimeStamp);
            RequestInfo requestInfo =servInsReq.getRequestDetails().getRequestInfo();
            if (requestInfo != null) {
            	
            	if(requestInfo.getSource() != null){
            		aq.setSource(requestInfo.getSource());
            	}
            	if(requestInfo.getCallbackUrl() != null){
            		aq.setCallBackUrl(requestInfo.getCallbackUrl());
            	}
            	if(requestInfo.getCorrelator() != null){
            		aq.setCorrelator(requestInfo.getCorrelator());
            	}

            	if(requestInfo.getRequestorId() != null) {
            		aq.setRequestorId(requestInfo.getRequestorId());
            	}
            }

            if (servInsReq.getRequestDetails().getModelInfo() != null  ||  (action == Action.inPlaceSoftwareUpdate || action == Action.applyUpdatedConfig)) {
            	aq.setRequestScope(requestScope);
            }

            if (servInsReq.getRequestDetails().getCloudConfiguration() != null) {
            	CloudConfiguration cloudConfiguration = servInsReq.getRequestDetails().getCloudConfiguration();
            	if(cloudConfiguration.getLcpCloudRegionId() != null) {
            		aq.setAicCloudRegion(cloudConfiguration.getLcpCloudRegionId());
            	}

               	if(cloudConfiguration.getTenantId() != null) {
            		aq.setTenantId(cloudConfiguration.getTenantId());
            	}

            }

            if(servInsReq.getServiceInstanceId() != null){
            	aq.setServiceInstanceId(servInsReq.getServiceInstanceId());
            }

            if(servInsReq.getVnfInstanceId() != null){
            	aq.setVnfId(servInsReq.getVnfInstanceId());
            }

            if(ModelType.service.name().equalsIgnoreCase(requestScope)){
              	if(servInsReq.getRequestDetails().getRequestInfo().getInstanceName() != null){
            		aq.setServiceInstanceName(requestInfo.getInstanceName());
            	}
            }

            if(ModelType.network.name().equalsIgnoreCase(requestScope)){
            	aq.setNetworkName(servInsReq.getRequestDetails().getRequestInfo().getInstanceName());
            	aq.setNetworkType(networkType);
            	aq.setNetworkId(servInsReq.getNetworkInstanceId());
            }

            if(ModelType.volumeGroup.name().equalsIgnoreCase(requestScope)){
            	aq.setVolumeGroupId(servInsReq.getVolumeGroupInstanceId());
            	aq.setVolumeGroupName(servInsReq.getRequestDetails().getRequestInfo().getInstanceName());
              	aq.setVnfType(vnfType);

            }

            if(ModelType.vfModule.name().equalsIgnoreCase(requestScope)){
             	aq.setVfModuleName(requestInfo.getInstanceName());
             	aq.setVfModuleModelName(servInsReq.getRequestDetails().getModelInfo().getModelName());
             	aq.setVfModuleId(servInsReq.getVfModuleInstanceId());
             	aq.setVolumeGroupId(servInsReq.getVolumeGroupInstanceId());
              	aq.setVnfType(vnfType);

            }
            
            if(ModelType.configuration.name().equalsIgnoreCase(requestScope)) {
            	aq.setConfigurationId(servInsReq.getConfigurationId());
            	aq.setConfigurationName(requestInfo.getInstanceName());
            }

            if(ModelType.vnf.name().equalsIgnoreCase(requestScope)){
              	aq.setVnfName(requestInfo.getInstanceName());
				if (null != servInsReq.getRequestDetails()) {
					RelatedInstanceList[] instanceList = servInsReq.getRequestDetails().getRelatedInstanceList();

					if (instanceList != null) {

						for(RelatedInstanceList relatedInstanceList : instanceList){

							RelatedInstance relatedInstance = relatedInstanceList.getRelatedInstance();
							if(relatedInstance.getModelInfo().getModelType().equals(ModelType.service)){
								aq.setVnfType(vnfType);
							}
						}
					}
				}
            }

            aq.setRequestBody (originalRequestJSON);

            aq.setRequestStatus (status.toString ());
            aq.setLastModifiedBy (Constants.MODIFIED_BY_APIHANDLER);           
        } catch (Exception e) {
        	msoLogger.error (MessageEnum.APIH_DB_INSERT_EXC, "", "", MsoLogger.ErrorCode.DataError, "Exception when creation record request", e);
        
            if (!status.equals (Status.FAILED)) {
                throw e;
            }
        }
        return aq;
    }
    
    public InfraActiveRequests createRequestObject (TasksRequest taskRequest, Action action, String requestId,
   		 Status status, String originalRequestJSON) {
    	InfraActiveRequests aq = new InfraActiveRequests ();
       try {
        
    	   org.onap.so.apihandlerinfra.tasksbeans.RequestInfo requestInfo = taskRequest.getRequestDetails().getRequestInfo();
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
           }  

           aq.setRequestBody (originalRequestJSON);
           aq.setRequestStatus (status.toString ());
           aq.setLastModifiedBy (Constants.MODIFIED_BY_APIHANDLER);
                  
       } catch (Exception e) {
       	msoLogger.error (MessageEnum.APIH_DB_INSERT_EXC, "", "", MsoLogger.ErrorCode.DataError, "Exception when creation record request", e);
       
           if (!status.equals (Status.FAILED)) {
               throw e;
           }
       }
       return aq;
   }
    
    public void createErrorRequestRecord (Status status, String requestId, String errorMessage, Actions action, String requestScope, String requestJSON) {
        try {
            InfraActiveRequests request = new InfraActiveRequests(requestId);
            Timestamp startTimeStamp = new Timestamp (System.currentTimeMillis());
            request.setStartTime (startTimeStamp);
            request.setRequestStatus(status.toString());
            request.setStatusMessage(errorMessage);
            request.setProgress((long) 100);
            request.setLastModifiedBy(Constants.MODIFIED_BY_APIHANDLER);
            request.setRequestAction(action.toString());
            request.setRequestScope(requestScope);
            request.setRequestBody(requestJSON);
            Timestamp endTimeStamp = new Timestamp(System.currentTimeMillis());
            request.setEndTime(endTimeStamp);
			requestsDbClient.save(request);
        } catch (Exception e) {
        	msoLogger.error(MessageEnum.APIH_DB_UPDATE_EXC, e.getMessage(), "", "", MsoLogger.ErrorCode.DataError, "Exception when updating record in DB");
            msoLogger.debug ("Exception: ", e);
        }
    }
    
    


    public Response buildResponse (int httpResponseCode, String errorCode, InfraActiveRequests inProgress) {
        return buildResponseWithError (httpResponseCode, errorCode, inProgress, null);
    }

    public Response buildResponseWithError (int httpResponseCode,
                                            String errorCode,
                                            InfraActiveRequests inProgress,
                                            String errorString) {



        // Log the failed request into the MSO Requests database

        return Response.status (httpResponseCode).entity (null).build ();

    }

    public Response buildResponseFailedValidation (int httpResponseCode, String exceptionMessage) {

        return Response.status (httpResponseCode).entity (null).build ();
    }
    
  

    public String getServiceType (VnfInputs vnfInputs) {
    	if (vnfInputs.getServiceType () != null)
    		return vnfInputs.getServiceType ();
    	if (vnfInputs.getServiceId () != null)
    		return vnfInputs.getServiceId ();
    	return null;
    }

    public long translateStatus (RequestStatusType status) {        
        switch (status) {
        case FAILED:
        case COMPLETE:
        	return Constants.PROGRESS_REQUEST_COMPLETED;        	
        case IN_PROGRESS:
        	return Constants.PROGRESS_REQUEST_IN_PROGRESS;
		default:
			return 0;        	
        }
    }

    public static String domToStr (Document doc) {
        if (doc == null) {
            return null;
        }

        try {
            StringWriter sw = new StringWriter ();
            StreamResult sr = new StreamResult (sw);
            TransformerFactory tf = TransformerFactory.newInstance ();
			tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET,"");
            Transformer t = tf.newTransformer ();
            t.setOutputProperty (OutputKeys.STANDALONE, "yes");
            NodeList nl = doc.getDocumentElement ().getChildNodes ();
            DOMSource source = null;
            for (int x = 0; x < nl.getLength (); x++) {
                Node e = nl.item (x);
                if (e instanceof Element) {
                    source = new DOMSource (e);
                    break;
                }
            }
            if (source != null) {
                t.transform (source, sr);

                String s = sw.toString ();
                return s;
            }

            return null;

        } catch (Exception e) {
            msoLogger.error (MessageEnum.APIH_DOM2STR_ERROR, "", "", MsoLogger.ErrorCode.DataError, "Exception in domToStr", e);
        }
        return null;
    }

    public void addBPMNSpecificInputs(VnfRequest vnfReq, VnfInputs vnfInputs, String personaModelId, String personaModelVersion, Boolean isBaseVfModule,
    			String vnfPersonaModelId, String vnfPersonaModelVersion) {
    	vnfInputs.setPersonaModelId(personaModelId);
    	vnfInputs.setPersonaModelVersion(personaModelVersion);
    	vnfInputs.setIsBaseVfModule(isBaseVfModule);
    	vnfInputs.setVnfPersonaModelId(vnfPersonaModelId);
    	vnfInputs.setVnfPersonaModelVersion(vnfPersonaModelVersion);

    	vnfReq.setVnfInputs(vnfInputs);
      
    }

    private static boolean empty(String s) {
    	  return (s == null || s.trim().isEmpty());
    }

    public String getRequestJSON(ServiceInstancesRequest sir) throws JsonGenerationException, JsonMappingException, IOException {
    	ObjectMapper mapper = new ObjectMapper();
    	mapper.setSerializationInclusion(Include.NON_NULL);
    	//mapper.configure(Feature.WRAP_ROOT_VALUE, true);
    	msoLogger.debug ("building sir from object " + sir);
    	String requestJSON = mapper.writeValueAsString(sir);
    	
    	// Perform mapping from VID-style modelInfo fields to ASDC-style modelInfo fields
    	
    	msoLogger.debug("REQUEST JSON before mapping: " + requestJSON);
    	// modelUuid = modelVersionId
    	requestJSON = requestJSON.replaceAll("\"modelVersionId\":","\"modelUuid\":");
    	// modelCustomizationUuid = modelCustomizationId
    	requestJSON = requestJSON.replaceAll("\"modelCustomizationId\":","\"modelCustomizationUuid\":");
    	// modelInstanceName = modelCustomizationName
    	requestJSON = requestJSON.replaceAll("\"modelCustomizationName\":","\"modelInstanceName\":");
    	// modelInvariantUuid = modelInvariantId 
    	requestJSON = requestJSON.replaceAll("\"modelInvariantId\":","\"modelInvariantUuid\":");    	
    	msoLogger.debug("REQUEST JSON after mapping: " + requestJSON);
    	
    	return requestJSON;
    }


	public boolean getAlacarteFlag(ServiceInstancesRequest sir) {
		if(sir.getRequestDetails().getRequestParameters() != null &&
				sir.getRequestDetails().getRequestParameters().getALaCarte() != null)
			return sir.getRequestDetails().getRequestParameters().getALaCarte();
		
		return false;
	}


	public String getNetworkType(ServiceInstancesRequest sir, String requestScope) {
		  if(requestScope.equalsIgnoreCase(ModelType.network.name()))
		        return sir.getRequestDetails().getModelInfo().getModelName();	
		  else return null;
	}


	public String getServiceInstanceType(ServiceInstancesRequest sir, String requestScope) {
		 if(requestScope.equalsIgnoreCase(ModelType.network.name()))
		        return sir.getRequestDetails().getModelInfo().getModelName();	
		  else return null;		
	}


	public String getSDCServiceModelVersion(ServiceInstancesRequest sir) {
		String sdcServiceModelVersion = null;
		if(sir.getRequestDetails().getRelatedInstanceList() != null)
			for(RelatedInstanceList relatedInstanceList : sir.getRequestDetails().getRelatedInstanceList()){
				RelatedInstance relatedInstance = relatedInstanceList.getRelatedInstance();
				ModelInfo relatedInstanceModelInfo = relatedInstance.getModelInfo ();	
				if(relatedInstanceModelInfo.getModelType().equals(ModelType.service))          		          		
					sdcServiceModelVersion = relatedInstanceModelInfo.getModelVersion ();
			}
        return sdcServiceModelVersion;
	}


	public String getVfModuleType(ServiceInstancesRequest sir, String requestScope, Actions action, int reqVersion) {	
	
      	String serviceInstanceType = null;
      	String networkType = null;
      	String vnfType = null;
      	String vfModuleType = null;
      	String vfModuleModelName = null;
		ModelInfo modelInfo = sir.getRequestDetails().getModelInfo();
		RelatedInstanceList[] instanceList = sir.getRequestDetails().getRelatedInstanceList();
		String serviceModelName = null;
        String vnfModelName = null;
        String asdcServiceModelVersion = null;
        String volumeGroupId = null;
        boolean isRelatedServiceInstancePresent = false;
        boolean isRelatedVnfInstancePresent = false;
    	boolean isSourceVnfPresent = false;
      	boolean isDestinationVnfPresent = false;
      	boolean isConnectionPointPresent = false;	

	    if (instanceList != null) {
	       	for(RelatedInstanceList relatedInstanceList : instanceList){
	        	RelatedInstance relatedInstance = relatedInstanceList.getRelatedInstance();
	        	ModelInfo relatedInstanceModelInfo = relatedInstance.getModelInfo ();	

	          	if (action != Action.deleteInstance) {
		          	
		          	if(ModelType.configuration.name().equalsIgnoreCase(requestScope)) {
		          		if(InstanceDirection.source.equals(relatedInstance.getInstanceDirection()) && relatedInstanceModelInfo.getModelType().equals(ModelType.vnf)) {
		          			isSourceVnfPresent = true;
		          		} else if(InstanceDirection.destination.equals(relatedInstance.getInstanceDirection()) && 
		          				(relatedInstanceModelInfo.getModelType().equals(ModelType.vnf) || (relatedInstanceModelInfo.getModelType().equals(ModelType.pnf) && reqVersion == 6))) {
		          			isDestinationVnfPresent = true;
		          		}
		          	}
		          	
		          	if(ModelType.connectionPoint.equals(relatedInstanceModelInfo.getModelType()) && ModelType.configuration.name().equalsIgnoreCase(requestScope)) {
		          		isConnectionPointPresent = true;
		          	}
		        }
	          	

	          	if(relatedInstanceModelInfo.getModelType().equals(ModelType.service)) {
	          		isRelatedServiceInstancePresent = true;	          		
	          		serviceModelName = relatedInstanceModelInfo.getModelName ();
	          		asdcServiceModelVersion = relatedInstanceModelInfo.getModelVersion ();
	          	} else if(relatedInstanceModelInfo.getModelType().equals(ModelType.vnf) && !(ModelType.configuration.name().equalsIgnoreCase(requestScope))) {
	          		isRelatedVnfInstancePresent = true;	          		
	          		vnfModelName = relatedInstanceModelInfo.getModelCustomizationName();
	          	} else if(relatedInstanceModelInfo.getModelType().equals(ModelType.volumeGroup)) {	          		
	           		volumeGroupId = relatedInstance.getInstanceId ();
	          	}
          	}
	       	
	        if(requestScope.equalsIgnoreCase (ModelType.volumeGroup.name ())) {	        	
	          	serviceInstanceType = serviceModelName;
	          	vnfType = serviceModelName + "/" + vnfModelName;	  
	        }
	        else if(requestScope.equalsIgnoreCase(ModelType.vfModule.name ())) {	     
	        	vfModuleModelName = modelInfo.getModelName ();
	          	serviceInstanceType = serviceModelName;
	          	vnfType = serviceModelName + "/" + vnfModelName;
	          	vfModuleType = vnfType + "::" + vfModuleModelName;
	          	sir.setVolumeGroupInstanceId (volumeGroupId);	         
	        }
	        else if (requestScope.equalsIgnoreCase (ModelType.vnf.name ()))
	        	vnfType = serviceModelName + "/" + sir.getRequestDetails().getModelInfo().getModelCustomizationName();	        	
	       
        }     
    	
		return vfModuleType;

	}
	
	public String getVnfType(ServiceInstancesRequest sir, String requestScope, Actions action, int reqVersion) {	
		
      	String serviceInstanceType = null;
      	String networkType = null;
      	String vnfType = null;
      	String vfModuleType = null;
      	String vfModuleModelName = null;
		ModelInfo modelInfo = sir.getRequestDetails().getModelInfo();
	    MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.APIH, MsoRequest.class);
		RelatedInstanceList[] instanceList = sir.getRequestDetails().getRelatedInstanceList();
		String serviceModelName = null;
        String vnfModelName = null;
        String asdcServiceModelVersion = null;
        String volumeGroupId = null;
        boolean isRelatedServiceInstancePresent = false;
        boolean isRelatedVnfInstancePresent = false;
    	boolean isSourceVnfPresent = false;
      	boolean isDestinationVnfPresent = false;
      	boolean isConnectionPointPresent = false;	

	    if (instanceList != null) {
	       	for(RelatedInstanceList relatedInstanceList : instanceList){
	        	RelatedInstance relatedInstance = relatedInstanceList.getRelatedInstance();
	        	ModelInfo relatedInstanceModelInfo = relatedInstance.getModelInfo ();	

	          	if (action != Action.deleteInstance) {
		          	
		          	if(ModelType.configuration.name().equalsIgnoreCase(requestScope)) {
		          		if(InstanceDirection.source.equals(relatedInstance.getInstanceDirection()) && relatedInstanceModelInfo.getModelType().equals(ModelType.vnf)) {
		          			isSourceVnfPresent = true;
		          		} else if(InstanceDirection.destination.equals(relatedInstance.getInstanceDirection()) && 
		          				(relatedInstanceModelInfo.getModelType().equals(ModelType.vnf) || (relatedInstanceModelInfo.getModelType().equals(ModelType.pnf) && reqVersion == 6))) {
		          			isDestinationVnfPresent = true;
		          		}
		          	}
		          	
		          	if(ModelType.connectionPoint.equals(relatedInstanceModelInfo.getModelType()) && ModelType.configuration.name().equalsIgnoreCase(requestScope)) {
		          		isConnectionPointPresent = true;
		          	}
		        }
	          	

	          	if(relatedInstanceModelInfo.getModelType().equals(ModelType.service)) {
	          		isRelatedServiceInstancePresent = true;	          		
	          		serviceModelName = relatedInstanceModelInfo.getModelName ();
	          		asdcServiceModelVersion = relatedInstanceModelInfo.getModelVersion ();
	          	} else if(relatedInstanceModelInfo.getModelType().equals(ModelType.vnf) && !(ModelType.configuration.name().equalsIgnoreCase(requestScope))) {
	          		isRelatedVnfInstancePresent = true;	          		
	          		vnfModelName = relatedInstanceModelInfo.getModelCustomizationName();
	          	} else if(relatedInstanceModelInfo.getModelType().equals(ModelType.volumeGroup)) {	          		
	           		volumeGroupId = relatedInstance.getInstanceId ();
	          	}
          	}
	       	
	        if(requestScope.equalsIgnoreCase (ModelType.volumeGroup.name ())) {	        	
	          	serviceInstanceType = serviceModelName;
	          	vnfType = serviceModelName + "/" + vnfModelName;	  
	        }
	        else if(requestScope.equalsIgnoreCase(ModelType.vfModule.name ())) {	     
	        	vfModuleModelName = modelInfo.getModelName ();
	          	serviceInstanceType = serviceModelName;
	          	vnfType = serviceModelName + "/" + vnfModelName;
	          	vfModuleType = vnfType + "::" + vfModuleModelName;
	          	sir.setVolumeGroupInstanceId (volumeGroupId);	         
	        }
	        else if (requestScope.equalsIgnoreCase (ModelType.vnf.name ()))
	        	vnfType = serviceModelName + "/" + sir.getRequestDetails().getModelInfo().getModelCustomizationName();	        	
	       
        }     
    	
		return vnfType;

	}
}
