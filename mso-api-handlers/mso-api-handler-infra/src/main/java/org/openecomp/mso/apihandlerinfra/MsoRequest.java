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

package org.openecomp.mso.apihandlerinfra;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.hibernate.Session;
import org.openecomp.mso.apihandler.common.ValidationException;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.CloudConfiguration;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.ModelInfo;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.PolicyException;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.RelatedInstance;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.RelatedInstanceList;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.RequestError;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.RequestInfo;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.RequestParameters;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.ServiceException;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.ServiceInstancesRequest;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.SubscriberInfo;
import org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType;
import org.openecomp.mso.apihandlerinfra.vnfbeans.VnfInputs;
import org.openecomp.mso.apihandlerinfra.vnfbeans.VnfRequest;
import org.openecomp.mso.db.AbstractSessionFactoryManager;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.requestsdb.InfraActiveRequests;
import org.openecomp.mso.requestsdb.RequestsDatabase;
import org.openecomp.mso.requestsdb.RequestsDbSessionFactoryManager;
import org.openecomp.mso.utils.UUIDChecker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

public class MsoRequest {

    private String requestId;
    private String requestXML;
    private String requestJSON;
    private String requestUri;
    private VnfRequest vnfReq;
    private RequestInfo requestInfo;
    private ModelInfo modelInfo;
    private CloudConfiguration cloudConfiguration ;
    private VnfInputs vnfInputs;
    private String vnfParams;
    private Action action;
    private String errorMessage;
    private String errorCode;
    private String httpResponse;
    private String responseBody;
    private RequestStatusType status;
    private ServiceInstancesRequest sir;
    private long startTime;
    private long progress = Constants.PROGRESS_REQUEST_RECEIVED;
    private String serviceInstanceType;
    private String vnfType;
    private String vfModuleType;
    private String vfModuleModelName;
    private String networkType;
    private String asdcServiceModelVersion;
    private String requestScope;
    private int reqVersion;
    private boolean aLaCarteFlag = false;

