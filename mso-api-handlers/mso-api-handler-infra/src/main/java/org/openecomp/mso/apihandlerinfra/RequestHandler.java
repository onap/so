package org.openecomp.mso.apihandlerinfra;

/*-
 * #%L
 * MSO
 * %%
 * Copyright (C) 2016 OPENECOMP - MSO
 * %%
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
 * #L%
 */


import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.openecomp.mso.HealthCheckUtils;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;

import org.openecomp.mso.apihandler.common.ErrorNumbers;
import org.openecomp.mso.apihandler.common.RequestClient;
import org.openecomp.mso.apihandler.common.RequestClientFactory;
import org.openecomp.mso.apihandler.common.ResponseHandler;
import org.openecomp.mso.apihandler.common.ValidationException;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.GetOrchestrationListResponse;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.GetOrchestrationResponse;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.InstanceReferences;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.RelatedInstance;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.RelatedInstanceList;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.Request;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.RequestDetails;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.RequestError;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.RequestList;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.RequestStatus;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.RequestReferences;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.ServiceInstancesRequest;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.ServiceInstancesResponse;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.Recipe;
import org.openecomp.mso.db.catalog.beans.Service;
import org.openecomp.mso.db.catalog.beans.ServiceRecipe;
import org.openecomp.mso.db.catalog.beans.VfModule;
import org.openecomp.mso.db.catalog.beans.VnfComponentsRecipe;
import org.openecomp.mso.db.catalog.beans.VnfRecipe;
import org.openecomp.mso.db.catalog.beans.VnfResource;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoAlarmLogger;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.properties.MsoPropertiesFactory;
import org.openecomp.mso.requestsdb.InfraActiveRequests;
import org.openecomp.mso.requestsdb.RequestsDatabase;
import org.openecomp.mso.utils.UUIDChecker;

@Path("/")
public class RequestHandler {

    public final static String MSO_PROP_APIHANDLER_INFRA = "MSO_PROP_APIHANDLER_INFRA";

    private static MsoPropertiesFactory msoPropertiesFactory = new MsoPropertiesFactory ();

    @Context
    private UriInfo uriInfo;

    private static final String NOT_FOUND = "<!DOCTYPE html><html><head><meta charset=\"ISO-8859-1\"><title>Application Not Started</title></head><body>Application not started, properties file missing or invalid or Database Connection failed</body></html>";

    private static final Response NOT_STARTED_RESPONSE = Response.status (HttpStatus.SC_SERVICE_UNAVAILABLE)
                                                                 .entity (NOT_FOUND)
                                                                 .build ();

    private static boolean noProperties = true;

