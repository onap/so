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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.http.HttpStatus;
import org.codehaus.jackson.map.ObjectMapper;
import org.openecomp.mso.apihandler.common.ErrorNumbers;
import org.openecomp.mso.apihandler.common.ValidationException;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.*;
import org.openecomp.mso.apihandlerinfra.utils.JsonUtils;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoAlarmLogger;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.requestsdb.InfraActiveRequests;
import org.openecomp.mso.requestsdb.RequestsDatabase;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Path("/")
@Api(value = "/", description = "API Requests for Orchestration requests")
public class OrchestrationRequests {

	public static final String MSO_PROP_APIHANDLER_INFRA = "MSO_PROP_APIHANDLER_INFRA";

	private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.APIH);

	private static MsoAlarmLogger alarmLogger = new MsoAlarmLogger();

	private RequestsDatabase requestsDB = RequestsDatabase.getInstance();

	/**
	 *
	 */
	public OrchestrationRequests() {
		// TODO Auto-generated constructor stub
	}

	@GET
	@Path("orchestrationRequests/{version:[vV][2-5]}/{requestId}")
	@ApiOperation(value = "Find Orchestrated Requests for a given requestId", response = Response.class)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOrchestrationRequest(@PathParam("requestId") String requestId,
			@PathParam("version") String version) {

		GetOrchestrationResponse orchestrationResponse = new GetOrchestrationResponse();

		MsoRequest msoRequest = new MsoRequest(requestId);

		long startTime = System.currentTimeMillis();

		InfraActiveRequests requestDB = null;

		try {
			requestDB = requestsDB.getRequestFromInfraActive(requestId);

		} catch (Exception e) {
			msoLogger.error(MessageEnum.APIH_DB_ACCESS_EXC, MSO_PROP_APIHANDLER_INFRA, "", "",
					MsoLogger.ErrorCode.AvailabilityError,
					"Exception while communciate with Request DB - Infra Request Lookup", e);
			msoRequest.setStatus(org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
			Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_NOT_FOUND,
					MsoException.ServiceException, e.getMessage(), ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB, null);
			alarmLogger.sendAlarm("MsoDatabaseAccessError", MsoAlarmLogger.CRITICAL,
					Messages.errors.get(ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB));
			msoLogger.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError,
					"Exception while communciate with Request DB");
			msoLogger.debug("End of the transaction, the final response is: " + (String) response.getEntity());
			return response;

		}

		if (requestDB == null) {
			Response resp = msoRequest.buildServiceErrorResponse(HttpStatus.SC_NO_CONTENT,
					MsoException.ServiceException, "Orchestration RequestId " + requestId + " is not found in DB",
					ErrorNumbers.SVC_DETAILED_SERVICE_ERROR, null);
			msoLogger.error(MessageEnum.APIH_BPEL_COMMUNICATE_ERROR, MSO_PROP_APIHANDLER_INFRA, "", "",
					MsoLogger.ErrorCode.BusinessProcesssError,
					"Null response from RequestDB when searching by RequestId");
			msoLogger.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound,
					"Null response from RequestDB when searching by RequestId");
			msoLogger.debug("End of the transaction, the final response is: " + (String) resp.getEntity());
			return resp;

		}

		Request request = mapInfraActiveRequestToRequest(requestDB);

		orchestrationResponse.setRequest(request);

		return Response.status(200).entity(orchestrationResponse).build();
	}

	@GET
	@Path("orchestrationRequests/{version:[vV][2-5]}")
	@ApiOperation(value = "Find Orchestrated Requests for a URI Information", response = Response.class)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOrchestrationRequest(@Context UriInfo ui, @PathParam("version") String version) {

		long startTime = System.currentTimeMillis();

		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
		List<InfraActiveRequests> activeRequests;
		GetOrchestrationListResponse orchestrationList;

		try {

			String queryParam;
			Map<String, List<String>> orchestrationFilterParams = new HashMap<>();


			for (Map.Entry<String,List<String>> entry : queryParams.entrySet()) {
                queryParam = entry.getKey();

                try{
                    if("filter".equalsIgnoreCase(queryParam)){
                        for(String value : entry.getValue()) {
                            StringTokenizer st = new StringTokenizer(value, ":");

                            int counter=0;
                            String mapKey=null;
                            List<String> orchestrationList1 = new ArrayList<>();
                            while (st.hasMoreElements()) {
                                if(counter == 0){
                                    mapKey = st.nextElement() + "";
                                } else{
                                    orchestrationList1.add(st.nextElement() + "");
                                }
                               counter++;
                          }
                            orchestrationFilterParams.put(mapKey, orchestrationList1);
                        }
                    }

                }catch(Exception e){
                    throw new ValidationException("QueryParam ServiceInfo", e);
                }
            }

			activeRequests = requestsDB.getOrchestrationFiltersFromInfraActive(orchestrationFilterParams);

			orchestrationList = new GetOrchestrationListResponse();

			List<RequestList> requestLists = new ArrayList<>();

			for (InfraActiveRequests infraActive : activeRequests) {

				Request request = mapInfraActiveRequestToRequest(infraActive);
				RequestList requestList = new RequestList();
				requestList.setRequest(request);

				requestLists.add(requestList);

			}

			orchestrationList.setRequestList(requestLists);

		} catch (Exception e) {
			msoLogger.debug("Get Orchestration Request with Filters Failed : ", e);
			Response response = buildServiceErrorResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR,
					MsoException.ServiceException, "Get Orchestration Request with Filters Failed.  " + e.getMessage(),
					ErrorNumbers.SVC_GENERAL_SERVICE_ERROR);
			msoLogger.error(MessageEnum.APIH_GENERAL_EXCEPTION, MSO_PROP_APIHANDLER_INFRA, "", "",
					MsoLogger.ErrorCode.BusinessProcesssError, "Get Orchestration Request with Filters Failed : " + e);
			msoLogger.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataError,
					"Get Orchestration Request with Filters Failed");
			msoLogger.debug("End of the transaction, the final response is: " + (String) response.getEntity());
			return response;
		}

		return Response.status(200).entity(orchestrationList).build();
	}

	@POST
	@Path("orchestrationRequests/{version: [vV][3-5]}/{requestId}/unlock")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Unlock Orchestrated Requests for a given requestId", response = Response.class)
	public Response unlockOrchestrationRequest(String requestJSON, @PathParam("requestId") String requestId,
			@PathParam("version") String version) {

		MsoRequest msoRequest = new MsoRequest(requestId);

		long startTime = System.currentTimeMillis();
		msoLogger.debug("requestId is: " + requestId);

		InfraActiveRequests requestDB = null;
		Request request = null;

		msoLogger.debug("requestId is: " + requestId);
		ServiceInstancesRequest sir = null;

		try {
			ObjectMapper mapper = new ObjectMapper();
			sir = mapper.readValue(requestJSON, ServiceInstancesRequest.class);

		} catch (Exception e) {
			msoLogger.debug("Mapping of request to JSON object failed : ", e);
			Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_REQUEST,
					MsoException.ServiceException, "Mapping of request to JSON object failed.  " + e.getMessage(),
					ErrorNumbers.SVC_BAD_PARAMETER, null);
			if (msoRequest.getRequestId() != null) {
				msoLogger.debug("Mapping of request to JSON object failed");
			}
			msoLogger.error(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, MSO_PROP_APIHANDLER_INFRA, "", "",
					MsoLogger.ErrorCode.SchemaError, requestJSON, e);
			msoLogger.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.SchemaError,
					"Mapping of request to JSON object failed");
			msoLogger.debug("End of the transaction, the final response is: " + (String) response.getEntity());
			return response;
		}

		try {
			msoRequest.parseOrchestration(sir);
		} catch (Exception e) {
			msoLogger.debug("Validation failed: ", e);
			Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_REQUEST,
					MsoException.ServiceException, "Error parsing request.  " + e.getMessage(),
					ErrorNumbers.SVC_BAD_PARAMETER, null);
			if (msoRequest.getRequestId() != null) {
				msoLogger.debug("Logging failed message to the database");
			}
			msoLogger.error(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, MSO_PROP_APIHANDLER_INFRA, "", "",
					MsoLogger.ErrorCode.SchemaError, requestJSON, e);
			msoLogger.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.SchemaError,
					"Validation of the input request failed");
			msoLogger.debug("End of the transaction, the final response is: " + (String) response.getEntity());
			return response;
		}

		try {
			requestDB = requestsDB.getRequestFromInfraActive(requestId);

			if (requestDB == null) {
				Response resp = msoRequest.buildServiceErrorResponse(HttpStatus.SC_NOT_FOUND,
						MsoException.ServiceException, "Orchestration RequestId " + requestId + " is not found in DB",
						ErrorNumbers.SVC_DETAILED_SERVICE_ERROR, null);
				msoLogger.error(MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND, MSO_PROP_APIHANDLER_INFRA, "", "",
						MsoLogger.ErrorCode.BusinessProcesssError,
						"Null response from RequestDB when searching by RequestId");
				msoLogger.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound,
						"Null response from RequestDB when searching by RequestId");
				msoLogger.debug("End of the transaction, the final response is: " + (String) resp.getEntity());
				return resp;

			} else {
				request = mapInfraActiveRequestToRequest(requestDB);
				RequestStatus reqStatus = request.getRequestStatus();
				Status status = Status.valueOf(reqStatus.getRequestState());
				if (status == Status.IN_PROGRESS || status == Status.PENDING) {
					msoRequest.setStatus(org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.UNLOCKED);
					reqStatus.setRequestState(Status.UNLOCKED.toString());
					requestsDB.updateInfraStatus(requestId, Status.UNLOCKED.toString(),
							Constants.MODIFIED_BY_APIHANDLER);

					msoLogger.recordAuditEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc,
							"RequestId " + requestId + " has been unlocked");

				} else {
					Response resp = msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_REQUEST,
							MsoException.ServiceException, "Orchestration RequestId " + requestId + " has a status of "
									+ status + " and can not be unlocked",
							ErrorNumbers.SVC_DETAILED_SERVICE_ERROR, null);
					msoLogger.error(MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND, MSO_PROP_APIHANDLER_INFRA, "", "",
							MsoLogger.ErrorCode.DataError, "Orchestration RequestId " + requestId + " has a status of "
									+ status + " and can not be unlocked");
					msoLogger.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataError,
							"Orchestration RequestId " + requestId + " has a status of " + status
									+ " and can not be unlocked");
					msoLogger.debug("End of the transaction, the final response is: " + (String) resp.getEntity());
					return resp;
				}
			}
		} catch (Exception e) {
			msoLogger.error(MessageEnum.APIH_DB_ACCESS_EXC, MSO_PROP_APIHANDLER_INFRA, "", "",
					MsoLogger.ErrorCode.AvailabilityError,
					"Exception while communciate with Request DB - Infra Request Lookup", e);
			msoRequest.setStatus(org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
			Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_NOT_FOUND,
					MsoException.ServiceException, e.getMessage(), ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB, null);
			alarmLogger.sendAlarm("MsoDatabaseAccessError", MsoAlarmLogger.CRITICAL,
					Messages.errors.get(ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB));
			msoLogger.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError,
					"Exception while communciate with Request DB");
			msoLogger.debug("End of the transaction, the final response is: " + (String) response.getEntity());
			return response;

		}

		return Response.status(HttpStatus.SC_NO_CONTENT).entity("").build();
	}

	private Request mapInfraActiveRequestToRequest(InfraActiveRequests requestDB) {

		Request request = new Request();

		ObjectMapper mapper = new ObjectMapper();

		request.setRequestId(requestDB.getRequestId());
		request.setRequestScope(requestDB.getRequestScope());
		request.setRequestType(requestDB.getRequestAction());

		InstanceReferences ir = new InstanceReferences();
		if (requestDB.getNetworkId() != null)
			ir.setNetworkInstanceId(requestDB.getNetworkId());
		if (requestDB.getNetworkName() != null)
			ir.setNetworkInstanceName(requestDB.getNetworkName());
		if (requestDB.getServiceInstanceId() != null)
			ir.setServiceInstanceId(requestDB.getServiceInstanceId());
		if (requestDB.getServiceInstanceName() != null)
			ir.setServiceInstanceName(requestDB.getServiceInstanceName());
		if (requestDB.getVfModuleId() != null)
			ir.setVfModuleInstanceId(requestDB.getVfModuleId());
		if (requestDB.getVfModuleName() != null)
			ir.setVfModuleInstanceName(requestDB.getVfModuleName());
		if (requestDB.getVnfId() != null)
			ir.setVnfInstanceId(requestDB.getVnfId());
		if (requestDB.getVnfName() != null)
			ir.setVnfInstanceName(requestDB.getVnfName());
		if (requestDB.getVolumeGroupId() != null)
			ir.setVolumeGroupInstanceId(requestDB.getVolumeGroupId());
		if (requestDB.getVolumeGroupName() != null)
			ir.setVolumeGroupInstanceName(requestDB.getVolumeGroupName());
		if (requestDB.getRequestorId() != null)
			ir.setRequestorId(requestDB.getRequestorId());

		request.setInstanceReferences(ir);

		String requestBody = requestDB.getRequestBody();

		RequestDetails requestDetails = null;

		try {
			requestDetails = mapper.readValue(requestBody, RequestDetails.class);

		} catch (Exception e) {
			msoLogger.debug("Exception caught mapping requestBody to RequestDetails", e);
		}

		request.setRequestDetails(requestDetails);
		String startTimeStamp = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss").format(requestDB.getStartTime())
				+ " GMT";
		request.setStartTime(startTimeStamp);

		RequestStatus status = new RequestStatus();
		if (requestDB.getStatusMessage() != null) {
			status.setStatusMessage(requestDB.getStatusMessage());
		}

		if (requestDB.getEndTime() != null) {
			String endTimeStamp = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss").format(requestDB.getEndTime())
					+ " GMT";
			status.setFinishTime(endTimeStamp);
		}

		if (requestDB.getRequestStatus() != null) {
			status.setRequestState(requestDB.getRequestStatus());
		}

		if (requestDB.getProgress() != null) {
			status.setPercentProgress(requestDB.getProgress().intValue());
		}

		request.setRequestStatus(status);

		return request;
	}

	private Response buildServiceErrorResponse (int httpResponseCode,
												MsoException exceptionType,
												String text,
												String messageId) {

		RequestError re = new RequestError();
		if("PolicyException".equals(exceptionType.name())){
			PolicyException pe = new PolicyException();
			pe.setMessageId(messageId);
			pe.setText(text);
			re.setPolicyException(pe);
		} else {
			ServiceException se = new ServiceException();
			se.setMessageId(messageId);
			se.setText(text);
			re.setServiceException(se);
		}

		String requestErrorStr = null;

		try{
			requestErrorStr = JsonUtils.toJsonString(re);
		}catch(IOException e){
			msoLogger.error (MessageEnum.APIH_VALIDATION_ERROR, "", "", MsoLogger.ErrorCode.DataError, "Exception in buildServiceErrorResponse writing exceptionType to string ", e);
		}
		return Response.status (httpResponseCode).entity(requestErrorStr).build ();
	}

}