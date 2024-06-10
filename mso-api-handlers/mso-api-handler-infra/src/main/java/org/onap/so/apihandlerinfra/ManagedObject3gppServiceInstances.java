/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (c) 2022 Deutsche telekom
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


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.http.HttpStatus;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aai.domain.yang.RelationshipData;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.SliceProfile;
import org.onap.aaiclient.client.aai.AAIRestClientImpl;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.so.apihandler.camundabeans.CamundaResponse;
import org.onap.so.apihandler.common.*;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.exceptions.BPMNFailureException;
import org.onap.so.apihandlerinfra.exceptions.RequestDbFailureException;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;
import org.onap.so.moi.*;
import org.onap.so.constants.Status;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.ServiceRecipe;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.logger.LogConstants;
import org.onap.so.logger.LoggingAnchor;
import org.onap.so.logger.MessageEnum;
import org.onap.so.rest.catalog.beans.Vnf;
import org.onap.so.serviceinstancebeans.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Path("/onap/so/infra/moi/api/rest/")
@OpenAPIDefinition(
        info = @Info(title = "/onap/so/infra/moi/api/rest/", description = "API Requests for 3gpp MO Instances"))
public class ManagedObject3gppServiceInstances {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagedObject3gppServiceInstances.class);

    private static final String MSO_PROP_APIHANDLER_INFRA = "MSO_PROP_APIHANDLER_INFRA";

    private static final String END_OF_THE_TRANSACTION = "End of the transaction, the final response is: ";

    private static final String SAVE_TO_DB = "save instance to db";

    private static final String URI_PREFIX = "/moi/api/rest/";

    @Autowired
    private MsoRequest msoRequest;

    @Autowired
    private CatalogDbClient catalogDbClient;

    @Autowired
    private RequestsDbClient requestsDbClient;

    @Autowired
    private RequestHandlerUtils requestHandlerUtils;

    @Autowired
    private ResponseBuilder builder;

    @Autowired
    private CamundaClient camundaClient;

    @Autowired
    private ResponseHandler responseHandler;

    private AAIRestClientImpl aaiRestClient = new AAIRestClientImpl();

    /**
     * POST Requests for 3GPP MOI Service create nssi on a version provided
     *
     * @throws ApiException
     */

    @PUT
    @Path("/{version:[vV][1]}/NetworkSliceSubnet/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Create a 3GPP MOI NSSI on a version provided", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    public Response allocateNssi(MoiAllocateRequest request, @PathParam("version") String version,
            @PathParam("id") String id, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", id);
        return processAllocateRequest(request, Action.createRanSlice, version, requestId, instanceIdMap,
                requestHandlerUtils.getRequestUri(requestContext, URI_PREFIX));
    }

    @PATCH
    @Path("/{version:[vV][1]}/NetworkSliceSubnet/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Modify a 3GPP MOI NSSI on a version provided", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    public Response modifyNssi(MoiAllocateRequest request, @PathParam("version") String version,
            @PathParam("id") String id, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", id);
        return processAllocateRequest(request, Action.modifyRanSlice, version, requestId, instanceIdMap,
                requestHandlerUtils.getRequestUri(requestContext, URI_PREFIX));
    }

    /**
     * DELETE Requests to delete 3GPP MOI Service instance on a version provided
     *
     * @throws ApiException
     */
    @DELETE
    @Path("/{version:[vV][1]}/NetworkSliceSubnet/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Delete a 3GPP MOI NSSI on a version provided", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    public Response deleteNssi(MoiAllocateRequest request, @PathParam("version") String version,
            @PathParam("id") String id, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", id);
        return processAllocateRequest(request, Action.deleteRanSlice, version, requestId, instanceIdMap,
                requestHandlerUtils.getRequestUri(requestContext, URI_PREFIX));
    }

    /**
     * GET Requests for 3GPP MOI Service get nssi on a version provided
     *
     * @throws ApiException
     */
    @GET
    @Operation(description = "Find slice Profile for nssi id", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Vnf.class)))))
    @Path("/{version:[vV][1]}/NetworkSliceSubnet/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    @Transactional(readOnly = true)
    public Response getMOIAttributes(@PathParam("version") String version, @PathParam("id") String id)
            throws ApiException {
        GETMoiResponse mOIResponse = new GETMoiResponse();
        String response = null;

        Optional<ServiceInstance> serviceInstance = aaiRestClient.getServiceInstanceById(id, "5G", "5GCustomer");
        if (serviceInstance.isPresent()) {
            LOGGER.info("Id from request {}", id);
            ServiceInstance serviceInstanceObj = serviceInstance.get();
            mOIResponse.setId(serviceInstanceObj.getServiceInstanceId());
            mOIResponse.setAdministrativeState(serviceInstanceObj.getOperationalStatus());
            mOIResponse.setOperationalState(serviceInstanceObj.getOrchestrationStatus());


            Attributes attributes = new Attributes();
            List<org.onap.so.moi.SliceProfile> sliceProfileList = new ArrayList<org.onap.so.moi.SliceProfile>();

            List<Relationship> listOfNssiRelationship = serviceInstanceObj.getRelationshipList().getRelationship();

            List<Relationship> listOfNssiRelationshipAR = listOfNssiRelationship.stream()
                    .filter(relationship -> relationship.getRelatedTo().equalsIgnoreCase("allotted-resource"))
                    .collect(Collectors.toList());

            for (Relationship relationship : listOfNssiRelationshipAR) {
                org.onap.so.moi.SliceProfile sliceProfile = new org.onap.so.moi.SliceProfile();
                for (RelationshipData relationshipData : relationship.getRelationshipData()) {
                    if (relationshipData.getRelationshipKey()
                            .equalsIgnoreCase("service-instance.service-instance-id")) {
                        String sliceProfileInstanceId = relationshipData.getRelationshipValue();
                        LOGGER.debug(">>> sliceProfileInstance: {}", sliceProfileInstanceId);

                        Optional<ServiceInstance> sliceProfileServiceInstance = aaiRestClient
                                .getServiceInstanceByIdWithDepth(sliceProfileInstanceId, "5G", "5GCustomer");
                        if (sliceProfileServiceInstance.isPresent()) {
                            ServiceInstance sliceProfileServiceInstanceObj = sliceProfileServiceInstance.get();
                            SliceProfile sliceProfile1 =
                                    sliceProfileServiceInstanceObj.getSliceProfiles().getSliceProfile().get(0);
                            try {
                                sliceProfileList
                                        .add(AAISliceProfileToMOISlice(sliceProfile1, sliceProfileServiceInstanceObj));
                            } catch (Exception e) {
                                LOGGER.info("Can not construct responce {}", e);
                            }

                        }


                    }
                }

            }

            attributes.setSliceProfileList(sliceProfileList);
            mOIResponse.setAttributes(attributes);
            ObjectMapper mapper = new ObjectMapper();
            try {
                response = mapper.writeValueAsString(mOIResponse);
            } catch (Exception exception) {
                LOGGER.error("Error while creating MOIResponse JSON" + exception.getMessage());
            }

            return builder.buildResponse(HttpStatus.SC_OK, null, response, version);


        }
        return builder.buildResponse(HttpStatus.SC_NOT_FOUND, null, null, version);
    }

    private org.onap.so.moi.SliceProfile AAISliceProfileToMOISlice(SliceProfile sliceProfile,
            ServiceInstance serviceInstance) {
        org.onap.so.moi.SliceProfile sliceProfileMoi = new org.onap.so.moi.SliceProfile();

        sliceProfileMoi.setSliceProfileId(sliceProfile.getProfileId());

        String serviceType = serviceInstance.getServiceType();

        // rANSliceSubnetProfile
        RANSliceSubnetProfile ranSliceSubnetProfile = new RANSliceSubnetProfile();
        ranSliceSubnetProfile.setServiceType(serviceType);
        ranSliceSubnetProfile.setdLLatency(sliceProfile.getLatency());
        ranSliceSubnetProfile.setCoverageAreaTAList(Integer.valueOf(sliceProfile.getCoverageAreaTAList()));
        ranSliceSubnetProfile.setMaxNumberofUEs(sliceProfile.getMaxNumberOfUEs());
        ranSliceSubnetProfile.setResourceSharingLevel(sliceProfile.getResourceSharingLevel());
        ranSliceSubnetProfile.setAreaTrafficCapDL(sliceProfile.getAreaTrafficCapDL());

        // plmnId
        String[] plmnIdData = serviceInstance.getServiceInstanceLocationId().split("-");
        PlmnId plmnId = new PlmnId();
        plmnId.setMcc(Integer.valueOf(plmnIdData[0]));
        plmnId.setMnc(Integer.valueOf(plmnIdData[1]));

        // snssai
        String[] snssaiData = serviceInstance.getEnvironmentContext().split("-");
        Snssai snssai = new Snssai();
        snssai.setSst(snssaiData[0]);
        snssai.setSd(snssaiData[1]);

        // Plmninfo
        PlmnInfo plmnInfo = new PlmnInfo();
        plmnInfo.setPlmnId(plmnId);
        plmnInfo.setSnssai(snssai);

        List<PlmnInfo> plmnInfoList = new ArrayList<PlmnInfo>();
        plmnInfoList.add(plmnInfo);

        sliceProfileMoi.setrANSliceSubnetProfile(ranSliceSubnetProfile);
        sliceProfileMoi.setPlmnInfoList(plmnInfoList);

        return sliceProfileMoi;

    }

    /**
     * Process allocate service request and send request to corresponding workflow
     *
     * @param request
     * @param action
     * @param version
     * @return
     * @throws ApiException
     */
    private Response processAllocateRequest(MoiAllocateRequest request, Action action, String version, String requestId,
            HashMap<String, String> instanceIdMap, String requestUri) throws ApiException {
        String defaultServiceModelName = "UUI_DEFAULT";
        String requestScope = ModelType.service.name();
        String apiVersion = version.substring(1);
        String serviceRequestJson = toString.apply(request);

        ServiceInstancesRequest sir = createServiceInstanceRequest(request, requestId);
        String requestDetails = null;
        try {
            requestDetails = new ObjectMapper().writeValueAsString(sir);
            LOGGER.debug(">>> sir: {}", sir);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        if (serviceRequestJson != null) {
            InfraActiveRequests currentActiveReq = createRequestObject(request, action, requestId, Status.IN_PROGRESS,
                    requestScope, serviceRequestJson);
            // instanceName ???
            String instanceId = instanceIdMap.get("serviceInstanceId");
            String instanceName = sir.getRequestDetails().getRequestInfo().getInstanceName();
            requestHandlerUtils.checkForDuplicateRequests(action, instanceIdMap, requestScope, currentActiveReq,
                    instanceName);

            requestHandlerUtils.setInstanceId(currentActiveReq, requestScope, instanceId, instanceIdMap);
            try {
                requestsDbClient.save(currentActiveReq);
            } catch (Exception e) {
                LOGGER.error("Exception occurred", e);
                ErrorLoggerInfo errorLoggerInfo =
                        new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ACCESS_EXC, ErrorCode.DataError)
                                .errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
                throw new RequestDbFailureException.Builder(SAVE_TO_DB, e.toString(),
                        HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).cause(e)
                                .errorInfo(errorLoggerInfo).build();
            }

            RecipeLookupResult recipeLookupResult;
            try {
                recipeLookupResult = getServiceInstanceOrchestrationURI("123", action, defaultServiceModelName);

                LOGGER.debug("recipeLookupResult: {}", recipeLookupResult);
            } catch (Exception e) {
                LOGGER.error(LoggingAnchor.FOUR, MessageEnum.APIH_DB_ACCESS_EXC.toString(), MSO_PROP_APIHANDLER_INFRA,
                        ErrorCode.AvailabilityError.getValue(), "Exception while communciate with Catalog DB", e);
                Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_NOT_FOUND,
                        MsoException.ServiceException, "No communication to catalog DB " + e.getMessage(),
                        ErrorNumbers.SVC_NO_SERVER_RESOURCES, null, version);
                LOGGER.debug(END_OF_THE_TRANSACTION + response.getEntity());
                return response;
            }

            if (recipeLookupResult == null) {
                LOGGER.error(LoggingAnchor.FOUR, MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND.toString(),
                        MSO_PROP_APIHANDLER_INFRA, ErrorCode.DataError.getValue(), "No recipe found in DB");
                Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_NOT_FOUND,
                        MsoException.ServiceException, "Recipe does not exist in catalog DB",
                        ErrorNumbers.SVC_GENERAL_SERVICE_ERROR, null, version);
                LOGGER.debug(END_OF_THE_TRANSACTION + response.getEntity());
                return response;
            }

            String serviceInstanceType = sir.getRequestDetails().getRequestParameters().getSubscriptionServiceType();
            RequestClientParameter parameter;
            try {
                parameter = new RequestClientParameter.Builder().setRequestId(requestId).setBaseVfModule(false)
                        .setRecipeTimeout(recipeLookupResult.getRecipeTimeout()).setRequestAction(action.name())
                        .setServiceInstanceId(instanceId).setServiceType(serviceInstanceType)
                        .setRequestDetails(requestDetails).setApiVersion(version).setALaCarte(false)
                        .setRecipeParamXsd(recipeLookupResult.getRecipeParamXsd()).setApiVersion(apiVersion)
                        .setRequestUri(requestUri).build();
            } catch (Exception e) {
                LOGGER.error("Exception occurred", e);
                ErrorLoggerInfo errorLoggerInfo =
                        new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_RESPONSE_ERROR, ErrorCode.SchemaError)
                                .errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
                throw new ValidateException.Builder("Unable to generate RequestClientParamter object" + e.getMessage(),
                        HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_BAD_PARAMETER).errorInfo(errorLoggerInfo)
                                .build();
            }
            return postBPELRequest(currentActiveReq, parameter, recipeLookupResult.getOrchestrationURI(), requestScope);
        } else {
            Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    MsoException.ServiceException, "JsonProcessingException occurred - serviceRequestJson is null",
                    ErrorNumbers.SVC_BAD_PARAMETER, null, version);
            return response;
        }
    }


    private ServiceInstancesRequest createServiceInstanceRequest(MoiAllocateRequest request, String id) {

        RequestDetails requestDetails = new RequestDetails();
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setInstanceName("ran_nssi_" + id);
        requestInfo.setRequestorId("MO");
        requestInfo.setProductFamilyId("5G");
        requestInfo.setSource("MO");

        requestDetails.setRequestInfo(requestInfo);

        SubscriberInfo subscriberInfo = new SubscriberInfo();
        subscriberInfo.setGlobalSubscriberId("5GCustomer");
        requestDetails.setSubscriberInfo(subscriberInfo);

        Project project = new Project();
        project.setProjectName("basicnw-project");
        requestDetails.setProject(project);

        OwningEntity owningEntity = new OwningEntity();
        owningEntity.setOwningEntityId("67f2e84c-734d-4e90-a1e4-d2ffa2e75849");
        owningEntity.setOwningEntityName("OE-5GCustomer");
        requestDetails.setOwningEntity(owningEntity);

        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setaLaCarte(false);
        requestParameters.setSubscriptionServiceType("5G");

        Map<String, Object> nssiUserParams = new HashMap<>();
        Attributes attributes = request.getAttributes();
        nssiUserParams.put("nssi", attributes);

        Map<String, Object> homingSolution = new HashMap<>();
        homingSolution.put("Homing_Solution", "true");

        List<Map<String, Object>> userParams = new ArrayList<>();

        userParams.add(nssiUserParams);
        userParams.add(homingSolution);

        requestParameters.setUserParams(userParams);

        requestDetails.setRequestParameters(requestParameters);

        ServiceInstancesRequest sir = new ServiceInstancesRequest();
        sir.setServiceInstanceId(id);
        sir.setRequestDetails(requestDetails);

        return sir;
    }

    public InfraActiveRequests createRequestObject(Object request, Action action, String requestId, Status status,
            String requestScope, String requestJson) {
        InfraActiveRequests aq = new InfraActiveRequests();
        try {
            String serviceInstanceName = null;
            String serviceInstanceId = null;
            if (action.name().equals("ranSlice")) {
                serviceInstanceName = "ran_nssi_" + requestId;
                aq.setServiceInstanceName(serviceInstanceName);
            } else if (action.name().equals("updateInstance")) {
                LOGGER.debug(">>>>> updateInstance");
            }

            aq.setRequestId(requestId);
            aq.setRequestAction(action.toString());
            aq.setRequestUrl(MDC.get(LogConstants.HTTP_URL));
            Timestamp startTimeStamp = new Timestamp(System.currentTimeMillis());
            aq.setStartTime(startTimeStamp);
            aq.setRequestScope(requestScope);
            aq.setRequestBody(requestJson);
            aq.setRequestStatus(status.toString());
            aq.setLastModifiedBy(Constants.MODIFIED_BY_APIHANDLER);
        } catch (Exception e) {
            LOGGER.error("Exception when creation record request", e);

            if (!status.equals(Status.FAILED)) {
                throw e;
            }
        }
        return aq;
    }

    /**
     * Getting recipes from catalogDb
     *
     * @param serviceModelUUID the service model version uuid
     * @param action the action for the service
     * @param defaultServiceModelName default service name
     * @return the service recipe result
     */
    private RecipeLookupResult getServiceInstanceOrchestrationURI(String serviceModelUUID, Action action,
            String defaultServiceModelName) {

        RecipeLookupResult recipeLookupResult = getServiceURI(serviceModelUUID, action, defaultServiceModelName);

        if (recipeLookupResult != null) {
            LOGGER.debug("Orchestration URI is: " + recipeLookupResult.getOrchestrationURI() + ", recipe Timeout is: "
                    + Integer.toString(recipeLookupResult.getRecipeTimeout()));
        } else {
            LOGGER.debug("No matching recipe record found");
        }
        return recipeLookupResult;
    }

    Function<Object, String> toString = serviceRequest -> {
        ObjectMapper mapper = new ObjectMapper();
        String requestAsString = null;
        try {
            requestAsString = mapper.writeValueAsString(serviceRequest);
        } catch (JsonProcessingException e) {
            LOGGER.debug("Exception while converting service request object to String {}", e);
        }
        return requestAsString;
    };

    /**
     * Getting recipes from catalogDb If Service recipe is not set, use default recipe, if set , use special recipe.
     *
     * @param serviceModelUUID the service version uuid
     * @param action the action of the service.
     * @param defaultServiceModelName default service name
     * @return the service recipe result.
     */
    private RecipeLookupResult getServiceURI(String serviceModelUUID, Action action, String defaultServiceModelName) {

        Service defaultServiceRecord =
                catalogDbClient.getFirstByModelNameOrderByModelVersionDesc(defaultServiceModelName);
        // set recipe as default generic recipe
        ServiceRecipe recipe =
                catalogDbClient.getFirstByServiceModelUUIDAndAction(defaultServiceRecord.getModelUUID(), action.name());
        // check the service special recipe
        if (null != serviceModelUUID && !serviceModelUUID.isEmpty()) {
            ServiceRecipe serviceSpecialRecipe =
                    catalogDbClient.getFirstByServiceModelUUIDAndAction(serviceModelUUID, action.name());
            if (null != serviceSpecialRecipe) {
                // set service special recipe.
                recipe = serviceSpecialRecipe;
            }
        }

        if (recipe == null) {
            return null;
        }
        return new RecipeLookupResult(recipe.getOrchestrationUri(), recipe.getRecipeTimeout(), recipe.getParamXsd());

    }

    private Response postBPELRequest(InfraActiveRequests currentActiveReq, RequestClientParameter parameter,
            String orchestrationURI, String requestScope) throws ApiException {
        ResponseEntity<String> response =
                requestHandlerUtils.postRequest(currentActiveReq, parameter, orchestrationURI);
        LOGGER.debug("BPEL response : " + response);
        int bpelStatus = responseHandler.setStatus(response.getStatusCodeValue());
        String jsonResponse;
        try {
            responseHandler.acceptedResponse(response);
            CamundaResponse camundaResponse = responseHandler.getCamundaResponse(response);
            String responseBody = camundaResponse.getResponse();
            if ("Success".equalsIgnoreCase(camundaResponse.getMessage())) {
                jsonResponse = responseBody;
            } else {
                BPMNFailureException bpmnException =
                        new BPMNFailureException.Builder(String.valueOf(bpelStatus) + responseBody, bpelStatus,
                                ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).build();
                requestHandlerUtils.updateStatus(currentActiveReq, Status.FAILED, bpmnException.getMessage());
                throw bpmnException;
            }
        } catch (ApiException e) {
            requestHandlerUtils.updateStatus(currentActiveReq, Status.FAILED, e.getMessage());
            throw e;
        }
        return builder.buildResponse(HttpStatus.SC_ACCEPTED, parameter.getRequestId(), jsonResponse,
                parameter.getApiVersion());
    }
}