    private static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.APIH);
    private static MsoAlarmLogger alarmLogger = new MsoAlarmLogger ();

    @Context
    private ServletContext sc;

    private static MsoJavaProperties props = loadMsoProperties ();
    HashMap<String, String> instanceIdMap = new HashMap<String,String>();

    @HEAD
    @GET
    @Path("/healthcheck")
    @Produces("text/html")
    public Response healthcheck (@QueryParam("requestId") String requestId) {
        long startTime = System.currentTimeMillis ();
        MsoLogger.setServiceName ("Healthcheck");
        UUIDChecker.verifyOldUUID(requestId, msoLogger);
        HealthCheckUtils healthCheck = new HealthCheckUtils ();
        if (!healthCheck.siteStatusCheck(msoLogger, startTime)) {
            return HealthCheckUtils.HEALTH_CHECK_NOK_RESPONSE;
        }

        if (!healthCheck.configFileCheck(msoLogger, startTime, MSO_PROP_APIHANDLER_INFRA)) {
            return HealthCheckUtils.NOT_STARTED_RESPONSE;
        }

        if (!healthCheck.requestDBCheck (msoLogger, startTime)) {
            return HealthCheckUtils.NOT_STARTED_RESPONSE;
        }
        msoLogger.debug("healthcheck - Successful");
        return HealthCheckUtils.HEALTH_CHECK_RESPONSE;
    }

    @HEAD
    @GET
    @Path("/globalhealthcheck")
    @Produces("text/html")
    public Response globalHealthcheck (@DefaultValue("true") @QueryParam("enableBpmn") boolean enableBpmn) {
        long startTime = System.currentTimeMillis ();
        MsoLogger.setServiceName ("GlobalHealthcheck");
        // Generate a Request Id
        String requestId = UUIDChecker.generateUUID(msoLogger);
        HealthCheckUtils healthCheck = new HealthCheckUtils ();
        if (!healthCheck.siteStatusCheck (msoLogger, startTime)) {
            return HealthCheckUtils.HEALTH_CHECK_NOK_RESPONSE;
        }

        if (healthCheck.verifyGlobalHealthCheck(enableBpmn, requestId)) {
            msoLogger.debug("globalHealthcheck - Successful");
            return HealthCheckUtils.HEALTH_CHECK_RESPONSE;
        } else {
            msoLogger.debug("globalHealthcheck - At leaset one of the sub-modules is not available");
            return  HealthCheckUtils.HEALTH_CHECK_NOK_RESPONSE;
        }
    } 

    private static MsoJavaProperties loadMsoProperties () {
        MsoJavaProperties msoProperties;
        try {
            msoProperties = msoPropertiesFactory.getMsoJavaProperties (MSO_PROP_APIHANDLER_INFRA);
        } catch (Exception e) {
            msoLogger.error (MessageEnum.APIH_LOAD_PROPERTIES_FAIL, MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.DataError, "Exception when loading MSO Properties", e);
            return null;
        }

        if (msoProperties != null && msoProperties.size () > 0) {
            noProperties = false;
            msoLogger.info (MessageEnum.APIH_PROPERTY_LOAD_SUC, "", "");
            return msoProperties;
        } else {
            msoLogger.error (MessageEnum.APIH_NO_PROPERTIES, MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.DataError, "No MSO APIH_INFRA Properties found");
            return null;
        }
    }
    
	@POST
	@Path("/serviceInstances/v2")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createServiceInstance(String request) {
				
		Response response = serviceInstances(request, Action.createInstance, null);
		
		return response;
	}
	
	@DELETE
	@Path("/serviceInstances/v2/{serviceInstanceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteServiceInstance(String request, @PathParam("serviceInstanceId") String serviceInstanceId) {
		
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		Response response = serviceInstances(request, Action.deleteInstance, instanceIdMap);
		return response;
	}
	
	@POST
	@Path("/serviceInstances/v2/{serviceInstanceId}/vnfs")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createVnfInstance(String request, @PathParam("serviceInstanceId") String serviceInstanceId) {
		
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		Response response = serviceInstances(request, Action.createInstance, instanceIdMap);
		
		return response;
	}
	
	@DELETE
	@Path("/serviceInstances/v2/{serviceInstanceId}/vnfs/{vnfInstanceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteVnfInstance(String request, @PathParam("serviceInstanceId") String serviceInstanceId,
													  @PathParam("vnfInstanceId") String vnfInstanceId) {
		
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		instanceIdMap.put("vnfInstanceId", vnfInstanceId);
		Response response = serviceInstances(request, Action.deleteInstance, instanceIdMap);
		
		return response;
	}
	
	@POST
	@Path("/serviceInstances/v2/{serviceInstanceId}/vnfs/{vnfInstanceId}/vfModules")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createVfModuleInstance(String request, @PathParam("serviceInstanceId") String serviceInstanceId, 
														   @PathParam("vnfInstanceId") String vnfInstanceId) {
		
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		instanceIdMap.put("vnfInstanceId", vnfInstanceId);
		Response response = serviceInstances(request, Action.createInstance, instanceIdMap);
		
		return response;
	}
	
	@PUT
	@Path("/serviceInstances/v2/{serviceInstanceId}/vnfs/{vnfInstanceId}/vfModules/{vfmoduleInstanceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateVfModuleInstance(String request, @PathParam("serviceInstanceId") String serviceInstanceId, 
														   @PathParam("vnfInstanceId") String vnfInstanceId,
														   @PathParam("vfmoduleInstanceId") String vfmoduleInstanceId) {
		
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		instanceIdMap.put("vnfInstanceId", vnfInstanceId);
		instanceIdMap.put("vfModuleInstanceId", vfmoduleInstanceId);
		Response response = serviceInstances(request, Action.updateInstance, instanceIdMap);
		
		return response;
	}
	
	@DELETE
	@Path("/serviceInstances/v2/{serviceInstanceId}/vnfs/{vnfInstanceId}/vfModules/{vfmoduleInstanceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteVfModuleInstance(String request, @PathParam("serviceInstanceId") String serviceInstanceId,
																		@PathParam("vnfInstanceId") String vnfInstanceId,
																		@PathParam("vfmoduleInstanceId") String vfmoduleInstanceId) {
		
		
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		instanceIdMap.put("vnfInstanceId", vnfInstanceId);
		instanceIdMap.put("vfModuleInstanceId", vfmoduleInstanceId);
		Response response = serviceInstances(request, Action.deleteInstance, instanceIdMap);
		
		return response;
	}
	
	
	@POST
	@Path("/serviceInstances/v2/{serviceInstanceId}/vnfs/{vnfInstanceId}/volumeGroups")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createVolumeGroupInstance(String request, @PathParam("serviceInstanceId") String serviceInstanceId,
			                                                               @PathParam("vnfInstanceId") String vnfInstanceId) {
		
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		instanceIdMap.put("vnfInstanceId", vnfInstanceId);
		Response response = serviceInstances(request, Action.createInstance, instanceIdMap);
		
		return response;
	}
	
	@PUT
	@Path("/serviceInstances/v2/{serviceInstanceId}/vnfs/{vnfInstanceId}/volumeGroups/{volumeGroupInstanceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateVolumeGroupInstance(String request, @PathParam("serviceInstanceId") String serviceInstanceId,
																		   @PathParam("vnfInstanceId") String vnfInstanceId,
																		   @PathParam("volumeGroupInstanceId") String volumeGroupInstanceId) {
		
		
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		instanceIdMap.put("vnfInstanceId", vnfInstanceId);
		instanceIdMap.put("volumeGroupInstanceId", volumeGroupInstanceId);
		Response response = serviceInstances(request, Action.updateInstance, instanceIdMap);
		
		return response;
	}
	
	@DELETE
	@Path("/serviceInstances/v2/{serviceInstanceId}/vnfs/{vnfInstanceId}/volumeGroups/{volumeGroupInstanceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteVolumeGroupInstance(String request, @PathParam("serviceInstanceId") String serviceInstanceId,
																		   @PathParam("vnfInstanceId") String vnfInstanceId,
																		   @PathParam("volumeGroupInstanceId") String volumeGroupInstanceId) {
		
		
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		instanceIdMap.put("vnfInstanceId", vnfInstanceId);
		instanceIdMap.put("volumeGroupInstanceId", volumeGroupInstanceId);
		Response response = serviceInstances(request, Action.deleteInstance, instanceIdMap);
		
		return response;
	}
	
	@POST
	@Path("/serviceInstances/v2/{serviceInstanceId}/networks")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createNetworkInstance(String request, @PathParam("serviceInstanceId") String serviceInstanceId) {
		
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		Response response = serviceInstances(request, Action.createInstance, instanceIdMap);
		
		return response;
	}
	
	@PUT
	@Path("/serviceInstances/v2/{serviceInstanceId}/networks/{networkInstanceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateNetworkInstance(String request, @PathParam("serviceInstanceId") String serviceInstanceId,
																	   @PathParam("networkInstanceId") String networkInstanceId) {
		
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		instanceIdMap.put("networkInstanceId", networkInstanceId);
		Response response = serviceInstances(request, Action.updateInstance, instanceIdMap);
		
		return response;
	} 
	
	@DELETE
	@Path("/serviceInstances/v2/{serviceInstanceId}/networks/{networkInstanceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteNetworkInstance(String request, @PathParam("serviceInstanceId") String serviceInstanceId,
																	   @PathParam("networkInstanceId") String networkInstanceId) {
		
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		instanceIdMap.put("networkInstanceId", networkInstanceId);
		Response response = serviceInstances(request, Action.deleteInstance, instanceIdMap);
		
		return response;
	} 
    
    
	
	private Response serviceInstances(String requestJSON, Action action, HashMap<String,String> instanceIdMap) {
		
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
		   msoRequest.parse(sir, instanceIdMap, action);
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
     		 dup = RequestsDatabase.checkInstanceNameDuplicate (instanceIdMap, instanceName, requestScope);
     		       		 
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
	   	   
       try (CatalogDatabase db = new CatalogDatabase()) {

           RecipeLookupResult recipeLookupResult = null;
           try {
               recipeLookupResult = getServiceInstanceOrchestrationURI (db, msoRequest, action);
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
                       msoRequest.getNetworkType (), requestJSON);

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
                   msoLogger.error (MessageEnum.APIH_BPEL_RESPONSE_ERROR, MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Response from BPEL engine is failed with HTTP Status=" + bpelStatus);
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
                   msoLogger.error (MessageEnum.APIH_BPEL_RESPONSE_ERROR, MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Response from BPEL engine is empty");
                   msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.InternalError, "Response from BPEL engine is empty");
                   msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity ());
                   return resp;
               }
           }

           //return Response.status (HttpStatus.SC_ACCEPTED).entity (serviceResponse).build ();
           // return serviceResponse;
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
	} 
	
	@GET
	@Path("/orchestrationRequests/v2/{requestId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOrchestrationRequest(@PathParam("requestId") String requestId) {
		
		GetOrchestrationResponse orchestrationResponse = new GetOrchestrationResponse();
		
		MsoRequest msoRequest = new MsoRequest (requestId);
		
		long startTime = System.currentTimeMillis ();
		
		InfraActiveRequests requestDB = null;
        
        try {
       		 requestDB = RequestsDatabase.getRequestFromInfraActive(requestId);
       		 
            } catch (Exception e) {
                msoLogger.error (MessageEnum.APIH_DB_ACCESS_EXC, MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.AvailabilityError, "Exception while communciate with Request DB - Infra Request Lookup", e);
                msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
                Response response = msoRequest.buildServiceErrorResponse (HttpStatus.SC_NOT_FOUND,
             		   												  MsoException.ServiceException,
             		   												  e.getMessage (),
                                                                       ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB,
                                                                       null);
                alarmLogger.sendAlarm ("MsoDatabaseAccessError",
                                       MsoAlarmLogger.CRITICAL,
                                       Messages.errors.get (ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB));
                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, "Exception while communciate with Request DB");
                msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
                return response;
            	
            }
        
        if(requestDB == null) {
            Response resp = msoRequest.buildServiceErrorResponse (HttpStatus.SC_NO_CONTENT,
         		   											 MsoException.ServiceException,
         		   											"Orchestration RequestId " + requestId + " is not found in DB",                                                            
                                                             ErrorNumbers.SVC_DETAILED_SERVICE_ERROR,
                                                             null);
            msoLogger.error (MessageEnum.APIH_BPEL_COMMUNICATE_ERROR, MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Null response from RequestDB when searching by RequestId");
            msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, "Null response from RequestDB when searching by RequestId");
            msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity ());
            return resp;
        	
        }
  
        Request request = mapInfraActiveRequestToRequest(requestDB);
         
        orchestrationResponse.setRequest(request);
		
        return Response.status(200).entity(orchestrationResponse).build();
	}
	
	@GET
	@Path("/orchestrationRequests/v2")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOrchestrationRequest(@Context UriInfo ui) {
		
		long startTime = System.currentTimeMillis ();
		
		MsoRequest msoRequest = new MsoRequest();
		
		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
		
		List<InfraActiveRequests> activeRequests = null;
		
		GetOrchestrationListResponse orchestrationList = null;
		
		
		try{
		
			Map<String, List<String>> orchestrationMap = msoRequest.getOrchestrationFilters(queryParams);
			
			activeRequests = RequestsDatabase.getOrchestrationFiltersFromInfraActive(orchestrationMap);
			
			orchestrationList = new GetOrchestrationListResponse();

			List<RequestList> requestLists = new ArrayList<RequestList>();
						
			for(InfraActiveRequests infraActive : activeRequests){
				
				Request request = mapInfraActiveRequestToRequest(infraActive);
				RequestList requestList = new RequestList();
				requestList.setRequest(request);

				requestLists.add(requestList);

			}
			
			orchestrationList.setRequestList(requestLists);

		}catch(Exception e){
	           msoLogger.debug ("Get Orchestration Request with Filters Failed : ", e);
	           Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, MsoException.ServiceException, 
	                   "Get Orchestration Request with Filters Failed.  " + e.getMessage(), 
	                   ErrorNumbers.SVC_GENERAL_SERVICE_ERROR, null);
	           msoLogger.error (MessageEnum.APIH_GENERAL_EXCEPTION, MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Get Orchestration Request with Filters Failed : " + e);
	           msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataError, "Get Orchestration Request with Filters Failed");
	           msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
	           return response;
		}
		
			
        return Response.status(200).entity(orchestrationList).build();
	}

	@POST
    @Path("/v3/vnf-request")
    @Consumes("*/*")
    @Produces("application/xml")
    public Response manageVnfRequestV3 (String reqXML) {
    	// Set logger parameters
        MsoLogger.setServiceName ("VnfRequest");
        return manageVnfRequestImpl (reqXML, Constants.SCHEMA_VERSION_V3);
    }


    @POST
    @Path("/v2/vnf-request")
    @Consumes("*/*")
    @Produces("application/xml")
    public Response manageVnfRequestV2 (String reqXML) {
    	// Set logger parameters
        MsoLogger.setServiceName ("VnfRequest");
        return manageVnfRequestImpl (reqXML, Constants.SCHEMA_VERSION_V2);
    }

    @POST
    @Path("/v1/vnf-request")
    @Consumes("*/*")
    @Produces("application/xml")
    public Response manageVnfRequestV1 (String reqXML) {

        // Set logger parameters
        MsoLogger.setServiceName ("VnfRequest");

        return manageVnfRequestImpl (reqXML, Constants.SCHEMA_VERSION_V1);
    }
    
    @POST
    @Path("/v3/network-request")
    @Consumes("*/*")
    @Produces("application/xml")
    public Response manageNetworkRequestV3 (String reqXML) {

        // Set logger parameters
        MsoLogger.setServiceName ("NetworkRequest");

        return manageNetworkRequestImpl (reqXML, Constants.SCHEMA_VERSION_V3);
    }

    @POST
    @Path("/v2/network-request")
    @Consumes("*/*")
    @Produces("application/xml")
    public Response manageNetworkRequestV2 (String reqXML) {

        // Set logger parameters
        MsoLogger.setServiceName ("NetworkRequest");

        return manageNetworkRequestImpl (reqXML, Constants.SCHEMA_VERSION_V2);
    }
    
    @POST
    @Path("/v1/network-request")
    @Consumes("*/*")
    @Produces("application/xml")
    public Response manageNetworkRequestV1 (String reqXML) {

        // Set logger parameters
        MsoLogger.setServiceName ("NetworkRequest");

        return manageNetworkRequestImpl (reqXML, Constants.SCHEMA_VERSION_V1);
    }
    
    @POST
    @Path("/v3/volume-request")
    @Consumes("*/*")
    @Produces("application/xml")
    public Response manageVolumeRequestV3 (String reqXML) {
    	// Set logger parameters
        MsoLogger.setServiceName ("VolumeRequest");
        return manageVolumeRequestImpl (reqXML, Constants.SCHEMA_VERSION_V3);
    }

    @POST
    @Path("/v2/volume-request")
    @Consumes("*/*")
    @Produces("application/xml")
    public Response manageVolumeRequestV2 (String reqXML) {
    	// Set logger parameters
        MsoLogger.setServiceName ("VolumeRequest");
        return manageVolumeRequestImpl (reqXML, Constants.SCHEMA_VERSION_V2);
    }

    @POST
    @Path("/v1/volume-request")
    @Consumes("*/*")
    @Produces("application/xml")
    public Response manageVolumeRequestV1 (String reqXML) {

        // Set logger parameters
        MsoLogger.setServiceName ("VolumeRequest");

        return manageVolumeRequestImpl (reqXML, Constants.SCHEMA_VERSION_V1);
    }


    private Response manageVnfRequestImpl (String reqXML, String version) {
    	String methodName = "VnfRequest";
    	props = loadMsoProperties ();
        long startTime = System.currentTimeMillis ();

        // Generate unique request id for the new request
        UUID requestUUID = UUID.randomUUID ();

        VnfMsoInfraRequest msoRequest = new VnfMsoInfraRequest (requestUUID.toString ());
        MsoLogger.setLogContext (msoRequest.getRequestId (), null);

        if (noProperties) {
            msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.ServiceNotAvailable, "Exiting the transaction: Infra API Handler not started, properties file missing or invalid");
            return NOT_STARTED_RESPONSE;
        }

        uriInfo.getRequestUri ();

        if (reqXML == null) {
            msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.BadRequest, "The content of the request is null");
            return Response.status (HttpStatus.SC_NO_CONTENT).entity ("").build ();
        }

        String requestUri = uriInfo.getRequestUri ().toString ();
        msoLogger.debug ("Incoming request received for pose VNFRequest:" + reqXML);

        msoRequest.setRequestUri (requestUri);
        msoLogger.debug ("Schema version: " + version);
        try {
            msoRequest.parse (reqXML, version, props);
        } catch (Exception e) {
            msoLogger.debug ("Validation failed: ", e);
            msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
            Response response = msoRequest.buildResponseFailedValidation (HttpStatus.SC_BAD_REQUEST, e.getMessage ());
            if (msoRequest.getRequestId () != null) {
                msoLogger.debug ("Logging failed message to the database");
                msoRequest.createRequestRecord (Status.FAILED);
            }
            msoLogger.error (MessageEnum.APIH_REQUEST_VALIDATION_ERROR, reqXML, "", "", MsoLogger.ErrorCode.SchemaError, "Exception when parsing reqXML", e);
            msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.SchemaError, "Validation of the input request failed");
            msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
            return response;
        }
        MsoLogger.setServiceName (MsoLogger.getServiceName () + "_" + msoRequest.getRequestInfo().getAction().name());
        msoLogger.debug ("Update serviceName with detailed action info to:" + MsoLogger.getServiceName () + "_" + msoRequest.getRequestInfo().getAction().name());
        if (msoRequest.getRequestInfo ().getAction () == org.openecomp.mso.apihandlerinfra.vnfbeans.ActionType.CREATE) {
            // Check if this request is a duplicate of the one with the same vnfName
            msoLogger.debug ("Checking for a duplicate with the same vnf-name");
            InfraActiveRequests dup = null;
            try {
                dup = RequestsDatabase.checkDuplicateByVnfName (msoRequest.getVnfInputs ().getVnfName (),
                                                                msoRequest.getRequestInfo ().getAction ().value (),
                                                                "VNF");

            } catch (Exception e) {
                msoLogger.debug ("Exception", e);
                msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
                Response response = msoRequest.buildResponseWithError (HttpStatus.SC_INTERNAL_SERVER_ERROR,
                                                                       ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB,
                                                                       null,
                                                                       e.getMessage ());
                alarmLogger.sendAlarm ("MsoDatabaseAccessError",
                                       MsoAlarmLogger.CRITICAL,
                                       Messages.errors.get (ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB));
                msoRequest.createRequestRecord (Status.FAILED);
                msoLogger.error (MessageEnum.APIH_DUPLICATE_CHECK_EXC_ATT, "vnf-name", "", "", MsoLogger.ErrorCode.DataError, "Exception while checking for duplicated request", e);
                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, "Exception while checking for duplicated request");
                msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
                return response;
            }
            if (dup != null) {
                // Found the duplicate record. Return the appropriate error.
                msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
                Response response = msoRequest.buildResponse (HttpStatus.SC_CONFLICT,
                                                              ErrorNumbers.LOCKED_CREATE_ON_THE_SAME_VNF_NAME_IN_PROGRESS,
                                                              dup);
                msoLogger.warn (MessageEnum.APIH_DUPLICATE_FOUND, "CREATE on the same VNF Name is already progress", "", "", MsoLogger.ErrorCode.DataError, "Duplicates request - CREATE on the same VNF Name is already progress");
                msoRequest.createRequestRecord (Status.FAILED);
                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.Conflict, "Duplicates request - CREATE on the same VNF Name is already progress");
                msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
                return response;
            }
        } else {
            // Check if this request is a duplicate of the one with the same vnfId
            InfraActiveRequests dup = null;
            msoLogger.debug ("Checking for a duplicate with the same vnf-id");
            try {
                dup = RequestsDatabase.checkDuplicateByVnfId (msoRequest.getVnfInputs ().getVnfId (),
                                                              msoRequest.getRequestInfo ().getAction ().value (),
                                                              "VNF");

            } catch (Exception e) {
                msoLogger.debug ("Exception", e);
                msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
                Response response = msoRequest.buildResponseWithError (HttpStatus.SC_INTERNAL_SERVER_ERROR,
                                                                       ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB,
                                                                       null,
                                                                       e.getMessage ());
                alarmLogger.sendAlarm ("MsoDatabaseAccessError",
                                       MsoAlarmLogger.CRITICAL,
                                       Messages.errors.get (ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB));
                msoRequest.createRequestRecord (Status.FAILED);
                msoLogger.error (MessageEnum.APIH_DUPLICATE_CHECK_EXC_ATT, "vnf-id", "", "", MsoLogger.ErrorCode.DataError, "Exception while checking for a duplicate request with the same vnf-id", e);
                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, "Exception while checking for a duplicate request with the same vnf-id");
                msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
                return response;
            }
            if (dup != null) {
                // Found the duplicate record. Return the appropriate error.
                msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
                Response response = msoRequest.buildResponse (HttpStatus.SC_CONFLICT,
                                                              ErrorNumbers.LOCKED_SAME_ACTION_AND_VNF_ID,
                                                              dup);
                msoLogger.warn (MessageEnum.APIH_DUPLICATE_FOUND,
                                msoRequest.getRequestInfo ().getAction ().value ()
                                                                  + " on the same VNF Id already in progress", "", "", MsoLogger.ErrorCode.DataError, "Duplicated request on the same VNF Id already in progress");

                msoRequest.createRequestRecord (Status.FAILED);
                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.Conflict, "Duplicated request on the same VNF Id already in progress");
                msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
                return response;
            }
        }

        String orchestrationURI = "";

        try (CatalogDatabase db = new CatalogDatabase()) {

            Recipe recipe = null;

            if (version.equals(Constants.SCHEMA_VERSION_V1)) {
                // First get recipe for the service type given
                if (msoRequest.getServiceType () != null
                        && msoRequest.getServiceType ().length () > 0) {
                    recipe = db.getVnfRecipe (msoRequest.getVnfInputs ().getVnfType (),
                            msoRequest.getRequestInfo ().getAction ().value (),
                            msoRequest.getServiceType ());
                }
                // If no recipe for the service type or no service type was given, look for recipe without service type
                if (recipe == null) {
                    recipe = db.getVnfRecipe (msoRequest.getVnfInputs ().getVnfType (),
                            msoRequest.getRequestInfo ().getAction ().value (),
                            null);
                }
            }
            if (version.equals (Constants.SCHEMA_VERSION_V2) || version.equals (Constants.SCHEMA_VERSION_V3)) {
                switch (msoRequest.getRequestInfo ().getAction ()) {
                    case CREATE:
                    case UPDATE:
                    case DELETE:
                        // First get recipe for the vnf type given
                        recipe = db.getVnfRecipe (msoRequest.getVnfInputs ().getVnfType (),
                                msoRequest.getRequestInfo ().getAction ().value ());

                        // If no recipe for the vnf type is found, look for generic recipe with "*" vnf type
                        if (recipe == null) {
                            recipe = db.getVnfRecipe (Constants.VNF_TYPE_WILDCARD,
                                    msoRequest.getRequestInfo ().getAction ().value ());
                        }
                        break;
                    case CREATE_VF_MODULE:
                    case UPDATE_VF_MODULE:
                    case DELETE_VF_MODULE:
                        // First get recipe for the vnf type/vf module model name through vf module id query
                        recipe = db.getVfModuleRecipe (msoRequest.getVnfInputs ().getVnfType (), msoRequest.getVnfInputs ().getVfModuleModelName (),
                                msoRequest.getRequestInfo ().getAction ().value ());

                        // If no recipe is found, look for generic recipe with "*" vnf type
                        if (recipe == null) {
                            recipe = db.getVnfRecipeByVfModuleId (msoRequest.getVnfInputs ().getVfModuleId (),
                                    Constants.VNF_TYPE_WILDCARD, msoRequest.getRequestInfo ().getAction ().value ());
                        }
                        // First get recipe for the vnf type given
                        //recipe = db.getVnfRecipe (msoRequest.getVnfInputs ().getVnfType (),
                        //	msoRequest.getRequestInfo ().getAction ().value ());

                        // If no recipe for the vnf type is found, look for generic recipe with "*" vnf type
                        //if (recipe == null) {
                        //	recipe = db.getVnfRecipe (Constants.VNF_TYPE_WILDCARD,
                        //			msoRequest.getRequestInfo ().getAction ().value ());
                        //
                        //}
                        break;
                    default:
                        break;
                }

            }

            if (recipe == null) {
                msoLogger.error (MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND, "VNF Recipe", "", "", MsoLogger.ErrorCode.DataError, "No recipe found in DB");
                msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
                Response response = msoRequest.buildResponseWithError (HttpStatus.SC_NOT_FOUND,
                        ErrorNumbers.RECIPE_DOES_NOT_EXIST,
                        null,
                        "");
                msoRequest.createRequestRecord (Status.FAILED);
                db.close ();
                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, "No recipe found in DB");
                msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
                return response;
            }
            orchestrationURI = recipe.getOrchestrationUri ();
            msoLogger.debug ("Orchestration URI is: " + orchestrationURI);

            // Retrieve additional info for Vf Modules from Catalog DB to send it to BPMN
            switch (msoRequest.getRequestInfo ().getAction ()) {
                case CREATE_VF_MODULE:
                case UPDATE_VF_MODULE:
                    String personaModelId = "";
                    String personaModelVersion = "";
                    String vnfPersonaModelId = "";
                    String vnfPersonaModelVersion = "";
                    Boolean isBase = false;
                    String asdcServiceModelVersion = msoRequest.getVnfInputs ().getAsdcServiceModelVersion ();

                    // Get VF Module-specific persona info and base module indicator
                    VfModule vfm = null;
                    String vfModuleType = msoRequest.getVnfInputs ().getVnfType () + "::" + msoRequest.getVnfInputs ().getVfModuleModelName ();
                    if (asdcServiceModelVersion != null && !asdcServiceModelVersion.isEmpty ()) {
                        vfm = db.getVfModuleType (vfModuleType, asdcServiceModelVersion);
                    }
                    else {
                        vfm = db.getVfModuleType (vfModuleType);
                    }
                    if (vfm != null) {
                        if (vfm.getIsBase() == 1) {
                            isBase = true;
                        }
                        personaModelId = vfm.getModelInvariantUuid();
                        personaModelVersion = vfm.getModelVersion();
                        msoLogger.debug("Setting personaModelId to " + personaModelId +
                                ", personaModelVersion to " + personaModelVersion);
                    }
                    // Get VNF-specific persona info
                    VnfResource vnfr = null;
                    if (asdcServiceModelVersion != null && !asdcServiceModelVersion.isEmpty ()) {
                        vnfr = db.getVnfResource (msoRequest.getVnfInputs ().getVnfType (), asdcServiceModelVersion);
                    }
                    else {
                        vnfr = db.getVnfResource (msoRequest.getVnfInputs ().getVnfType ());
                    }
                    if (vnfr != null) {
                        vnfPersonaModelId = vnfr.getModelInvariantUuid ();
                        vnfPersonaModelVersion = vnfr.getModelVersion();
                        msoLogger.debug("Setting vnfPersonaModelId to " + vnfPersonaModelId +
                                ", vnfPersonaModelVersion to " + vnfPersonaModelVersion);
                    }

                    msoRequest.addBPMNSpecificInputs(personaModelId, personaModelVersion, isBase,
                            vnfPersonaModelId, vnfPersonaModelVersion);

                    break;
                default:
                    break;
            }

            db.close ();

            String requestId = msoRequest.getRequestId ();
            msoLogger.debug ("requestId is: " + requestId);
            msoLogger.debug ("About to insert a record");

            try {
                msoRequest.createRequestRecord (Status.PENDING);
            } catch (Exception e) {
                msoLogger.error (MessageEnum.APIH_DB_ACCESS_EXC_REASON, "Exception while creating record in DB", "", "", MsoLogger.ErrorCode.SchemaError, "Exception while creating record in DB", e);
                msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
                Response response = msoRequest.buildResponseWithError (HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        ErrorNumbers.COULD_NOT_WRITE_TO_REQUESTS_DB,
                        null,
                        "non-unique request-id specified");
                // Cannot create a record of this request here, our communication with MSO DB just failed. Do not try
                // to create a failed record
                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, "Exception while creating record in DB");
                msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
                return response;
            }

            msoLogger.debug("Request going to BPEL: " + msoRequest.getRequestXML ());

            RequestClient requestClient = null;
            HttpResponse response = null;
            long subStartTime = System.currentTimeMillis();
            try {
                requestClient = RequestClientFactory.getRequestClient (orchestrationURI, props);
                // Capture audit event
                msoLogger.debug ("MSO API Handler Posting call to BPEL engine for url: " + requestClient.getUrl ());
                response = requestClient.post (msoRequest.getRequestXML (),
                        requestId,
                        Integer.toString (recipe.getRecipeTimeout ()).toString (),
                        version,
                        null,
                        null);
                msoLogger.recordMetricEvent (subStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from BPMN engine", "BPMN", orchestrationURI, null);
            } catch (Exception e) {
                msoLogger.recordMetricEvent (subStartTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, "Exception while communicate with BPMN engine", "BPMN", orchestrationURI, null);
                msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
                Response resp = msoRequest.buildResponseWithError (HttpStatus.SC_BAD_GATEWAY,
                        ErrorNumbers.NO_COMMUNICATION_TO_BPEL,
                        null,
                        e.getMessage ());
                alarmLogger.sendAlarm ("MsoConfigurationError",
                        MsoAlarmLogger.CRITICAL,
                        Messages.errors.get (ErrorNumbers.NO_COMMUNICATION_TO_BPEL));
                msoRequest.updateFinalStatus (Status.FAILED);
                msoLogger.error (MessageEnum.APIH_BPEL_COMMUNICATE_ERROR, "Camunda", "", MsoLogger.ErrorCode.AvailabilityError, "Exception while communicate with BPMN engine", e);
                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, "Exception while communicate with BPMN engine");
                msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity ());
                return resp;
            }

            if (response == null) {
                msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
                Response resp = msoRequest.buildResponseWithError (HttpStatus.SC_BAD_GATEWAY,
                        ErrorNumbers.NO_RESPONSE_FROM_BPEL,
                        null,
                        "bpelResponse is null");
                msoRequest.updateFinalStatus (Status.FAILED);
                msoLogger.error (MessageEnum.APIH_BPEL_RESPONSE_ERROR, "Null response from BPEL", "Camunda", "", MsoLogger.ErrorCode.AvailabilityError, "Null response from BPEL");
                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.InternalError, "Null response from BPMN");
                msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity ());
                return resp;
            }

            ResponseHandler respHandler = new ResponseHandler (response, requestClient.getType ());
            int bpelStatus = respHandler.getStatus ();

            // BPEL accepted the request, the request is in progress
            if (bpelStatus == HttpStatus.SC_ACCEPTED) {
                String bpelXMLResponseBody = respHandler.getResponseBody ();
                msoLogger.debug ("Received from BPEL: " + bpelXMLResponseBody);
                msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.IN_PROGRESS);
                RequestsDatabase.updateInfraStatus (msoRequest.getRequestId (),
                        Status.IN_PROGRESS.toString (),
                        Constants.PROGRESS_REQUEST_IN_PROGRESS,
                        Constants.MODIFIED_BY_APIHANDLER);
                Response resp = msoRequest.buildResponse (bpelStatus, null, null);
                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "BPMN accepted the request, the request is in progress");
                msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity ());
                return resp;
            } else {

                String bpelXMLResponseBody = respHandler.getResponseBody ();
                if (bpelXMLResponseBody != null && !bpelXMLResponseBody.isEmpty ()) {
                    msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
                    Response resp = msoRequest.buildResponse (bpelStatus, bpelXMLResponseBody, null);
                    msoRequest.updateFinalStatus (Status.FAILED);
                    msoLogger.error (MessageEnum.APIH_BPEL_RESPONSE_ERROR,
                            "Response from BPEL engine is failed with HTTP Status=" + bpelStatus, "Camunda", "", MsoLogger.ErrorCode.BusinessProcesssError, "Response from BPEL engine is failed with HTTP Status=" + bpelStatus);
                    msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.InternalError, "Response from BPMN engine is failed");
                    msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity ());
                    return resp;
                } else {
                    msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
                    Response resp = msoRequest.buildResponse (bpelStatus, ErrorNumbers.ERROR_FROM_BPEL, null);
                    msoRequest.updateFinalStatus (Status.FAILED);
                    msoLogger.error (MessageEnum.APIH_BPEL_RESPONSE_ERROR, "Response from BPEL engine is empty", "Camunda", "", MsoLogger.ErrorCode.BusinessProcesssError, "Response from BPEL engine is empty");
                    msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.InternalError, "Response from BPEL engine is empty");
                    msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity ());
                    return resp;
                }
            }
        } catch (Exception e) {
            msoLogger.error (MessageEnum.APIH_DB_ACCESS_EXC, "", "", MsoLogger.ErrorCode.AvailabilityError, "Exception while communciate with Catalog DB", e);
            msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
            Response response = msoRequest.buildResponseWithError (HttpStatus.SC_NOT_FOUND,
                                                                   ErrorNumbers.NO_COMMUNICATION_TO_CATALOG_DB,
                                                                   null,
                                                                   e.getMessage ());
            alarmLogger.sendAlarm ("MsoDatabaseAccessError",
                                   MsoAlarmLogger.CRITICAL,
                                   Messages.errors.get (ErrorNumbers.NO_COMMUNICATION_TO_CATALOG_DB));
            msoRequest.createRequestRecord (Status.FAILED);
            msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, "Exception while communciate with DB");
            msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
            return response;
        }
    }

    private Response manageNetworkRequestImpl (String reqXML, String version) {
    	String methodName = "NetworkRequest";

    	props = loadMsoProperties ();

        long startTime = System.currentTimeMillis ();
        if (noProperties) {
            msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.ServiceNotAvailable, "Application not started, properties file missing or invalid");
        	return NOT_STARTED_RESPONSE;
        }
        uriInfo.getRequestUri ();

        // Generate unique request id for the new request
        UUID requestUUID = UUID.randomUUID ();

        NetworkMsoInfraRequest msoRequest = new NetworkMsoInfraRequest (requestUUID.toString ());
        MsoLogger.setLogContext (msoRequest.getRequestId (), null);

        if (reqXML == null) {
            msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.BadRequest, "The input Request is null");
            return Response.status (HttpStatus.SC_NO_CONTENT).entity ("").build ();
        }

        String requestUri = uriInfo.getRequestUri ().toString ();

        msoLogger.debug ("Incoming Request: " + reqXML);

        msoRequest.setRequestUri (requestUri);
        msoLogger.debug ("Schema version: " + version);
        try {
            msoRequest.parse (reqXML, version, props);
        } catch (Exception e) {
            msoLogger.debug ("Validation failed: ", e);
            msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.networkbeans.RequestStatusType.FAILED);
            Response response = msoRequest.buildResponseFailedValidation (HttpStatus.SC_BAD_REQUEST, e.getMessage ());
            if (msoRequest.getRequestId () != null) {
                msoLogger.debug ("Logging failed message to the database");
                msoRequest.createRequestRecord (Status.FAILED);
            }
            msoLogger.error (MessageEnum.APIH_REQUEST_VALIDATION_ERROR, reqXML, "", "", MsoLogger.ErrorCode.DataError, "Exception when parsing reqXML", e);
            msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.SchemaError, "Validation of the input request failed");
            msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
            return response;
        }
        MsoLogger.setServiceName (MsoLogger.getServiceName () + "_" + msoRequest.getRequestInfo().getAction().name());
        msoLogger.debug ("Update serviceName with detailed action info to:" + MsoLogger.getServiceName () + "_" + msoRequest.getRequestInfo().getAction().name());
        if (msoRequest.getRequestInfo ()
                      .getAction () == org.openecomp.mso.apihandlerinfra.networkbeans.ActionType.CREATE) {
            // Check if this request is a duplicate of the one with the same network Name
            msoLogger.debug ("Checking for a duplicate with the same network-name");
            InfraActiveRequests dup = null;
            try {

                dup = RequestsDatabase.checkDuplicateByVnfName (msoRequest.getNetworkInputs ().getNetworkName (),
                                                                msoRequest.getRequestInfo ().getAction ().value (),
                                                                "NETWORK");

            } catch (Exception e) {
                msoLogger.debug ("Exception", e);
                msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.networkbeans.RequestStatusType.FAILED);
                Response response = msoRequest.buildResponseWithError (HttpStatus.SC_INTERNAL_SERVER_ERROR,
                                                                       ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB,
                                                                       null,
                                                                       e.getMessage ());
                alarmLogger.sendAlarm ("MsoDatabaseAccessError",
                                       MsoAlarmLogger.CRITICAL,
                                       Messages.errors.get (ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB));
                msoRequest.createRequestRecord (Status.FAILED);
                msoLogger.error (MessageEnum.APIH_DUPLICATE_CHECK_EXC_ATT, "network-name", "", "", MsoLogger.ErrorCode.DataError, "Exception while checking for duplicated request", e);
                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, "Exception while checking for duplicated request");
                msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
                return response;
            }
            if (dup != null) {
                // Found the duplicate record. Return the appropriate error.
                msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.networkbeans.RequestStatusType.FAILED);
                Response response = msoRequest.buildResponse (HttpStatus.SC_CONFLICT,
                                                              ErrorNumbers.LOCKED_CREATE_ON_THE_SAME_VNF_NAME_IN_PROGRESS,
                                                              dup);
                msoLogger.warn (MessageEnum.APIH_DUPLICATE_FOUND,
                                "CREATE on the same Network Name is already progress", "", "", MsoLogger.ErrorCode.DataError, "Duplicates request - CREATE on the same Network Name is already progress");
                msoRequest.createRequestRecord (Status.FAILED);
                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.Conflict, "Duplicates request - CREATE on the same Network Name is already progress");
                msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
                return response;
            }
        } else {
            // Check if this request is a duplicate of the one with the same networkId
            InfraActiveRequests dup = null;
            msoLogger.debug ("Checking for a duplicate with the same network-id");
            try {
                dup = RequestsDatabase.checkDuplicateByVnfId (msoRequest.getNetworkInputs ().getNetworkId (),
                                                              msoRequest.getRequestInfo ().getAction ().value (),
                                                              "NETWORK");

            } catch (Exception e) {
                msoLogger.debug ("Exception", e);
                msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.networkbeans.RequestStatusType.FAILED);
                Response response = msoRequest.buildResponseWithError (HttpStatus.SC_INTERNAL_SERVER_ERROR,
                                                                       ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB,
                                                                       null,
                                                                       e.getMessage ());
                alarmLogger.sendAlarm ("MsoDatabaseAccessError",
                                       MsoAlarmLogger.CRITICAL,
                                       Messages.errors.get (ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB));
                msoRequest.createRequestRecord (Status.FAILED);
                msoLogger.error (MessageEnum.APIH_DUPLICATE_CHECK_EXC_ATT, "network-id", "", "", MsoLogger.ErrorCode.DataError, "Exception while checking for a duplicate request with the same network-id", e);
                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, "Exception while checking for a duplicate request with the same network-id");
                msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
                return response;
            }
            if (dup != null) {
                // Found the duplicate record. Return the appropriate error.
                msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.networkbeans.RequestStatusType.FAILED);
                Response response = msoRequest.buildResponse (HttpStatus.SC_CONFLICT,
                                                              ErrorNumbers.LOCKED_SAME_ACTION_AND_VNF_ID,
                                                              dup);
                msoLogger.warn (MessageEnum.APIH_DUPLICATE_FOUND,
                                msoRequest.getRequestInfo ().getAction ().value ()
                                                                  + " on the same Network Id already in progress", "", "", MsoLogger.ErrorCode.DataError, "Duplicated request on the same Network Id already in progress");

                msoRequest.createRequestRecord (Status.FAILED);
                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.Conflict, "Duplicated request on the same Network Id already in progress.");
                msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
                return response;
            }
        }

        String orchestrationURI = "";

        // Query MSO Catalog DB
        try (CatalogDatabase db = new CatalogDatabase()) {
            Recipe recipe = null;

            if (msoRequest.getServiceType () != null
                    && msoRequest.getServiceType ().length () > 0) {
                recipe = db.getNetworkRecipe (msoRequest.getNetworkInputs ().getNetworkType (),
                        msoRequest.getRequestInfo ().getAction ().value (),
                        msoRequest.getServiceType ());

            }
            if (recipe == null) {
                recipe = db.getNetworkRecipe (msoRequest.getNetworkInputs ().getNetworkType (),
                        msoRequest.getRequestInfo ().getAction ().value (),
                        null);
            }

            if (recipe == null) {
                msoLogger.error (MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND, "VNF Recipe", "", "", MsoLogger.ErrorCode.DataError, "VNF Recipe attribute not found");
                msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.networkbeans.RequestStatusType.FAILED);
                Response response = msoRequest.buildResponseWithError (HttpStatus.SC_NOT_FOUND,
                        ErrorNumbers.RECIPE_DOES_NOT_EXIST,
                        null,
                        "");
                msoRequest.createRequestRecord (Status.FAILED);
                db.close ();
                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, "No recipe found in DB");
                msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
                return response;
            }
            orchestrationURI = recipe.getOrchestrationUri ();
            msoLogger.debug ("Orchestration URI is: " + orchestrationURI);

            String requestId = msoRequest.getRequestId ();
            msoLogger.debug ("requestId is: " + requestId);
            msoLogger.debug ("About to insert a record");

            try {
                msoRequest.createRequestRecord (Status.PENDING);
            } catch (Exception e) {
                msoLogger.error (MessageEnum.APIH_DB_ACCESS_EXC_REASON, "Exception while creating record in DB", "", "", MsoLogger.ErrorCode.DataError, "Exception while creating record in DB", e);
                msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.networkbeans.RequestStatusType.FAILED);
                Response response = msoRequest.buildResponseWithError (HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        ErrorNumbers.COULD_NOT_WRITE_TO_REQUESTS_DB,
                        null,
                        "non-unique request-id specified");
                // Cannot create a record of this request here, our communication with MSO DB just failed. Do not try
                // to create a failed record
                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, "Exception while creating record in DB");
                msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
                return response;
            }

            RequestClient requestClient = null;
            HttpResponse response = null;
            long subStartTime = System.currentTimeMillis();
            try {
                requestClient = RequestClientFactory.getRequestClient (orchestrationURI, props);
                // Capture audit event
                msoLogger.debug ("MSO API Handler Posting call to BPEL engine for url: " + requestClient.getUrl ());
                response = requestClient.post (msoRequest.getRequestXML (),
                        requestId,
                        Integer.toString (recipe.getRecipeTimeout ()).toString (),
                        version,
                        null,
                        null);
                msoLogger.recordMetricEvent (subStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from BPMN engine", "BPMN", orchestrationURI, null);
            } catch (Exception e) {
                msoLogger.recordMetricEvent (subStartTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, "Exception while communicate with BPMN engine", "BPMN", orchestrationURI, null);
                msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.networkbeans.RequestStatusType.FAILED);
                Response resp = msoRequest.buildResponseWithError (HttpStatus.SC_BAD_GATEWAY,
                        ErrorNumbers.NO_COMMUNICATION_TO_BPEL,
                        null,
                        e.getMessage ());
                alarmLogger.sendAlarm ("MsoConfigurationError",
                        MsoAlarmLogger.CRITICAL,
                        Messages.errors.get (ErrorNumbers.NO_COMMUNICATION_TO_BPEL));
                msoRequest.updateFinalStatus (Status.FAILED);
                msoLogger.error (MessageEnum.APIH_BPEL_COMMUNICATE_ERROR, "Camunda", "", MsoLogger.ErrorCode.AvailabilityError, "Exception while communicate with BPMN engine", e);
                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, "Exception while communicate with BPMN engine");
                msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity ());
                return resp;
            }

            if (response == null) {
                msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.networkbeans.RequestStatusType.FAILED);
                Response resp = msoRequest.buildResponseWithError (HttpStatus.SC_BAD_GATEWAY,
                        ErrorNumbers.NO_RESPONSE_FROM_BPEL,
                        null,
                        "bpelResponse is null");
                msoRequest.updateFinalStatus (Status.FAILED);
                msoLogger.error (MessageEnum.APIH_BPEL_RESPONSE_ERROR, "Null response from BPEL", "Camunda", "", MsoLogger.ErrorCode.DataError, "bpelResponse is null");
                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.InternalError, "Response from BPMN engine is null");
                msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity ());
                return resp;
            }

            ResponseHandler respHandler = new ResponseHandler (response, requestClient.getType ());
            int bpelStatus = respHandler.getStatus ();

            // BPEL accepted the request, the request is in progress
            if (bpelStatus == HttpStatus.SC_ACCEPTED) {
                String bpelXMLResponseBody = respHandler.getResponseBody ();
                msoLogger.debug ("Received from BPEL: " + bpelXMLResponseBody);
                msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.networkbeans.RequestStatusType.IN_PROGRESS);
                RequestsDatabase.updateInfraStatus (msoRequest.getRequestId (),
                        Status.IN_PROGRESS.toString (),
                        Constants.PROGRESS_REQUEST_IN_PROGRESS,
                        Constants.MODIFIED_BY_APIHANDLER);
                Response resp = msoRequest.buildResponse (bpelStatus, null, null);
                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "BPMN accepted the request, the request is in progress");
                msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity ());
                return resp;
            } else {

                String bpelXMLResponseBody = respHandler.getResponseBody ();
                if (bpelXMLResponseBody != null && !bpelXMLResponseBody.isEmpty ()) {
                    msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.networkbeans.RequestStatusType.FAILED);
                    Response resp = msoRequest.buildResponse (bpelStatus, bpelXMLResponseBody, null);
                    msoRequest.updateFinalStatus (Status.FAILED);
                    msoLogger.error (MessageEnum.APIH_BPEL_RESPONSE_ERROR,
                            "Response from BPEL engine is failed with HTTP Status=" + bpelStatus, "Camunda", "", MsoLogger.ErrorCode.BusinessProcesssError, "Response from BPEL engine is failed with HTTP Status=" + bpelStatus);
                    msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.InternalError, "Response from BPMN engine is with status Failed");
                    msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity ());
                    return resp;
                } else {
                    msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.networkbeans.RequestStatusType.FAILED);
                    Response resp = msoRequest.buildResponse (bpelStatus, ErrorNumbers.ERROR_FROM_BPEL, null);
                    msoRequest.updateFinalStatus (Status.FAILED);
                    msoLogger.error (MessageEnum.APIH_BPEL_RESPONSE_ERROR, "Response from BPEL engine is empty", "Camunda", "", MsoLogger.ErrorCode.BusinessProcesssError, "Response from BPEL engine is empty");
                    msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.InternalError, "Response from BPEL engine is empty");
                    msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity ());
                    return resp;
                }
            }
        } catch (Exception e) {
            msoLogger.error (MessageEnum.APIH_DB_ACCESS_EXC, "", "", MsoLogger.ErrorCode.DataError, "Exception while communciate with Catalog DB", e);
            msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.networkbeans.RequestStatusType.FAILED);
            Response response = msoRequest.buildResponseWithError (HttpStatus.SC_NOT_FOUND,
                                                                   ErrorNumbers.NO_COMMUNICATION_TO_CATALOG_DB,
                                                                   null,
                                                                   e.getMessage ());
            alarmLogger.sendAlarm ("MsoDatabaseAccessError",
                                   MsoAlarmLogger.CRITICAL,
                                   Messages.errors.get (ErrorNumbers.NO_COMMUNICATION_TO_CATALOG_DB));
            msoRequest.createRequestRecord (Status.FAILED);
            msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, "Exception while communciate with DB");
            msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
            return response;
        }
    }

    private Response manageVolumeRequestImpl (String reqXML, String version) {
    	String methodName = "VolumeRequest";
    	props = loadMsoProperties ();
       
        long startTime = System.currentTimeMillis ();
        if (noProperties) {
            msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.ServiceNotAvailable, "Application not started, properties file missing or invalid");
        	return NOT_STARTED_RESPONSE;
        }

        uriInfo.getRequestUri ();

        // Generate unique request id for the new request
        UUID requestUUID = UUID.randomUUID ();

        VolumeMsoInfraRequest msoRequest = new VolumeMsoInfraRequest (requestUUID.toString ());

        if (reqXML == null) {
            msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.BadRequest, "The input request is null");
            return Response.status (HttpStatus.SC_NO_CONTENT).entity ("").build ();
        }

        String requestUri = uriInfo.getRequestUri ().toString ();

        msoLogger.debug ("Incoming Request: " + reqXML);

        msoRequest.setRequestUri (requestUri);
       
        msoLogger.debug ("Schema version: " + version);
        try {
            msoRequest.parse (reqXML, version, props);
        } catch (Exception e) {
            msoLogger.debug ("Validation failed: ", e);
            msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.volumebeans.RequestStatusType.FAILED);
            Response response = msoRequest.buildResponseFailedValidation (HttpStatus.SC_BAD_REQUEST, e.getMessage ());
            if (msoRequest.getRequestId () != null) {
                msoLogger.debug ("Logging failed message to the database");
                msoRequest.createRequestRecord (Status.FAILED);
            }
            msoLogger.error (MessageEnum.APIH_REQUEST_VALIDATION_ERROR, reqXML, "", "", MsoLogger.ErrorCode.DataError, "Exception when parsing reqXML", e);
            msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.SchemaError, "Validation of the input request failed");
            msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
            return response;
        }
        MsoLogger.setServiceName (MsoLogger.getServiceName () + "_" + msoRequest.getRequestInfo ().getAction ().name ());
        msoLogger.debug ("Update serviceName with detailed action info to:" + MsoLogger.getServiceName () + "_" + msoRequest.getRequestInfo ().getAction ().name ());
        if (msoRequest.getRequestInfo ()
                      .getAction () == org.openecomp.mso.apihandlerinfra.volumebeans.ActionType.CREATE) {
            // Check if this request is a duplicate of the one with the same network Name
            msoLogger.debug ("Checking for a duplicate with the same volume-name");
            InfraActiveRequests dup = null;
            try {

                dup = RequestsDatabase.checkDuplicateByVnfName (msoRequest.getVolumeInputs ().getVolumeGroupName (),
                                                                msoRequest.getRequestInfo ().getAction ().value (),
                                                                "VOLUME");

            } catch (Exception e) {
                msoLogger.debug ("Exception", e);
                msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.volumebeans.RequestStatusType.FAILED);
                Response response = msoRequest.buildResponseWithError (HttpStatus.SC_INTERNAL_SERVER_ERROR,
                                                                       ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB,
                                                                       null,
                                                                       e.getMessage ());
                alarmLogger.sendAlarm ("MsoDatabaseAccessError",
                                       MsoAlarmLogger.CRITICAL,
                                       Messages.errors.get (ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB));
                msoRequest.createRequestRecord (Status.FAILED);
                msoLogger.error (MessageEnum.APIH_DUPLICATE_CHECK_EXC_ATT, "volume-group-name", "", "", MsoLogger.ErrorCode.DataError, "Exception while checking for duplicated request", e);
                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, "Exception while checking for duplicated request");
                msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
                return response;
            }
            if (dup != null) {
                // Found the duplicate record. Return the appropriate error.
                msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.volumebeans.RequestStatusType.FAILED);
                Response response = msoRequest.buildResponse (HttpStatus.SC_CONFLICT,
                                                              ErrorNumbers.LOCKED_CREATE_ON_THE_SAME_VNF_NAME_IN_PROGRESS,
                                                              dup);
                msoLogger.warn (MessageEnum.APIH_DUPLICATE_FOUND,
                                "CREATE on the same Volume Group Name is already progress", "", "", MsoLogger.ErrorCode.DataError, "Duplicates request - CREATE on the same Volume Group Name is already progress");
                msoRequest.createRequestRecord (Status.FAILED);
                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.Conflict, "Duplicates request - CREATE on the same Volume Group Name is already progress");
                msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
                return response;
            }
        } else {
            // Check if this request is a duplicate of the one with the same volumeGroupId
            InfraActiveRequests dup = null;
            msoLogger.debug ("Checking for a duplicate with the same volume-group-id");
            try {
                dup = RequestsDatabase.checkDuplicateByVnfId (msoRequest.getVolumeInputs ().getVolumeGroupId (),
                                                              msoRequest.getRequestInfo ().getAction ().value (),
                                                              "VOLUME");

            } catch (Exception e) {
                msoLogger.debug ("Exception", e);
                msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.volumebeans.RequestStatusType.FAILED);
                Response response = msoRequest.buildResponseWithError (HttpStatus.SC_INTERNAL_SERVER_ERROR,
                                                                       ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB,
                                                                       null,
                                                                       e.getMessage ());
                alarmLogger.sendAlarm ("MsoDatabaseAccessError",
                                       MsoAlarmLogger.CRITICAL,
                                       Messages.errors.get (ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB));
                msoRequest.createRequestRecord (Status.FAILED);
                msoLogger.error (MessageEnum.APIH_DUPLICATE_CHECK_EXC_ATT, "volume-group-id", "", "", MsoLogger.ErrorCode.DataError, "Exception while checking for a duplicate request with the sam volume-group-id", e);
                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, "Exception while checking for a duplicate request with the sam volume-group-id");
                msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
                return response;
            }
            if (dup != null) {
                // Found the duplicate record. Return the appropriate error.
                msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.volumebeans.RequestStatusType.FAILED);
                Response response = msoRequest.buildResponse (HttpStatus.SC_CONFLICT,
                                                              ErrorNumbers.LOCKED_SAME_ACTION_AND_VNF_ID,
                                                              dup);
                msoLogger.warn (MessageEnum.APIH_DUPLICATE_FOUND,
                                msoRequest.getRequestInfo ().getAction ().value ()
                                                                  + " on the same Volume Group Id already in progress", "", "", MsoLogger.ErrorCode.DataError, "Duplicated request on the same Volume Group Id already in progress");
                msoRequest.createRequestRecord (Status.FAILED);
                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.Conflict, "Duplicated request on the same Volume Group Id already in progress");
                msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
                return response;
            }
        }

        String orchestrationURI = "";

        // Query MSO Catalog DB
        try (CatalogDatabase db = new CatalogDatabase()) {

            Recipe recipe = null;

            if (version.equals(Constants.SCHEMA_VERSION_V1)) {
                if (msoRequest.getServiceType () != null
                        && msoRequest.getServiceType ().length () > 0) {
                    recipe = db.getVnfComponentsRecipe (msoRequest.getVolumeInputs ().getVnfType (),
                            Constants.VOLUME_GROUP_COMPONENT_TYPE,
                            msoRequest.getRequestInfo ().getAction ().value (),
                            msoRequest.getServiceType ());
                }
                if (recipe == null) {
                    recipe = db.getVnfComponentsRecipe (msoRequest.getVolumeInputs ().getVnfType (),
                            Constants.VOLUME_GROUP_COMPONENT_TYPE,
                            msoRequest.getRequestInfo ().getAction ().value (),
                            null);
                    // If no recipe for the vnf type is found, look for generic recipe with "*" vnf type
                    if (recipe == null) {
                        recipe = db.getVnfComponentsRecipe (Constants.VNF_TYPE_WILDCARD,
                                Constants.VOLUME_GROUP_COMPONENT_TYPE,
                                msoRequest.getRequestInfo ().getAction ().value (),
                                null);
                    }
                }
            }
            else if (version.equals (Constants.SCHEMA_VERSION_V2) || version.equals (Constants.SCHEMA_VERSION_V3)) {
                switch (msoRequest.getRequestInfo ().getAction ()) {
                    case CREATE:
                    case UPDATE:
                    case DELETE:
                        // First get recipe for the vnf type given
                        recipe = db.getVnfComponentsRecipe (msoRequest.getVolumeInputs ().getVnfType (),
                                Constants.VOLUME_GROUP_COMPONENT_TYPE,
                                msoRequest.getRequestInfo ().getAction ().value (), null);

                        // If no recipe for the vnf type is found, look for generic recipe with "*" vnf type
                        if (recipe == null) {
                            recipe = db.getVnfComponentsRecipe (Constants.VNF_TYPE_WILDCARD,
                                    Constants.VOLUME_GROUP_COMPONENT_TYPE,
                                    msoRequest.getRequestInfo ().getAction ().value (), null);
                        }
                        break;
                    case CREATE_VF_MODULE_VOL:
                    case UPDATE_VF_MODULE_VOL:
                    case DELETE_VF_MODULE_VOL:
                        // First get recipe for the vnf type given
                        recipe = db.getVnfComponentsRecipe (msoRequest.getVolumeInputs ().getVnfType (),
                                Constants.VOLUME_GROUP_COMPONENT_TYPE,
                                msoRequest.getRequestInfo ().getAction ().value (), null);

                        // If no recipe for the vnf type is found, look for generic recipe with "*" in vf module id
                        if (recipe == null) {
                            recipe = db.getVnfComponentsRecipeByVfModuleId (Constants.VNF_TYPE_WILDCARD,
                                    Constants.VOLUME_GROUP_COMPONENT_TYPE,
                                    msoRequest.getRequestInfo ().getAction ().value ());
                        }
                        break;
                    default:
                        break;
                }

            }

            if (recipe == null) {
                msoLogger.error (MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND, "VNF Recipe", "", "", MsoLogger.ErrorCode.DataError, "VNF Recipe not found in DB");
                msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.volumebeans.RequestStatusType.FAILED);
                Response response = msoRequest.buildResponseWithError (HttpStatus.SC_NOT_FOUND,
                        ErrorNumbers.RECIPE_DOES_NOT_EXIST,
                        null,
                        "");
                msoRequest.createRequestRecord (Status.FAILED);
                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, "VNF Recipe not found in DB");
                msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
                return response;
            }
            orchestrationURI = recipe.getOrchestrationUri ();
            msoLogger.debug ("Orchestration URI is: " + orchestrationURI);

            String requestId = msoRequest.getRequestId ();
            msoLogger.debug ("requestId is: " + requestId);
            msoLogger.debug ("About to insert a record");

            try {
                msoRequest.createRequestRecord (Status.PENDING);
            } catch (Exception e) {
                msoLogger.error (MessageEnum.APIH_DB_ACCESS_EXC_REASON, "Exception while creating record in DB", "", "", MsoLogger.ErrorCode.AvailabilityError, "Exception in createRequestRecord", e);
                msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.volumebeans.RequestStatusType.FAILED);
                Response response = msoRequest.buildResponseWithError (HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        ErrorNumbers.COULD_NOT_WRITE_TO_REQUESTS_DB,
                        null,
                        "non-unique request-id specified");
                // Cannot create a record of this request here, our communication with MSO DB just failed. Do not try
                // to create a failed record
                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, "Exception while creating record in DB");
                msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
                return response;
            }

            RequestClient requestClient = null;
            HttpResponse response = null;
            long subStartTime = System.currentTimeMillis();
            try {
                requestClient = RequestClientFactory.getRequestClient (orchestrationURI, props);
                // Capture audit event
                msoLogger.debug ("MSO API Handler Posting call to BPEL engine for url: " + requestClient.getUrl ());
                response = requestClient.post (msoRequest.getRequestXML (),
                        requestId,
                        Integer.toString (recipe.getRecipeTimeout ()).toString (),
                        version,
                        null,
                        null);
                msoLogger.recordMetricEvent (subStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from BPMN engine", "BPMN", orchestrationURI, null);
            } catch (Exception e) {
                msoLogger.recordMetricEvent (subStartTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, "Exception while communicate with BPMN engine", "BPMN", orchestrationURI, null);
                msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.volumebeans.RequestStatusType.FAILED);
                Response resp = msoRequest.buildResponseWithError (HttpStatus.SC_BAD_GATEWAY,
                        ErrorNumbers.NO_COMMUNICATION_TO_BPEL,
                        null,
                        e.getMessage ());
                alarmLogger.sendAlarm ("MsoConfigurationError",
                        MsoAlarmLogger.CRITICAL,
                        Messages.errors.get (ErrorNumbers.NO_COMMUNICATION_TO_BPEL));
                msoRequest.updateFinalStatus (Status.FAILED);
                msoLogger.error (MessageEnum.APIH_BPEL_COMMUNICATE_ERROR, "Camunda", "", MsoLogger.ErrorCode.AvailabilityError, "Exception while communicate with BPMN engine", e);
                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, "Exception while communicate with BPMN engine");
                msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity ());
                return resp;
            }

            if (response == null) {
                msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.volumebeans.RequestStatusType.FAILED);
                Response resp = msoRequest.buildResponseWithError (HttpStatus.SC_BAD_GATEWAY,
                        ErrorNumbers.NO_RESPONSE_FROM_BPEL,
                        null,
                        "bpelResponse is null");
                msoRequest.updateFinalStatus (Status.FAILED);
                msoLogger.error (MessageEnum.APIH_BPEL_RESPONSE_ERROR, "Null response from BPEL", "Camunda", "", MsoLogger.ErrorCode.DataError, "Null response from BPMN engine");
                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.InternalError, "Null response from BPMN engine");
                msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity ());
                return resp;
            }

            ResponseHandler respHandler = new ResponseHandler (response, requestClient.getType ());
            int bpelStatus = respHandler.getStatus ();

            // BPEL accepted the request, the request is in progress
            if (bpelStatus == HttpStatus.SC_ACCEPTED) {
                String bpelXMLResponseBody = respHandler.getResponseBody ();
                msoLogger.debug ("Received from BPEL: " + bpelXMLResponseBody);
                msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.volumebeans.RequestStatusType.IN_PROGRESS);
                RequestsDatabase.updateInfraStatus (msoRequest.getRequestId (),
                        Status.IN_PROGRESS.toString (),
                        Constants.PROGRESS_REQUEST_IN_PROGRESS,
                        Constants.MODIFIED_BY_APIHANDLER);
                Response resp = msoRequest.buildResponse (bpelStatus, null, null);
                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "BPMN accepted the request, the request is in progress");
                msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity ());
                return resp;
            } else {

                String bpelXMLResponseBody = respHandler.getResponseBody ();
                if (bpelXMLResponseBody != null && !bpelXMLResponseBody.isEmpty ()) {
                    msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.volumebeans.RequestStatusType.FAILED);
                    Response resp = msoRequest.buildResponse (bpelStatus, bpelXMLResponseBody, null);
                    msoRequest.updateFinalStatus (Status.FAILED);
                    msoLogger.error (MessageEnum.APIH_BPEL_RESPONSE_ERROR,
                            "Response from BPEL engine is failed with HTTP Status=" + bpelStatus, "Camunda", "", MsoLogger.ErrorCode.DataError, "Response from BPEL engine is failed with HTTP Status=" + bpelStatus);
                    msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.InternalError, "Response from BPMN engine is with status Failed");
                    msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity ());
                    return resp;
                } else {
                    msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.volumebeans.RequestStatusType.FAILED);
                    Response resp = msoRequest.buildResponse (bpelStatus, ErrorNumbers.ERROR_FROM_BPEL, null);
                    msoRequest.updateFinalStatus (Status.FAILED);
                    msoLogger.error (MessageEnum.APIH_BPEL_RESPONSE_ERROR, "Response from BPEL engine is empty", "Camunda", "", MsoLogger.ErrorCode.DataError, "Response from BPEL engine is empty");
                    msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.InternalError, "Response from BPMN engine is empty");
                    msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity ());
                    return resp;
                }
            }
        } catch (Exception e) {
            msoLogger.error (MessageEnum.APIH_DB_ACCESS_EXC, "", "", MsoLogger.ErrorCode.DataError, "Exception while communciate with Catalog DB", e);
            msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.volumebeans.RequestStatusType.FAILED);
            Response response = msoRequest.buildResponseWithError (HttpStatus.SC_NOT_FOUND,
                                                                   ErrorNumbers.NO_COMMUNICATION_TO_CATALOG_DB,
                                                                   null,
                                                                   e.getMessage ());
            alarmLogger.sendAlarm ("MsoDatabaseAccessError",
                                   MsoAlarmLogger.CRITICAL,
                                   Messages.errors.get (ErrorNumbers.NO_COMMUNICATION_TO_CATALOG_DB));
            msoRequest.createRequestRecord (Status.FAILED);
            msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, "Exception while communciate with DB");
            msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
            return response;
        }
    }
    
    private Request mapInfraActiveRequestToRequest(InfraActiveRequests requestDB)  {
    	
  	  
        Request request = new Request();
        
        ObjectMapper mapper = new ObjectMapper();
       // mapper.configure(Feature.WRAP_ROOT_VALUE, true);
       
       request.setRequestId(requestDB.getRequestId());
       request.setRequestScope(requestDB.getRequestScope());
       request.setRequestType(requestDB.getRequestAction());
       
       InstanceReferences ir = new InstanceReferences();
       if(requestDB.getNetworkId() != null)
       	ir.setNetworkInstanceId(requestDB.getNetworkId());
       if(requestDB.getNetworkName() != null)
       	ir.setNetworkInstanceName(requestDB.getNetworkName());
       if(requestDB.getServiceInstanceId() != null)
       	ir.setServiceInstanceId(requestDB.getServiceInstanceId());
       if(requestDB.getServiceInstanceName() != null)
       	ir.setServiceInstanceName(requestDB.getServiceInstanceName());
       if(requestDB.getVfModuleId() != null)
       	ir.setVfModuleInstanceId(requestDB.getVfModuleId());
       if(requestDB.getVfModuleName() != null)
       	ir.setVfModuleInstanceName(requestDB.getVfModuleName());
       if(requestDB.getVnfId() != null)
       	ir.setVnfInstanceId(requestDB.getVnfId());
       if(requestDB.getVnfName() != null)
       	ir.setVnfInstanceName(requestDB.getVnfName());
       if(requestDB.getVolumeGroupId() != null)
       	ir.setVolumeGroupInstanceId(requestDB.getVolumeGroupId());
       if(requestDB.getVolumeGroupName() != null)
       	ir.setVolumeGroupInstanceName(requestDB.getVolumeGroupName());

       
       request.setInstanceReferences(ir);
       
       String requestBody = requestDB.getRequestBody();
              
       RequestDetails requestDetails = null;
       
       try{
       	requestDetails = mapper.readValue(requestBody, RequestDetails.class);
       	
       }catch(Exception e){
       	msoLogger.debug("Exception caught mapping requestBody to RequestDetails");
       }
               
       request.setRequestDetails(requestDetails);
       String startTimeStamp = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss").format(requestDB.getStartTime()) + " GMT";
       request.setStartTime(startTimeStamp);
       
       RequestStatus status = new RequestStatus();
       if(requestDB.getStatusMessage() != null){
    	   status.setStatusMessage(requestDB.getStatusMessage());
       }
       
       if(requestDB.getEndTime() != null){
    	   String endTimeStamp = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss").format(requestDB.getEndTime()) + " GMT";
    	   status.setFinishTime(endTimeStamp);
       }

        
       if(requestDB.getRequestStatus() != null){
    	   status.setRequestState(requestDB.getRequestStatus());
       }
       
       if(requestDB.getProgress() != null){
    	   status.setPercentProgress(requestDB.getProgress().intValue());
       }
       
       request.setRequestStatus(status);
       
       return request;
   }
    
    private RecipeLookupResult getServiceInstanceOrchestrationURI (CatalogDatabase db, MsoRequest msoRequest, Action action) throws Exception {

    	RecipeLookupResult recipeLookupResult = null;
        // Query MSO Catalog DB
               
        if (msoRequest.getModelInfo().getModelType().equals(ModelType.service)) {
        	
        // SERVICE REQUEST
        	// Construct the default service name
            // TODO need to make this a configurable property
            
            String defaultServiceName = msoRequest.getRequestInfo().getSource() + "_DEFAULT";
        	Service serviceRecord = db.getServiceByName(defaultServiceName);
        	int serviceId = serviceRecord.getId();
        	ServiceRecipe recipe = db.getServiceRecipe(serviceId, action.name());
        	
        	if (recipe == null) {                 
                return null;
            }	
        	
        	recipeLookupResult = new RecipeLookupResult (recipe.getOrchestrationUri (), recipe.getRecipeTimeout ());        	
        }
        else if (msoRequest.getModelInfo().getModelType().equals(ModelType.vfModule) ||
        		msoRequest.getModelInfo().getModelType().equals(ModelType.volumeGroup)) {
        	
        	String vnfComponentType = msoRequest.getModelInfo().getModelType().name();
        	VnfComponentsRecipe recipe = null;
        	
        	if (action != Action.deleteInstance) {
	        	RelatedInstanceList[] instanceList = null;
	        	if (msoRequest.getServiceInstancesRequest().getRequestDetails() != null) {
	        		instanceList = msoRequest.getServiceInstancesRequest().getRequestDetails().getRelatedInstanceList();
	        	}
	         	
	         	String serviceModelName = null;
	         	String vnfModelName = null;
	         	String vfModuleModelName = null;
	         	String asdcServiceModelVersion = null;
	         	String modelVersion = null;
	         	
	         	if (instanceList != null) {
	          	
		          	for(RelatedInstanceList relatedInstanceList : instanceList){
		          		
		          		RelatedInstance relatedInstance = relatedInstanceList.getRelatedInstance();
		          		if(relatedInstance.getModelInfo().getModelType().equals(ModelType.service)){
		          			serviceModelName = relatedInstance.getModelInfo().getModelName();
		          			asdcServiceModelVersion = relatedInstance.getModelInfo().getModelVersion();
		          		}
		          		
		          		if(relatedInstance.getModelInfo().getModelType().equals(ModelType.vnf)){
		          			vnfModelName = relatedInstance.getModelInfo().getModelCustomizationName();
		          		}
		          		
		          		if(relatedInstance.getModelInfo().getModelType().equals(ModelType.vfModule) ||
		          				relatedInstance.getModelInfo().getModelType().equals(ModelType.volumeGroup)) {
		          			vfModuleModelName = relatedInstance.getModelInfo().getModelName();
		          			modelVersion = relatedInstance.getModelInfo().getModelVersion();
		          		}          		
		          	}
	         	}
	          	
	          	String vnfType = serviceModelName + "/" + vnfModelName;
	          	
	          	// Try to find a recipe for a custom flow first
	        	recipe = db.getVnfComponentsRecipe(vnfType, vfModuleModelName, asdcServiceModelVersion, modelVersion, action.name());
        	}
        	
        	if (recipe == null) {
        		// Find the default recipe record
        		recipe = db.getVnfComponentsRecipeByVfModuleId("VID_DEFAULT", vnfComponentType, action.name());
        		
        		if (recipe == null) {        			   
        			return null;
        		}
            }	        	
        	recipeLookupResult = new RecipeLookupResult (recipe.getOrchestrationUri (), recipe.getRecipeTimeout ());         	
          	
        }
        else if (msoRequest.getModelInfo().getModelType().equals(ModelType.vnf)) {
        	// VNF REQUEST
        	// Construct the default vnf type
            // TODO need to make this a configurable property
            
            String defaultVnfType = msoRequest.getRequestInfo().getSource() + "_DEFAULT";
        	
        	VnfRecipe recipe = db.getVnfRecipe(defaultVnfType, action.name());
        	
        	if (recipe == null) {               
                return null;
            }	        	
        	recipeLookupResult = new RecipeLookupResult (recipe.getOrchestrationUri (), recipe.getRecipeTimeout ());       	
        }
        else if (msoRequest.getModelInfo().getModelType().equals(ModelType.network)) {
        	// NETWORK REQUEST
        	// Construct the default network type
            // TODO need to make this a configurable property
            
            String defaultNetworkType = msoRequest.getRequestInfo().getSource() + "_DEFAULT";
        	
        	Recipe recipe = db.getNetworkRecipe(defaultNetworkType, action.name());
        	
        	if (recipe == null) {                
                return null;
            }	        	
        	recipeLookupResult = new RecipeLookupResult (recipe.getOrchestrationUri (), recipe.getRecipeTimeout ());   
        }       
        
        if (recipeLookupResult != null) {
        	msoLogger.debug ("Orchestration URI is: " + recipeLookupResult.getOrchestrationURI() + ", recipe Timeout is: " + Integer.toString(recipeLookupResult.getRecipeTimeout ()));
        }
        else {
        	msoLogger.debug("No matching recipe record found");
        }
        return recipeLookupResult;
    }
}