    private static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.APIH);
    private static final String NOT_PROVIDED = "not provided";

    protected AbstractSessionFactoryManager requestsDbSessionFactoryManager = new RequestsDbSessionFactoryManager ();

    MsoRequest (String requestId) {
        this.requestId = requestId;
        this.startTime = System.currentTimeMillis();
        MsoLogger.setLogContext (requestId, null);

    }

    MsoRequest () {

        this.startTime = System.currentTimeMillis();
        MsoLogger.setLogContext (requestId, null);

    }


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

    	if("PolicyException".equals(exceptionType.name())){

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
				for(String variable: variables){
					se.getVariables().add(variable);
				}
    		}
    		re.setServiceException(se);
     	}

        String requestErrorStr = null;

        try{
        	ObjectMapper mapper = new ObjectMapper();
        	mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_DEFAULT);
        	requestErrorStr = mapper.writeValueAsString(re);
        }catch(Exception e){
        	msoLogger.error (MessageEnum.APIH_VALIDATION_ERROR, "", "", MsoLogger.ErrorCode.DataError, "Exception in buildServiceErrorResponse writing exceptionType to string ", e);
        }


        return Response.status (httpResponseCode).entity(requestErrorStr).build ();

    }

    private int reqVersionToInt(String version){
    	if(version!=null){
    		return Integer.parseInt(version.substring(1));
    	}else{
    		return 0;
    	}
    }

    // Parse request JSON
    void parse (ServiceInstancesRequest sir, HashMap<String,String> instanceIdMap, Action action, String version) throws ValidationException {

        msoLogger.debug ("Validating the Service Instance request");

        this.sir = sir;
        this.action = action;
        this.reqVersion = reqVersionToInt(version);
        msoLogger.debug ("Incoming version is: " + version + " coverting to int: " + this.reqVersion);


        try{
        	ObjectMapper mapper = new ObjectMapper();
        	//mapper.configure(Feature.WRAP_ROOT_VALUE, true);
        	requestJSON = mapper.writeValueAsString(sir.getRequestDetails());

        } catch(Exception e){
        	throw new ValidationException ("Parse ServiceInstanceRequest to JSON string",e);
        }

        if(instanceIdMap != null){
        	if(instanceIdMap.get("serviceInstanceId") != null){
        		if (!UUIDChecker.isValidUUID (instanceIdMap.get ("serviceInstanceId"))) {
        			throw new ValidationException ("serviceInstanceId");
        		}
        		this.sir.setServiceInstanceId(instanceIdMap.get("serviceInstanceId"));
        	}

        	if(instanceIdMap.get("vnfInstanceId") != null){
        		if (!UUIDChecker.isValidUUID (instanceIdMap.get ("vnfInstanceId"))) {
        			throw new ValidationException ("vnfInstanceId");
        		}
        		this.sir.setVnfInstanceId(instanceIdMap.get("vnfInstanceId"));
        	}

        	if(instanceIdMap.get("vfModuleInstanceId") != null){
        		if (!UUIDChecker.isValidUUID (instanceIdMap.get ("vfModuleInstanceId"))) {
        			throw new ValidationException ("vfModuleInstanceId");
        		}
        		this.sir.setVfModuleInstanceId(instanceIdMap.get("vfModuleInstanceId"));
        	}

        	if(instanceIdMap.get("volumeGroupInstanceId") != null){
        		if (!UUIDChecker.isValidUUID (instanceIdMap.get ("volumeGroupInstanceId"))) {
        			throw new ValidationException ("volumeGroupInstanceId");
        		}
        		this.sir.setVolumeGroupInstanceId(instanceIdMap.get("volumeGroupInstanceId"));
        	}

        	if(instanceIdMap.get("networkInstanceId") != null){
        		if (!UUIDChecker.isValidUUID (instanceIdMap.get ("networkInstanceId"))) {
        			throw new ValidationException ("networkInstanceId");
        		}
        		this.sir.setNetworkInstanceId(instanceIdMap.get("networkInstanceId"));
        	}
        }

        RequestParameters requestParameters = sir.getRequestDetails().getRequestParameters();
		if (this.reqVersion >= 3) {
			this.aLaCarteFlag =
				requestParameters != null && sir.getRequestDetails().getRequestParameters().isaLaCarte();
		} else {
			this.aLaCarteFlag = true;
		}

		if(requestParameters != null && (reqVersion < 3) && requestParameters.getAutoBuildVfModules()){
    		throw new ValidationException("AutoBuildVfModule", version);
        }

        this.modelInfo = sir.getRequestDetails().getModelInfo();

        if (this.modelInfo == null) {
            throw new ValidationException ("model-info");
        }

        this.requestInfo = sir.getRequestDetails().getRequestInfo();

        if (this.requestInfo == null) {
            throw new ValidationException ("requestInfo");
        }

        if (modelInfo.getModelType () == null) {
        	throw new ValidationException ("modelType");
        }

        this.requestScope = modelInfo.getModelType().name();

        // modelCustomizationId is required when usePreLoad is false for v4 and higher for VF Module Create
        if(requestParameters != null && reqVersion > 3 && requestScope.equalsIgnoreCase(ModelType.vfModule.name()) && action == Action.createInstance && !requestParameters.isUsePreload()) {
        	if(!UUIDChecker.isValidUUID(modelInfo.getModelCustomizationId())) {
        		throw new ValidationException("modelCustomizationId");
        	}
        }
        
        // modelCustomizationId is required when usePreLoad is false for v5 and higher for VF Module Replace
        if(requestParameters != null && reqVersion > 4 && requestScope.equalsIgnoreCase(ModelType.vfModule.name()) && action == Action.replaceInstance && !requestParameters.isUsePreload()) {
        	if(!UUIDChecker.isValidUUID(modelInfo.getModelCustomizationId())) {
        		throw new ValidationException("modelCustomizationId");
        	}
        }
        
        // modelCustomizationId or modelCustomizationName are required when usePreLoad is false for v5 and higher for VNF Replace
        if(requestParameters != null && reqVersion > 4 && requestScope.equalsIgnoreCase(ModelType.vnf.name()) && action == Action.replaceInstance && !requestParameters.isUsePreload()) {
        	if(!UUIDChecker.isValidUUID(modelInfo.getModelCustomizationId()) && modelInfo.getModelCustomizationName() == null) {
        		throw new ValidationException("modelCustomizationId or modelCustomizationName");
        	}
        }

        //is required for serviceInstance delete macro when aLaCarte=false (v3)
        //create and updates except for network (except v4)
        if (empty (modelInfo.getModelInvariantId ()) && ((this.reqVersion >2 && !this.aLaCarteFlag && requestScope.equalsIgnoreCase(ModelType.service.name()) && action == Action.deleteInstance) ||
                !(this.reqVersion < 4 && requestScope.equalsIgnoreCase (ModelType.network.name ())) && (action == Action.createInstance || action == Action.updateInstance))) {
        	throw new ValidationException ("modelInvariantId");
        }

        if (!empty (modelInfo.getModelInvariantId ()) && !UUIDChecker.isValidUUID (modelInfo.getModelInvariantId ())) {
        	throw new ValidationException ("modelInvariantId format");
        }

        if (this.reqVersion <= 2 && empty (modelInfo.getModelName ()) && (action == Action.createInstance || action == Action.updateInstance || (action == Action.deleteInstance &&
        		(requestScope.equalsIgnoreCase (ModelType.network.name ()) || requestScope.equalsIgnoreCase (ModelType.vfModule.name ()))))) {
        	throw new ValidationException ("modelName");
        }
        if(this.reqVersion > 2 && empty (modelInfo.getModelName ()) && (action == Action.createInstance || action == Action.updateInstance || (action == Action.deleteInstance &&
        		(requestScope.equalsIgnoreCase (ModelType.vfModule.name ()))))){
        	throw new ValidationException ("modelName");
        }

        if (empty (modelInfo.getModelVersion ()) && ((this.reqVersion == 3 && !this.aLaCarteFlag && requestScope.equalsIgnoreCase(ModelType.service.name()) && action == Action.deleteInstance) || 
        		!(this.reqVersion < 4 && requestScope.equalsIgnoreCase (ModelType.network.name ())) && (action == Action.createInstance || action == Action.updateInstance))) {
        	throw new ValidationException ("modelVersion");
        }

        // modelVersionId doesn't exist in v2, not required field in v3, is required for serviceInstance delete macro when aLaCarte=false in v4
        if (this.reqVersion > 3 && empty (modelInfo.getModelVersionId()) && ((!this.aLaCarteFlag && requestScope.equalsIgnoreCase(ModelType.service.name()) && action == Action.deleteInstance) ||
        		(action == Action.createInstance || action == Action.updateInstance))) {
        	throw new ValidationException ("modelVersionId");
        }
        
        if(requestScope.equalsIgnoreCase(ModelType.vnf.name()) && action != Action.deleteInstance && empty (modelInfo.getModelCustomizationName ())) {
        	if(this.reqVersion<=2){
        		throw new ValidationException ("modelCustomizationName");
        	} else if (!UUIDChecker.isValidUUID (modelInfo.getModelCustomizationId())) {
          		throw new ValidationException ("modelCustomizationId or modelCustomizationName");
          	}
        }

        if(this.reqVersion > 2 && (!UUIDChecker.isValidUUID (modelInfo.getModelCustomizationId())) && requestScope.equalsIgnoreCase (ModelType.network.name ())
        		&& (action == Action.updateInstance || action == Action.createInstance)){
        	throw new ValidationException ("modelCustomizationId");
        }

        if(!empty(modelInfo.getModelNameVersionId())){
        	modelInfo.setModelVersionId(modelInfo.getModelNameVersionId());
        }

        this.cloudConfiguration = sir.getRequestDetails ().getCloudConfiguration ();
        if ( (((!this.aLaCarteFlag && requestScope.equalsIgnoreCase (ModelType.service.name ()) && this.reqVersion < 5) ||
        		(!requestScope.equalsIgnoreCase (ModelType.service.name ())) && action != Action.updateInstance))
        		&& cloudConfiguration == null) {
        	throw new ValidationException ("cloudConfiguration");
        }

        if (cloudConfiguration != null) {
        	if (empty (cloudConfiguration.getLcpCloudRegionId ())) {
        		throw new ValidationException ("lcpCloudRegionId");
        	}
        	if (empty (cloudConfiguration.getTenantId ())) {
        		throw new ValidationException ("tenantId");
        	}
        }


        if (requestScope.equalsIgnoreCase (ModelType.service.name ()) && action == Action.createInstance) {
        	if (requestParameters == null) {
        		throw new ValidationException ("requestParameters");
        	}
        	if (empty (requestParameters.getSubscriptionServiceType ())) {
        		throw new ValidationException ("subscriptionServiceType");
        	}
        }
        
        if (this.reqVersion > 4 && requestScope.equalsIgnoreCase (ModelType.service.name ()) && action == Action.createInstance) {
        	SubscriberInfo subscriberInfo = sir.getRequestDetails ().getSubscriberInfo();
        	if (subscriberInfo == null) {
        		throw new ValidationException ("subscriberInfo");
        	}
        	if (empty (subscriberInfo.getGlobalSubscriberId ())) {
        		throw new ValidationException ("globalSubscriberId");
        	}
        }

        if(requestScope.equalsIgnoreCase(ModelType.service.name())){
        	this.serviceInstanceType = modelInfo.getModelName();
        }

        if(requestScope.equalsIgnoreCase(ModelType.network.name())){
        	this.networkType = modelInfo.getModelName();
        }

        // Verify instanceName existence and format except for macro serviceInstance
        if (this.reqVersion < 3 && requestScope.equalsIgnoreCase (ModelType.service.name ()) && empty (requestInfo.getInstanceName ()) && action == Action.createInstance) {
        	throw new ValidationException ("instanceName");
        }

        if (!empty (requestInfo.getInstanceName ())) {
        	if (!requestInfo.getInstanceName ().matches (Constants.VALID_INSTANCE_NAME_FORMAT)) {
        		throw new ValidationException ("instanceName format");
        	}
        }

        if (empty (requestInfo.getProductFamilyId ()))  {
        	// Mandatory for vnf Create(aLaCarte=true), Network Create(aLaCarte=true) and network update
        	//Mandatory for macro request create service instance
        	if((requestScope.equalsIgnoreCase (ModelType.vnf.name ()) && action == Action.createInstance) || 
        		(requestScope.equalsIgnoreCase (ModelType.network.name ()) && (action == Action.createInstance || action == Action.updateInstance)) ||
        		(this.reqVersion > 3 && !this.aLaCarteFlag && requestScope.equalsIgnoreCase(ModelType.service.name()) && action == Action.createInstance)) {
        	throw new ValidationException ("productFamilyId");
        }
        }
       
        //required for all operations in V4
        if(empty(requestInfo.getRequestorId()) && this.reqVersion > 3) {
        	throw new ValidationException ("requestorId");
        }

        if (empty (requestInfo.getSource ())) {
        	throw new ValidationException ("source");
        }


        RelatedInstanceList[] instanceList = sir.getRequestDetails().getRelatedInstanceList();

        String serviceModelName = null;
        String vnfModelName = null;
        String asdcServiceModelVersion = null;
        String volumeGroupId = null;
        boolean isRelatedServiceInstancePresent = false;
        boolean isRelatedVnfInstancePresent = false;

        if (instanceList != null) {
	       	for(RelatedInstanceList relatedInstanceList : instanceList){
	        	RelatedInstance relatedInstance = relatedInstanceList.getRelatedInstance();

	        	ModelInfo relatedInstanceModelInfo = relatedInstance.getModelInfo ();
				if (relatedInstanceModelInfo == null) {
	          		throw new ValidationException ("modelInfo in relatedInstance");
	          	}

	          	if (relatedInstanceModelInfo.getModelType () == null) {
	          		throw new ValidationException ("modelType in relatedInstance");
	          	}


	        	if (!empty (relatedInstance.getInstanceName ())) {
	            	if (!relatedInstance.getInstanceName ().matches (Constants.VALID_INSTANCE_NAME_FORMAT)) {
	            		throw new ValidationException ("instanceName format in relatedInstance");
	            	}
	            }

	          	if (empty (relatedInstance.getInstanceId ())) {
	          		throw new ValidationException ("instanceId in relatedInstance");
	          	}

	          	if (!UUIDChecker.isValidUUID (relatedInstance.getInstanceId ())) {
	          		throw new ValidationException ("instanceId format in relatedInstance");
	          	}


	          	if (action != Action.deleteInstance) {
	          		if(!relatedInstanceModelInfo.getModelType().equals(ModelType.volumeGroup)) {

	          			if(empty (relatedInstanceModelInfo.getModelInvariantId ())) {
	          			throw new ValidationException ("modelInvariantId in relatedInstance");
	          			} else if(this.reqVersion > 3 && empty(relatedInstanceModelInfo.getModelVersionId ())) {
	          				throw new ValidationException("modelVersionId in relatedInstance");
	          			} else if(empty(relatedInstanceModelInfo.getModelName ())) {
	          				throw new ValidationException ("modelName in relatedInstance");
	          			} else if (empty (relatedInstanceModelInfo.getModelVersion ())) {
	          				throw new ValidationException ("modelVersion in relatedInstance");
	          			}
	          		}

		          	if (!empty (relatedInstanceModelInfo.getModelInvariantId ()) &&
		          			!UUIDChecker.isValidUUID (relatedInstanceModelInfo.getModelInvariantId ())) {
		          		throw new ValidationException ("modelInvariantId format in relatedInstance");
		          	}
		          	}

	          	if (empty (relatedInstanceModelInfo.getModelCustomizationName ()) && relatedInstanceModelInfo.getModelType ().equals (ModelType.vnf) ) {
	          		if(this.reqVersion >=3 && empty (relatedInstanceModelInfo.getModelCustomizationId()) && action != Action.deleteInstance) {
	          			throw new ValidationException ("modelCustomizationName or modelCustomizationId in relatedInstance of vnf");
	          		} else if(this.reqVersion < 3) {
	          			throw new ValidationException ("modelCustomizationName in relatedInstance");
	          	}
	          	}

	          	if(relatedInstanceModelInfo.getModelType().equals(ModelType.service)) {
	          		isRelatedServiceInstancePresent = true;
	          		if (!relatedInstance.getInstanceId ().equals (this.sir.getServiceInstanceId ())) {
	          			throw new ValidationException ("serviceInstanceId matching the serviceInstanceId in request URI");
	          		}
	          		serviceModelName = relatedInstanceModelInfo.getModelName ();
	          		asdcServiceModelVersion = relatedInstanceModelInfo.getModelVersion ();
	          	} else if(relatedInstanceModelInfo.getModelType().equals(ModelType.vnf)) {
	          		isRelatedVnfInstancePresent = true;
	          		if (!relatedInstance.getInstanceId ().equals (this.sir.getVnfInstanceId ())) {
	          			throw new ValidationException ("vnfInstanceId matching the vnfInstanceId in request URI");
	          		}
	          		vnfModelName = relatedInstanceModelInfo.getModelCustomizationName();
	          	} else if(relatedInstanceModelInfo.getModelType().equals(ModelType.volumeGroup)) {	          		
	           		volumeGroupId = relatedInstance.getInstanceId ();
	          	}
          	}


	        if(requestScope.equalsIgnoreCase (ModelType.volumeGroup.name ())) {
	        	if (!isRelatedServiceInstancePresent) {
	        		throw new ValidationException ("related service instance for volumeGroup request");
	        	}
	        	if (!isRelatedVnfInstancePresent) {
	        		throw new ValidationException ("related vnf instance for volumeGroup request");
	        	}
	          	this.serviceInstanceType = serviceModelName;
	          	this.vnfType = serviceModelName + "/" + vnfModelName;
	          	this.asdcServiceModelVersion = asdcServiceModelVersion;
	        }
	        else if(requestScope.equalsIgnoreCase(ModelType.vfModule.name ())) {
	        	if (!isRelatedServiceInstancePresent) {
	        		throw new ValidationException ("related service instance for vfModule request");
	        	}
	        	if (!isRelatedVnfInstancePresent) {
	        		throw new ValidationException ("related vnf instance for vfModule request");
	        	}
	        	String vfModuleModelName = modelInfo.getModelName ();
	         	this.vfModuleModelName = vfModuleModelName;
	          	this.serviceInstanceType = serviceModelName;
	          	this.vnfType = serviceModelName + "/" + vnfModelName;
	          	this.asdcServiceModelVersion = asdcServiceModelVersion;
	          	this.vfModuleType = vnfType + "::" + vfModuleModelName;
	          	this.sir.setVolumeGroupInstanceId (volumeGroupId);
	        }
	        else if (requestScope.equalsIgnoreCase (ModelType.vnf.name ())) {
	        	if (!isRelatedServiceInstancePresent) {
	        		throw new ValidationException ("related service instance for vnf request");
	        	}
	        	this.vnfType = serviceModelName + "/" + sir.getRequestDetails().getModelInfo().getModelCustomizationName();
	       }
        }
        else if ((( requestScope.equalsIgnoreCase(ModelType.vnf.name ()) || requestScope.equalsIgnoreCase(ModelType.volumeGroup.name ()) || requestScope.equalsIgnoreCase(ModelType.vfModule.name ()) ) && (action == Action.createInstance)) ||
        		(this.reqVersion > 2 && (requestScope.equalsIgnoreCase(ModelType.volumeGroup.name ()) || requestScope.equalsIgnoreCase(ModelType.vfModule.name ())) && action == Action.updateInstance)){
        	 msoLogger.debug ("related instance exception");
        	throw new ValidationException ("related instances");
        }

    }

    void parseOrchestration (ServiceInstancesRequest sir) throws ValidationException {

        msoLogger.debug ("Validating the Orchestration request");

        this.sir = sir;

        try{
        	ObjectMapper mapper = new ObjectMapper();
        	//mapper.configure(Feature.WRAP_ROOT_VALUE, true);
        	requestJSON = mapper.writeValueAsString(sir.getRequestDetails());

        } catch(Exception e){
        	throw new ValidationException ("Parse ServiceInstanceRequest to JSON string", e);
        }

        this.requestInfo = sir.getRequestDetails().getRequestInfo();

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

    public void createRequestRecord (Status status, Action action) {

        Session session = null;
        try {

            session = requestsDbSessionFactoryManager.getSessionFactory ().openSession ();
            session.beginTransaction ();

            if (null == sir) {
                sir = new ServiceInstancesRequest ();
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

            if (modelInfo != null) {
            	aq.setRequestScope(requestScope);
            }

            if (cloudConfiguration != null) {
            	if(cloudConfiguration.getLcpCloudRegionId() != null) {
            		aq.setAicCloudRegion(cloudConfiguration.getLcpCloudRegionId());
            	}

               	if(cloudConfiguration.getTenantId() != null) {
            		aq.setTenantId(cloudConfiguration.getTenantId());
            	}

            }

            if(sir.getServiceInstanceId() != null){
            	aq.setServiceInstanceId(sir.getServiceInstanceId());
            }

            if(sir.getVnfInstanceId() != null){
            	aq.setVnfId(sir.getVnfInstanceId());
            }


            if(ModelType.service.name().equalsIgnoreCase(requestScope)){
              	if(requestInfo.getInstanceName() != null){
            		aq.setServiceInstanceName(requestInfo.getInstanceName());
            	}
            }

            if(ModelType.network.name().equalsIgnoreCase(requestScope)){
            	aq.setNetworkName(requestInfo.getInstanceName());
            	aq.setNetworkType(networkType);
            	aq.setNetworkId(sir.getNetworkInstanceId());
            }

            if(ModelType.volumeGroup.name().equalsIgnoreCase(requestScope)){
            	aq.setVolumeGroupId(sir.getVolumeGroupInstanceId());
            	aq.setVolumeGroupName(requestInfo.getInstanceName());
              	aq.setVnfType(vnfType);

            }

            if(ModelType.vfModule.name().equalsIgnoreCase(requestScope)){
             	aq.setVfModuleName(requestInfo.getInstanceName());
             	aq.setVfModuleModelName(modelInfo.getModelName());
             	aq.setVfModuleId(sir.getVfModuleInstanceId());
             	aq.setVolumeGroupId(sir.getVolumeGroupInstanceId());
              	aq.setVnfType(vnfType);

            }

            if(ModelType.vnf.name().equalsIgnoreCase(requestScope)){
              	aq.setVnfName(requestInfo.getInstanceName());
				if (null != sir.getRequestDetails()) {
					RelatedInstanceList[] instanceList = sir.getRequestDetails().getRelatedInstanceList();

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

            aq.setRequestBody (this.requestJSON);

            aq.setRequestStatus (status.toString ());
            aq.setLastModifiedBy (Constants.MODIFIED_BY_APIHANDLER);

            if ((status == Status.FAILED) || (status == Status.COMPLETE)) {
                aq.setStatusMessage (this.errorMessage);
                aq.setResponseBody (this.responseBody);
                aq.setProgress(100L);

                Timestamp endTimeStamp = new Timestamp (System.currentTimeMillis());
                aq.setEndTime (endTimeStamp);
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

    public void updateFinalStatus (Status status) {
        int result = 0;
        try {
            result = (RequestsDatabase.getInstance()).updateInfraFinalStatus (requestId,
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

    public String getRequestUri () {
        return requestUri;
    }

    public void setRequestUri (String requestUri) {
        this.requestUri = requestUri;
    }

    public VnfInputs getVnfInputs () {
        return vnfInputs;
    }

    public RequestInfo getRequestInfo () {
        return requestInfo;
    }

    public String getResponseBody () {
        return responseBody;
    }

    public void setResponseBody (String responseBody) {
        this.responseBody = responseBody;
    }

    public String getHttpResponse () {
        return httpResponse;
    }

    public void setHttpResponse (String httpResponse) {
        this.httpResponse = httpResponse;
    }

    public String getRequestId () {
        return requestId;
    }

    public String getRequestXML () {
        return requestXML;
    }

    public void setRequestXML (String requestXML) {
        this.requestXML = requestXML;
    }

    public RequestStatusType getStatus () {
        return status;
    }

    public String getServiceType () {
    	if (this.vnfInputs.getServiceType () != null)
    		return this.vnfInputs.getServiceType ();
    	if (this.vnfInputs.getServiceId () != null)
    		return this.vnfInputs.getServiceId ();
    	return null;
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

    public ModelInfo getModelInfo() {
    	return modelInfo;
    }

    public ServiceInstancesRequest getServiceInstancesRequest() {
    	return sir;
    }

    public String getServiceInstanceType () {
    	return serviceInstanceType;
    }

    public String getNetworkType () {
    	return networkType;
    }

    public String getVnfType () {
    	return vnfType;
    }

    public String getVfModuleModelName () {
    	return vfModuleModelName;
    }

    public String getVfModuleType () {
    	return vfModuleType;
    }

    public String getAsdcServiceModelVersion () {
    	return asdcServiceModelVersion;
    }

    public static String domToStr (Document doc) {
        if (doc == null) {
            return null;
        }

        try {
            StringWriter sw = new StringWriter ();
            StreamResult sr = new StreamResult (sw);
            TransformerFactory tf = TransformerFactory.newInstance ();
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

    public void addBPMNSpecificInputs(String personaModelId, String personaModelVersion, Boolean isBaseVfModule,
    			String vnfPersonaModelId, String vnfPersonaModelVersion) {
    	vnfInputs.setPersonaModelId(personaModelId);
    	vnfInputs.setPersonaModelVersion(personaModelVersion);
    	vnfInputs.setIsBaseVfModule(isBaseVfModule);
    	vnfInputs.setVnfPersonaModelId(vnfPersonaModelId);
    	vnfInputs.setVnfPersonaModelVersion(vnfPersonaModelVersion);

    	this.vnfReq.setVnfInputs(vnfInputs);

          StringWriter stringWriter = new StringWriter ();
          try {
              JAXBContext jaxbContext = JAXBContext.newInstance (VnfRequest.class);
              Marshaller jaxbMarshaller = jaxbContext.createMarshaller ();

              // output pretty printed
              jaxbMarshaller.setProperty (Marshaller.JAXB_FORMATTED_OUTPUT, true);

              jaxbMarshaller.marshal (this.vnfReq, stringWriter);

          } catch (JAXBException e) {
              msoLogger.debug ("Exception: ", e);
          }

          this.requestXML = stringWriter.toString ();
          msoLogger.debug("REQUEST XML to BPEL: " + this.requestXML);


    }

    private static boolean empty(String s) {
    	  return (s == null || s.trim().isEmpty());
    }

    public String getRequestJSON() throws JsonGenerationException, JsonMappingException, IOException {
    	ObjectMapper mapper = new ObjectMapper();
    	mapper.setSerializationInclusion(Inclusion.NON_NULL);
    	//mapper.configure(Feature.WRAP_ROOT_VALUE, true);
    	msoLogger.debug ("building sir from object " + sir);
    	requestJSON = mapper.writeValueAsString(sir);
    	
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

	public boolean getALaCarteFlag() {
		return aLaCarteFlag;
	}

	public void setaLaCarteFlag(boolean aLaCarteFlag) {
		this.aLaCarteFlag = aLaCarteFlag;
	}

	public int getReqVersion() {
		return reqVersion;
	}

	public void setReqVersion(int reqVersion) {
		this.reqVersion = reqVersion;
	}
}
