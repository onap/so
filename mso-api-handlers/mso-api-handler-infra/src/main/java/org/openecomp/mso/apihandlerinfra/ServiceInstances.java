/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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
package org.openecomp.mso.apihandlerinfra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.codehaus.jackson.map.ObjectMapper;

import org.openecomp.mso.apihandler.common.*;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.*;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.*;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoAlarmLogger;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.properties.MsoPropertiesFactory;
import org.openecomp.mso.requestsdb.InfraActiveRequests;
import org.openecomp.mso.requestsdb.RequestsDatabase;
import org.openecomp.mso.utils.UUIDChecker;

@Path("/serviceInstances/{version:[vV][2-3]}")
public class ServiceInstances {

    private HashMap<String, String> instanceIdMap = new HashMap<String,String>();

    private static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.APIH);

    private static MsoAlarmLogger alarmLogger = new MsoAlarmLogger ();

    public final static String MSO_PROP_APIHANDLER_INFRA = "MSO_PROP_APIHANDLER_INFRA";

    private static MsoJavaProperties props = MsoPropertiesUtils.loadMsoProperties ();

    /**
	 *
	 */
	public ServiceInstances() {
		// TODO Auto-generated constructor stub
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createServiceInstance(String request, @PathParam("version") String version) {

		Response response = serviceInstances(request, Action.createInstance, null, version);

		return response;
	}

	@DELETE
	@Path("/{serviceInstanceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteServiceInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId) {

		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		Response response = serviceInstances(request, Action.deleteInstance, instanceIdMap, version);
		return response;
	}

	@POST
	@Path("/{serviceInstanceId}/vnfs")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createVnfInstance(String request,  @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId) {
        msoLogger.debug ("version is: " + version);
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		Response response = serviceInstances(request, Action.createInstance, instanceIdMap, version);

		return response;
	}

	@DELETE
	@Path("/{serviceInstanceId}/vnfs/{vnfInstanceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteVnfInstance(String request,  @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
													  @PathParam("vnfInstanceId") String vnfInstanceId) {

		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		instanceIdMap.put("vnfInstanceId", vnfInstanceId);
		Response response = serviceInstances(request, Action.deleteInstance, instanceIdMap, version);

		return response;
	}

	@POST
	@Path("/{serviceInstanceId}/vnfs/{vnfInstanceId}/vfModules")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createVfModuleInstance(String request,  @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
														   @PathParam("vnfInstanceId") String vnfInstanceId) {
        msoLogger.debug ("version is: " + version);
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		instanceIdMap.put("vnfInstanceId", vnfInstanceId);
		Response response = serviceInstances(request, Action.createInstance, instanceIdMap, version);

		return response;
	}

	@PUT
	@Path("/{serviceInstanceId}/vnfs/{vnfInstanceId}/vfModules/{vfmoduleInstanceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateVfModuleInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
														   @PathParam("vnfInstanceId") String vnfInstanceId,
														   @PathParam("vfmoduleInstanceId") String vfmoduleInstanceId) {

		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		instanceIdMap.put("vnfInstanceId", vnfInstanceId);
		instanceIdMap.put("vfModuleInstanceId", vfmoduleInstanceId);
		Response response = serviceInstances(request, Action.updateInstance, instanceIdMap, version);

		return response;
	}

	@DELETE
	@Path("/{serviceInstanceId}/vnfs/{vnfInstanceId}/vfModules/{vfmoduleInstanceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteVfModuleInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
																		@PathParam("vnfInstanceId") String vnfInstanceId,
																		@PathParam("vfmoduleInstanceId") String vfmoduleInstanceId) {


		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		instanceIdMap.put("vnfInstanceId", vnfInstanceId);
		instanceIdMap.put("vfModuleInstanceId", vfmoduleInstanceId);
		Response response = serviceInstances(request, Action.deleteInstance, instanceIdMap, version);

		return response;
	}


	@POST
	@Path("/{serviceInstanceId}/vnfs/{vnfInstanceId}/volumeGroups")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createVolumeGroupInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
			                                                               @PathParam("vnfInstanceId") String vnfInstanceId) {

		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		instanceIdMap.put("vnfInstanceId", vnfInstanceId);
		Response response = serviceInstances(request, Action.createInstance, instanceIdMap, version);

		return response;
	}

	@PUT
	@Path("/{serviceInstanceId}/vnfs/{vnfInstanceId}/volumeGroups/{volumeGroupInstanceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateVolumeGroupInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
																		   @PathParam("vnfInstanceId") String vnfInstanceId,
																		   @PathParam("volumeGroupInstanceId") String volumeGroupInstanceId) {


		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		instanceIdMap.put("vnfInstanceId", vnfInstanceId);
		instanceIdMap.put("volumeGroupInstanceId", volumeGroupInstanceId);
		Response response = serviceInstances(request, Action.updateInstance, instanceIdMap, version);

		return response;
	}

	@DELETE
	@Path("/{serviceInstanceId}/vnfs/{vnfInstanceId}/volumeGroups/{volumeGroupInstanceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteVolumeGroupInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
																		   @PathParam("vnfInstanceId") String vnfInstanceId,
																		   @PathParam("volumeGroupInstanceId") String volumeGroupInstanceId) {


		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		instanceIdMap.put("vnfInstanceId", vnfInstanceId);
		instanceIdMap.put("volumeGroupInstanceId", volumeGroupInstanceId);
		Response response = serviceInstances(request, Action.deleteInstance, instanceIdMap, version);

		return response;
	}

	@POST
	@Path("/{serviceInstanceId}/networks")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createNetworkInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId) {

		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		Response response = serviceInstances(request, Action.createInstance, instanceIdMap, version);

		return response;
	}

	@PUT
	@Path("/{serviceInstanceId}/networks/{networkInstanceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateNetworkInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
																	   @PathParam("networkInstanceId") String networkInstanceId) {

		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		instanceIdMap.put("networkInstanceId", networkInstanceId);
		Response response = serviceInstances(request, Action.updateInstance, instanceIdMap, version);

		return response;
	}

	@DELETE
	@Path("/{serviceInstanceId}/networks/{networkInstanceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteNetworkInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
																	   @PathParam("networkInstanceId") String networkInstanceId) {

		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		instanceIdMap.put("networkInstanceId", networkInstanceId);
		Response response = serviceInstances(request, Action.deleteInstance, instanceIdMap, version);

		return response;
	}



	private Response serviceInstances(String requestJSON, Action action, HashMap<String,String> instanceIdMap, String version) {

	   String requestId = UUIDChecker.generateUUID(msoLogger);
	   long startTime = System.currentTimeMillis ();
	   msoLogger.debug ("requestId is: " + requestId);
	   ServiceInstancesRequest sir = null;

	   MsoRequest msoRequest = new MsoRequest (requestId);


	   try{
       	ObjectMapper mapper = new ObjectMapper();
       	sir = mapper.readValue(requestJSON, ServiceInstancesRequest.class);

       } catch(Exception e){
           msoLogger.debug ("Mapping of request to JSON object failed : ", e);
           Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_REQUEST, MsoException.ServiceException,
                   "Mapping of request to JSON object failed.  " + e.getMessage(),
                   ErrorNumbers.SVC_BAD_PARAMETER, null);
           if (msoRequest.getRequestId () != null) {
               msoLogger.debug ("Mapping of request to JSON object failed");
               msoRequest.createRequestRecord (Status.FAILED, action);
           }
           msoLogger.error (MessageEnum.APIH_REQUEST_VALIDATION_ERROR, MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.SchemaError, requestJSON, e);
           msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.SchemaError, "Mapping of request to JSON object failed");
           msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
           return response;
       }


	   try{
		   msoRequest.parse(sir, instanceIdMap, action, version);
       } catch (Exception e) {
           msoLogger.debug ("Validation failed: ", e);
           Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_REQUEST, MsoException.ServiceException,
                   "Error parsing request.  " + e.getMessage(),
                   ErrorNumbers.SVC_BAD_PARAMETER, null);
           if (msoRequest.getRequestId () != null) {
               msoLogger.debug ("Logging failed message to the database");
               msoRequest.createRequestRecord (Status.FAILED, action);
           }
           msoLogger.error (MessageEnum.APIH_REQUEST_VALIDATION_ERROR, MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.SchemaError, requestJSON, e);
           msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.SchemaError, "Validation of the input request failed");
           msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
           return response;
       }

	   InfraActiveRequests dup = null;
	   String instanceName = sir.getRequestDetails().getRequestInfo().getInstanceName();
	   String requestScope = sir.getRequestDetails().getModelInfo().getModelType().name();
       try {
           if(!(instanceName==null && requestScope.equals("service") && action == Action.createInstance)){
               dup = RequestsDatabase.checkInstanceNameDuplicate (instanceIdMap, instanceName, requestScope);
           }
          } catch (Exception e) {
           msoLogger.error (MessageEnum.APIH_DUPLICATE_CHECK_EXC, MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.DataError, "Error during duplicate check ", e);

          Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, MsoException.ServiceException,
                                                                 e.getMessage(),
                                                                 ErrorNumbers.SVC_DETAILED_SERVICE_ERROR,
                                                                 null) ;


          msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, "Error during duplicate check");
          msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
          return response;
       }

       if (dup != null) {
        	 // Found the duplicate record. Return the appropriate error.
  	 	   String instance = null;
  	 	   if(instanceName != null){
  	 		   instance = instanceName;
  	 	   }else{
  	 		   instance = instanceIdMap.get(requestScope + "InstanceId");
  	 	   }
  	 	   String dupMessage = "Error: Locked instance - This " + requestScope + " (" + instance + ") " + "already has a request being worked with a status of " + dup.getRequestStatus() + " (RequestId - " + dup.getRequestId() + "). The existing request must finish or be cleaned up before proceeding.";
           //List<String> variables = new ArrayList<String>();
           //variables.add(dup.getRequestStatus());

           Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_CONFLICT, MsoException.ServiceException,
                   dupMessage,
                   ErrorNumbers.SVC_DETAILED_SERVICE_ERROR,
                   null) ;


           msoLogger.warn (MessageEnum.APIH_DUPLICATE_FOUND, dupMessage, "", "", MsoLogger.ErrorCode.SchemaError, "Duplicate request - Subscriber already has a request for this service");
           msoRequest.createRequestRecord (Status.FAILED, action);
           msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.Conflict, dupMessage);
           msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
           return response;
       }


	   ServiceInstancesResponse serviceResponse = new ServiceInstancesResponse();

	   RequestReferences referencesResponse = new RequestReferences();

	   referencesResponse.setRequestId(requestId);

	   serviceResponse.setRequestReferences(referencesResponse);

        CatalogDatabase db = null;
        try {
            db = new CatalogDatabase ();
        } catch (Exception e) {
            msoLogger.error (MessageEnum.APIH_DB_ACCESS_EXC, MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.AvailabilityError, "Exception while communciate with Catalog DB", e);
            msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
            Response response = msoRequest.buildServiceErrorResponse (HttpStatus.SC_NOT_FOUND,
                    MsoException.ServiceException,
                    "No communication to catalog DB " + e.getMessage (),
                    ErrorNumbers.SVC_NO_SERVER_RESOURCES,
                    null);
            alarmLogger.sendAlarm ("MsoDatabaseAccessError",
                    MsoAlarmLogger.CRITICAL,
                    Messages.errors.get (ErrorNumbers.NO_COMMUNICATION_TO_CATALOG_DB));
            msoRequest.createRequestRecord (Status.FAILED,action);
            msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, "Exception while communciate with DB");
            msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
            return response;
        }



           RecipeLookupResult recipeLookupResult = null;
           try {
               recipeLookupResult = getServiceInstanceOrchestrationURI (db, msoRequest, action);
           } catch (ValidationException e) {
               msoLogger.debug ("Validation failed: ", e);
               Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_REQUEST, MsoException.ServiceException,
                       "Error validating request.  " + e.getMessage(),
                       ErrorNumbers.SVC_BAD_PARAMETER, null);
               if (msoRequest.getRequestId () != null) {
                   msoLogger.debug ("Logging failed message to the database");
                   msoRequest.createRequestRecord (Status.FAILED, action);
               }
               msoLogger.error (MessageEnum.APIH_REQUEST_VALIDATION_ERROR, MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.SchemaError, requestJSON, e);
               msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.SchemaError, "Validation of the input request failed");
               msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
               return response;
           } catch (Exception e) {
               msoLogger.error (MessageEnum.APIH_DB_ACCESS_EXC, MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.DataError, "Exception while querying Catalog DB", e);
               msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
               Response response = msoRequest.buildServiceErrorResponse (HttpStatus.SC_NOT_FOUND,
                       MsoException.ServiceException,
                       "Recipe could not be retrieved from catalog DB " + e.getMessage (),
                       ErrorNumbers.SVC_GENERAL_SERVICE_ERROR,
                       null);
               alarmLogger.sendAlarm ("MsoDatabaseAccessError",
                       MsoAlarmLogger.CRITICAL,
                       Messages.errors.get (ErrorNumbers.ERROR_FROM_CATALOG_DB));
               msoRequest.createRequestRecord (Status.FAILED,action);
               msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, "Exception while querying Catalog DB");
               msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
               db.close();
               return response;
           }

           if (recipeLookupResult == null) {
               msoLogger.error (MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND, MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.DataError, "No recipe found in DB");
               msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
               Response response = msoRequest.buildServiceErrorResponse (HttpStatus.SC_NOT_FOUND,
                       MsoException.ServiceException,
                       "Recipe does not exist in catalog DB",
                       ErrorNumbers.SVC_GENERAL_SERVICE_ERROR,
                       null);
               msoRequest.createRequestRecord (Status.FAILED, action);
               msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, "No recipe found in DB");
               msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
               db.close();
               return response;
           }


           Boolean isBaseVfModule = false;

           if (msoRequest.getModelInfo().getModelType().equals(ModelType.vfModule)) {
               String asdcServiceModelVersion = msoRequest.getAsdcServiceModelVersion ();

               // Get VF Module-specific base module indicator
               VfModule vfm = null;

               if (asdcServiceModelVersion != null && !asdcServiceModelVersion.isEmpty ()) {
                   vfm = db.getVfModuleType (msoRequest.getVfModuleType (), asdcServiceModelVersion);
               }
               else {
                   vfm = db.getVfModuleType (msoRequest.getVfModuleType ());
               }

               if (vfm != null) {
                   if (vfm.getIsBase() == 1) {
                       isBaseVfModule = true;
                   }
               }
               else if (action == Action.createInstance || action == Action.updateInstance){
                   // There is no entry for this vfModuleType with this version, if specified, in VF_MODULE table in Catalog DB.
                   // This request cannot proceed
                   msoLogger.error (MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND, MSO_PROP_APIHANDLER_INFRA, "VF Module Type", "", MsoLogger.ErrorCode.DataError, "No VfModuleType found in DB");
                   msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
                   String serviceVersionText = "";
                   if (asdcServiceModelVersion != null && !asdcServiceModelVersion.isEmpty ()) {
                       serviceVersionText = " with version " + asdcServiceModelVersion;
                   }
                   Response response = msoRequest.buildServiceErrorResponse (HttpStatus.SC_NOT_FOUND,
                           MsoException.ServiceException,
                           "VnfType " + msoRequest.getVnfType () + " and VF Module Model Name " + msoRequest.getVfModuleModelName() + serviceVersionText + " not found in MSO Catalog DB",
                           ErrorNumbers.SVC_BAD_PARAMETER,
                           null);
                   msoRequest.createRequestRecord (Status.FAILED, action);
                   msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, "No matching vfModuleType found in DB");
                   msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
                   db.close();
                   return response;
               }
           }

           db.close();

           String serviceInstanceId = "";
           String vnfId = "";
           String vfModuleId = "";
           String volumeGroupId = "";
           String networkId = "";
           ServiceInstancesRequest siReq = msoRequest.getServiceInstancesRequest();

           if(siReq.getServiceInstanceId () != null){
               serviceInstanceId = siReq.getServiceInstanceId ();
           }

           if(siReq.getVnfInstanceId () != null){
               vnfId = siReq.getVnfInstanceId ();
           }

           if(siReq.getVfModuleInstanceId () != null){
               vfModuleId = siReq.getVfModuleInstanceId ();
           }

           if(siReq.getVolumeGroupInstanceId () != null){
               volumeGroupId = siReq.getVolumeGroupInstanceId ();
           }

           if(siReq.getNetworkInstanceId () != null){
               networkId = siReq.getNetworkInstanceId ();
           }


           requestId = msoRequest.getRequestId ();
           msoLogger.debug ("requestId is: " + requestId);
           msoLogger.debug ("About to insert a record");

           try {
               msoRequest.createRequestRecord (Status.PENDING, action);
           } catch (Exception e) {
               msoLogger.error (MessageEnum.APIH_DB_ACCESS_EXC_REASON, "Exception while creating record in DB", "", "", MsoLogger.ErrorCode.SchemaError, "Exception while creating record in DB", e);
               msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
               Response response = msoRequest.buildServiceErrorResponse (HttpStatus.SC_INTERNAL_SERVER_ERROR,
                       MsoException.ServiceException,
                       "Exception while creating record in DB " + e.getMessage(),
                       ErrorNumbers.SVC_BAD_PARAMETER,
                       null);
               msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, "Exception while creating record in DB");
               msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
               return response;
           }

           RequestClient requestClient = null;
           HttpResponse response = null;
           long subStartTime = System.currentTimeMillis();
           try {
               requestClient = RequestClientFactory.getRequestClient (recipeLookupResult.getOrchestrationURI (), props);
               // Capture audit event
               msoLogger.debug ("MSO API Handler Posting call to BPEL engine for url: " + requestClient.getUrl ());

               System.out.println("URL : " + requestClient.getUrl ());

               response = requestClient.post(requestId, isBaseVfModule, recipeLookupResult.getRecipeTimeout (), action.name (),
                       serviceInstanceId, vnfId, vfModuleId, volumeGroupId, networkId,
                       msoRequest.getServiceInstanceType (),
                       msoRequest.getVnfType (), msoRequest.getVfModuleType (),
                       msoRequest.getNetworkType (), msoRequest.getRequestJSON());

               msoLogger.recordMetricEvent (subStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from BPMN engine", "BPMN", recipeLookupResult.getOrchestrationURI (), null);
           } catch (Exception e) {
               msoLogger.recordMetricEvent (subStartTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, "Exception while communicate with BPMN engine", "BPMN", recipeLookupResult.getOrchestrationURI (), null);
               msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
               Response resp = msoRequest.buildServiceErrorResponse (HttpStatus.SC_BAD_GATEWAY,
                       MsoException.ServiceException,
                       "Failed calling bpmn " + e.getMessage (),
                       ErrorNumbers.SVC_NO_SERVER_RESOURCES,
                       null);
               alarmLogger.sendAlarm ("MsoConfigurationError",
                       MsoAlarmLogger.CRITICAL,
                       Messages.errors.get (ErrorNumbers.NO_COMMUNICATION_TO_BPEL));
               msoRequest.updateFinalStatus (Status.FAILED);
               msoLogger.error (MessageEnum.APIH_BPEL_COMMUNICATE_ERROR, MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.AvailabilityError, "Exception while communicate with BPMN engine");
               msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, "Exception while communicate with BPMN engine");
               msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity ());
               return resp;
           }

           if (response == null) {
               msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
               Response resp = msoRequest.buildServiceErrorResponse (HttpStatus.SC_BAD_GATEWAY,
                       MsoException.ServiceException,
                       "bpelResponse is null",
                       ErrorNumbers.SVC_NO_SERVER_RESOURCES,
                       null);
               msoRequest.updateFinalStatus (Status.FAILED);
               msoLogger.error (MessageEnum.APIH_BPEL_COMMUNICATE_ERROR, MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Null response from BPEL");
               msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.InternalError, "Null response from BPMN");
               msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity ());
               return resp;
           }

           ResponseHandler respHandler = new ResponseHandler (response, requestClient.getType ());
           int bpelStatus = respHandler.getStatus ();

           // BPEL accepted the request, the request is in progress
           if (bpelStatus == HttpStatus.SC_ACCEPTED) {
               String camundaJSONResponseBody = respHandler.getResponseBody ();
               msoLogger.debug ("Received from Camunda: " + camundaJSONResponseBody);
               msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.IN_PROGRESS);
               RequestsDatabase.updateInfraStatus (msoRequest.getRequestId (),
                       Status.IN_PROGRESS.toString (),
                       Constants.PROGRESS_REQUEST_IN_PROGRESS,
                       Constants.MODIFIED_BY_APIHANDLER);
               msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "BPMN accepted the request, the request is in progress");
               msoLogger.debug ("End of the transaction, the final response is: " + (String) camundaJSONResponseBody);
               return Response.status (HttpStatus.SC_ACCEPTED).entity (camundaJSONResponseBody).build ();
           } else {
               List<String> variables = new ArrayList<String>();
               variables.add(bpelStatus + "");
               String camundaJSONResponseBody = respHandler.getResponseBody ();
               if (camundaJSONResponseBody != null && !camundaJSONResponseBody.isEmpty ()) {
                   msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
                   Response resp =  msoRequest.buildServiceErrorResponse(bpelStatus,
                           MsoException.ServiceException,
                           "Request Failed due to BPEL error with HTTP Status= %1 " + '\n' + camundaJSONResponseBody,
                           ErrorNumbers.SVC_DETAILED_SERVICE_ERROR,
                           variables);
                   msoRequest.updateFinalStatus (Status.FAILED);
                   msoLogger.error (MessageEnum.APIH_BPEL_RESPONSE_ERROR, requestClient.getUrl (), "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Response from BPEL engine is failed with HTTP Status=" + bpelStatus);
                   msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.InternalError, "Response from BPMN engine is failed");
                   msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity ());
                   return resp;
               } else {
                   msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
                   Response resp = msoRequest.buildServiceErrorResponse(bpelStatus,
                           MsoException.ServiceException,
                           "Request Failed due to BPEL error with HTTP Status= %1" ,
                           ErrorNumbers.SVC_DETAILED_SERVICE_ERROR,
                           variables);
                   msoRequest.updateFinalStatus (Status.FAILED);
                   msoLogger.error (MessageEnum.APIH_BPEL_RESPONSE_ERROR, requestClient.getUrl (), "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Response from BPEL engine is empty");
                   msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.InternalError, "Response from BPEL engine is empty");
                   msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity ());
                   return resp;
               }
           }

           //return Response.status (HttpStatus.SC_ACCEPTED).entity (serviceResponse).build ();
           // return serviceResponse;
	}

    private RecipeLookupResult getServiceInstanceOrchestrationURI (CatalogDatabase db, MsoRequest msoRequest, Action action) throws Exception {
        RecipeLookupResult recipeLookupResult = null;
        //if the aLaCarte flag is set to TRUE, the API-H should choose the â€œVID_DEFAULTâ€ recipe for the requested action

        msoLogger.debug("aLaCarteFlag is " + msoRequest.getALaCarteFlag());
        // Query MSO Catalog DB

        if (msoRequest.getModelInfo().getModelType().equals(ModelType.service)) {
            recipeLookupResult = getServiceURI(db, msoRequest, action);
        }
        else if (msoRequest.getModelInfo().getModelType().equals(ModelType.vfModule) ||
                msoRequest.getModelInfo().getModelType().equals(ModelType.volumeGroup) || msoRequest.getModelInfo().getModelType().equals(ModelType.vnf)) {

            recipeLookupResult = getVnfOrVfModuleUri(db, msoRequest, action);

        }else if (msoRequest.getModelInfo().getModelType().equals(ModelType.network)) {

            recipeLookupResult = getNetworkUri(db, msoRequest, action);
        }

        if (recipeLookupResult != null) {
            msoLogger.debug ("Orchestration URI is: " + recipeLookupResult.getOrchestrationURI() + ", recipe Timeout is: " + Integer.toString(recipeLookupResult.getRecipeTimeout ()));
        }
        else {
            msoLogger.debug("No matching recipe record found");
        }
        return recipeLookupResult;
    }


    private RecipeLookupResult getServiceURI (CatalogDatabase db, MsoRequest msoRequest, Action action) throws Exception {
        // SERVICE REQUEST
        // Construct the default service name
        // TODO need to make this a configurable property
        String sourceDefaultServiceName = msoRequest.getRequestInfo().getSource() + "_DEFAULT";
        String defaultService = "*";

        Service serviceRecord = null;
        int serviceId;
        ServiceRecipe recipe = null;

        //if an aLaCarte flag was Not sent in the request, look first if there is a custom recipe for the specific model version
        if(!msoRequest.getALaCarteFlag()){
            serviceRecord = db.getServiceByVersionAndInvariantId(msoRequest.getModelInfo().getModelInvariantId(), msoRequest.getModelInfo().getModelVersion());
            if(serviceRecord !=null){
                serviceId = serviceRecord.getId();
                recipe = db.getServiceRecipe(serviceId, action.name());
            }
        }

        if (recipe == null) {
            //find source(initiator) default recipe
            recipe = db.getServiceRecipeByServiceNameAndAction(sourceDefaultServiceName, action.name());
        }
        if (recipe == null) {
            //find default recipe
            recipe = db.getServiceRecipeByServiceNameAndAction(defaultService, action.name());
        }
        if(recipe==null){
            return null;
        }
        return new RecipeLookupResult (recipe.getOrchestrationUri (), recipe.getRecipeTimeout ());

    }


    private RecipeLookupResult getVnfOrVfModuleUri (CatalogDatabase db, MsoRequest msoRequest, Action action) throws Exception {

        String vnfComponentType = msoRequest.getModelInfo().getModelType().name();

        RelatedInstanceList[] instanceList = null;
        if (msoRequest.getServiceInstancesRequest().getRequestDetails() != null) {
            instanceList = msoRequest.getServiceInstancesRequest().getRequestDetails().getRelatedInstanceList();
        }

        String serviceModelName = null;
        String vnfModelName = null;
        String asdcServiceModelVersion = null;
        String modelVersion = msoRequest.getModelInfo().getModelVersion();
        Recipe recipe = null;
        String defaultVnfType = msoRequest.getRequestInfo().getSource() + "_DEFAULT";
        String modelCustomizationId = msoRequest.getModelInfo().getModelCustomizationId();
        String vfModuleModelName = msoRequest.getModelInfo().getModelName();
        if (instanceList != null) {

            for(RelatedInstanceList relatedInstanceList : instanceList){

                RelatedInstance relatedInstance = relatedInstanceList.getRelatedInstance();
                ModelInfo modelInfo = relatedInstance.getModelInfo();
                if(modelInfo.getModelType().equals(ModelType.service)){
                    serviceModelName = modelInfo.getModelName();
                    asdcServiceModelVersion = modelInfo.getModelVersion();
                }

                if(modelInfo.getModelType().equals(ModelType.vnf)){
                    vnfModelName = modelInfo.getModelCustomizationName();
                    if (null == vnfModelName || vnfModelName.trim().isEmpty()) {
                        VnfResource vnfResource = db.getVnfResourceByModelCustomizationId(modelInfo.getModelCustomizationUuid(), modelInfo.getModelVersion());
                        vnfModelName = vnfResource.getModelName();
                    }
                }
            }

            if(msoRequest.getModelInfo().getModelType().equals(ModelType.vnf)) {
                String modelCustomizationName = msoRequest.getModelInfo().getModelCustomizationName();

                VnfResource vnfResource = null;

                // Validation for vnfResource
                if(modelCustomizationName!=null) {
                    vnfResource = db.getVnfResource(serviceModelName + "/" + modelCustomizationName, asdcServiceModelVersion);
                }else{
                    vnfResource = db.getVnfResourceByModelCustomizationId(modelCustomizationId, asdcServiceModelVersion);
                }

                if(vnfResource==null){
                    throw new ValidationException("catalog entry");
                }

                VnfRecipe vnfRecipe = db.getVnfRecipe(defaultVnfType, action.name());

                if (vnfRecipe == null) {
                    return null;
                }

                return new RecipeLookupResult (vnfRecipe.getOrchestrationUri(), vnfRecipe.getRecipeTimeout());
            }else{
                String vnfType = serviceModelName + "/" + vnfModelName;
                String vfModuleType = vnfType + "::" + vfModuleModelName;
                List<VfModule> vfModule = db.getVfModule(vfModuleType, modelCustomizationId, asdcServiceModelVersion, modelVersion, action.name());
                if(vfModule==null || vfModule.isEmpty()){
                    throw new ValidationException("catalog entry");
                }else{
                    if(!msoRequest.getALaCarteFlag() && action != Action.deleteInstance){
                        recipe = db.getVnfComponentsRecipeByVfModule(vfModule, action.name());
                    }
                }
                if (recipe == null) {
                    msoLogger.debug("recipe is null, getting default");
                    recipe = db.getVnfComponentsRecipeByVfModuleId("VID_DEFAULT", vnfComponentType, action.name());

                    if (recipe == null) {
                        return null;
                    }
                }

            }
        } else {
            msoLogger.debug("recipe is null, getting default");

            if(msoRequest.getModelInfo().getModelType().equals(ModelType.vnf)) {
                recipe = db.getVnfRecipe(defaultVnfType, action.name());
                if (recipe == null) {
                    return null;
                }
            } else {
                recipe = db.getVnfComponentsRecipeByVfModuleId("VID_DEFAULT", vnfComponentType, action.name());

                if (recipe == null) {
                    return null;
                }
            }
        }

        return new RecipeLookupResult (recipe.getOrchestrationUri (), recipe.getRecipeTimeout ());
    }

    private RecipeLookupResult getNetworkUri (CatalogDatabase db, MsoRequest msoRequest, Action action) throws Exception {

        String sourceDefaultNetworkType = msoRequest.getRequestInfo().getSource() + "_DEFAULT";
        String defaultNetworkType = "*";

        String modelName = msoRequest.getModelInfo().getModelName();
        Recipe recipe = null;
        //if an aLaCarte flag was Not sent in the request, look first if there is a custom recipe for the specific ModelCustomizationId
        if(!msoRequest.getALaCarteFlag()){
            String networkType = null;

            if(msoRequest.getModelInfo().getModelCustomizationId()!=null){
                NetworkResource networkResource = db.getNetworkResourceByModelCustUuid(msoRequest.getModelInfo().getModelCustomizationId());
                if(networkResource!=null){
                    networkType = networkResource.getNetworkType();
                }else{
                    throw new ValidationException("no catalog entry found");
                }
            }else{
                //ok for version < 3
                networkType = modelName;
            }

            //find latest version Recipe for the given networkType and action
            recipe = db.getNetworkRecipe(networkType, action.name());
        }

        if(recipe == null){
            //find source(initiator) default recipe
            recipe = db.getNetworkRecipe(sourceDefaultNetworkType, action.name());
        }
        if(recipe == null){
            //find default recipe
            recipe = db.getNetworkRecipe(defaultNetworkType, action.name());
        }
        if (recipe == null) {
            return null;
        }
        return new RecipeLookupResult (recipe.getOrchestrationUri (), recipe.getRecipeTimeout ());
    }
}
