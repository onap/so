/*
 * Copyright (C) 2019 Verizon. All Rights Reserved Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.onap.so.adapters.vfc.rest;

import java.time.LocalDateTime;
import java.util.*;
import org.onap.so.adapters.vfc.constant.CommonConstant;
import org.onap.so.adapters.vfc.constant.CommonConstant.Step;
import org.onap.so.adapters.vfc.constant.DriverExceptionID;
import org.onap.so.adapters.vfc.constant.HttpCode;
import org.onap.so.adapters.vfc.exceptions.ApplicationException;
import org.onap.so.adapters.vfc.model.*;
import org.onap.so.adapters.vfc.util.JsonUtil;
import org.onap.so.adapters.vfc.util.RestfulUtil;
import org.onap.so.adapters.vfc.util.ValidateUtil;
import org.onap.so.db.request.beans.InstanceNfvoMapping;
import org.onap.so.db.request.beans.OperationStatus;
import org.onap.so.db.request.beans.ResourceOperationStatus;
import org.onap.so.db.request.data.repository.InstanceNfvoMappingRepository;
import org.onap.so.db.request.data.repository.OperationStatusRepository;
import org.onap.so.db.request.data.repository.ResourceOperationStatusRepository;
import org.onap.so.requestsdb.RequestsDbConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;

/**
 * VF-C Manager <br>
 * <p>
 * </p>
 *
 * @author
 * @version ONAP Amsterdam Release 2017-08-28
 */
@Component
@Primary
public class VfcManagerSol005 {

    private static final Logger LOGGER = LoggerFactory.getLogger(VfcManagerSol005.class);

    /**
     * nfvo url map
     */
    private Map<String, String> nfvoUrlMap;

    @Autowired
    private ResourceOperationStatusRepository resourceOperationStatusRepository;

    @Autowired
    private RestfulUtil restfulUtil;

    @Autowired
    private OperationStatusRepository operationStatusRepository;

    @Autowired
    private InstanceNfvoMappingRepository instanceNfvoMappingRepository;

    private InstanceNfvoMapping instanceNfvoMapping = new InstanceNfvoMapping();

    public VfcManagerSol005() {
        nfvoUrlMap = new HashMap<>();
        nfvoUrlMap.put(Step.CREATE, CommonConstant.SOL005_NFVO_CREATE_URL);
        nfvoUrlMap.put(Step.INSTANTIATE, CommonConstant.SOL005_NFVO_INSTANTIATE_URL);
        nfvoUrlMap.put(Step.TERMINATE, CommonConstant.SOL005_NFVO_TERMINATE_URL);
        nfvoUrlMap.put(Step.DELETE, CommonConstant.SOL005_NFVO_DELETE_URL);
        nfvoUrlMap.put(Step.QUERY, CommonConstant.SOL005_NFVO_QUERY_URL);
        nfvoUrlMap.put(Step.SCALE, CommonConstant.NFVO_SCALE_URL);
    }

    /**
     * create network service <br>
     *
     * @param segInput input parameters for current node from http request
     * @return
     * @since ONAP Dubilin Release
     */
    public RestfulResponse createNs(NSResourceInputParameter segInput) throws ApplicationException {

        Map<String, String> nfvoDetails;
        // Step1: get service template by node type
        String nsdId = segInput.getNsServiceModelUUID();
        // nsdId for NFVO is "id" in the response, while for SDNO is "servcice template id"
        LOGGER.info("serviceTemplateId is {}, id is {}", nsdId, nsdId);


        LOGGER.info("SOL005 create ns -> begin");
        // Step2: Prepare url and method type
        String url = getUrl(null, CommonConstant.Step.CREATE);
        String methodType = CommonConstant.MethodType.POST;

        // Step3: Prepare restful parameters and options
        CreateNsRequest createNsRequest = new CreateNsRequest();
        createNsRequest.setNsDescription(segInput.getNsServiceDescription());
        createNsRequest.setNsdId(segInput.getNsServiceModelUUID());
        createNsRequest.setNsName(segInput.getNsServiceName());

        String createReq = JsonUtil.marshal(createNsRequest);
        RestfulResponse aaiRestfulResponse = null;
        NsParameters nsParameters = segInput.getNsParameters();
        if (nsParameters.getAdditionalParamForNs().containsKey("orchestrator")) {
            if (nsParameters.getAdditionalParamForNs().get("orchestrator") != null) {
                String nfvo = nsParameters.getAdditionalParamForNs().get("nfvo").toString();
                aaiRestfulResponse = restfulUtil.getNfvoFromAAI(nfvo);
                nfvoDetails = JsonUtil.unMarshal(aaiRestfulResponse.getResponseContent(), Map.class);
                url = nfvoDetails.get("url") + nfvoDetails.get("api-root") + url;

            }
        } else {
            LOGGER.error("Nfvo not present in AAI");
            throw new ApplicationException(HttpCode.INTERNAL_SERVER_ERROR, DriverExceptionID.FAIL_TO_CREATE_NS);
        }


        // Prepare request header for createNs request.
        Map<String, String> requestHeader = new HashMap<>();
        requestHeader.put("GLOBALCUSTOMERID", segInput.getNsOperationKey().getGlobalSubscriberId());
        requestHeader.put("SERVICETYPE", segInput.getNsOperationKey().getServiceType());

        // Step4: Call NFVO or SDNO lcm to create ns
        LOGGER.info("Request Payload for CreateNs: " + createReq);

        RestfulResponse createRsp = restfulUtil.send(url, methodType, createReq, requestHeader);
        ValidateUtil.assertObjectNotNull(createRsp);
        LOGGER.info("create ns response status is : {}", createRsp.getStatus());
        LOGGER.info("create ns response content is : {}", createRsp.getResponseContent());

        // Step 5: save resource operation information
        ResourceOperationStatus status = new ResourceOperationStatus(segInput.getNsOperationKey().getServiceId(),
                segInput.getNsOperationKey().getOperationId(), segInput.getNsOperationKey().getNodeTemplateUUID());
        status.setStatus(RequestsDbConstant.Status.PROCESSING);
        status = resourceOperationStatusRepository.save(status);
        if (!HttpCode.isSucess(createRsp.getStatus())) {
            LOGGER.error("update segment operation status : fail to create ns");
            status.setProgress("40");
            status.setStatusDescription("NS not created");
            status.setStatus(RequestsDbConstant.Status.ERROR);
            status.setErrorCode(String.valueOf(createRsp.getStatus()));
            resourceOperationStatusRepository.save(status);
            throw new ApplicationException(HttpCode.INTERNAL_SERVER_ERROR, DriverExceptionID.FAIL_TO_CREATE_NS);
        }
        // TODO: Capture all the content of the response. Currently fetching ID value alone.
        // Should be converted into the NsInstance.class
        @SuppressWarnings("unchecked")
        Map<String, String> rsp = JsonUtil.unMarshal(createRsp.getResponseContent(), Map.class);
        String nsInstanceId = rsp.get(CommonConstant.SOL005_NS_INSTANCE_ID);
        if (ValidateUtil.isStrEmpty(nsInstanceId)) {
            LOGGER.error("Invalid instanceId from create operation");
            throw new ApplicationException(HttpCode.INTERNAL_SERVER_ERROR,
                    DriverExceptionID.INVALID_RESPONSEE_FROM_CREATE_OPERATION);
        }

        nfvoDetails = JsonUtil.unMarshal(aaiRestfulResponse.getResponseContent(), Map.class);
        instanceNfvoMapping.setInstanceId(nsInstanceId);
        instanceNfvoMapping.setPassword(nfvoDetails.get("password"));
        instanceNfvoMapping.setUsername(nfvoDetails.get("userName"));
        instanceNfvoMapping.setNfvoName(nfvoDetails.get("nfvoId"));
        instanceNfvoMapping.setEndpoint(nfvoDetails.get("url"));
        instanceNfvoMapping.setApiRoot(nfvoDetails.get("api-root"));
        instanceNfvoMappingRepository.save(instanceNfvoMapping);
        LOGGER.info("create ns -> end");
        LOGGER.info("save segment and operaton info -> begin");
        // Step 6: add relation between service and NS
        AaiUtil.addRelation(segInput.getNsOperationKey().getGlobalSubscriberId(),
                segInput.getNsOperationKey().getServiceType(), segInput.getNsOperationKey().getServiceId(),
                nsInstanceId);
        LOGGER.info("save segment and operation info -> end");
        return createRsp;
    }

    /**
     * delete network service <br>
     *
     * @param nsOperationKey The operation key of the NS resource
     * @param nsInstanceId The NS instance id
     * @return
     * @since ONAP Dublin Release
     */
    public RestfulResponse deleteNs(NsOperationKey nsOperationKey, String nsInstanceId) throws ApplicationException {
        LOGGER.info("SOL005 delete ns -> begin");
        // Step1: prepare url and methodType
        String url = getUrl(nsInstanceId, CommonConstant.Step.DELETE);
        String methodType = CommonConstant.MethodType.DELETE;

        // Step2: prepare restful parameters and options
        RestfulResponse deleteRsp = restfulUtil.send(url, methodType, "");
        ValidateUtil.assertObjectNotNull(deleteRsp);
        LOGGER.info("delete ns response status is : {}", deleteRsp.getStatus());
        LOGGER.info("delete ns response content is : {}", deleteRsp.getResponseContent());
        LOGGER.info("delete ns -> end");

        ResourceOperationStatus status = new ResourceOperationStatus(nsOperationKey.getServiceId(),
                nsOperationKey.getOperationId(), nsOperationKey.getNodeTemplateUUID());
        if (!HttpCode.isSucess(deleteRsp.getStatus())) {
            LOGGER.error("fail to delete ns");

            status.setStatus(RequestsDbConstant.Status.ERROR);
            status.setErrorCode(String.valueOf(deleteRsp.getStatus()));
            status.setStatusDescription(CommonConstant.StatusDesc.TERMINATE_NS_FAILED);
            resourceOperationStatusRepository.save(status);
            throw new ApplicationException(HttpCode.INTERNAL_SERVER_ERROR, DriverExceptionID.FAIL_TO_DELETE_NS);
        }

        // Step3: remove relation info between service and ns
        AaiUtil.removeRelation(nsOperationKey.getGlobalSubscriberId(), nsOperationKey.getServiceType(),
                nsOperationKey.getServiceId(), nsInstanceId);
        LOGGER.info("delete segment information -> end");

        // Step4: update service segment operation status
        status.setStatus(RequestsDbConstant.Status.FINISHED);
        status.setErrorCode(String.valueOf(deleteRsp.getStatus()));
        status.setProgress("100");
        status.setStatusDescription("VFC resource deletion finished");
        resourceOperationStatusRepository.save(status);
        LOGGER.info("update segment operaton status for delete -> end");

        return deleteRsp;

    }

    /**
     * instantiate network service <br>
     *
     * @param nsInstanceId The NS instance id
     * @param segInput input parameters for current node from http request
     * @return
     * @since ONAP Dublin Release
     */
    public RestfulResponse instantiateNs(String nsInstanceId, NSResourceInputParameter segInput)
            throws ApplicationException {
        // Call the NFVO or SDNO service to instantiate service
        LOGGER.info("SOL005 instantiate ns -> begin");

        // Step1: Prepare restful parameters and options
        InstantiateNsRequest instantiateNsRequest = new InstantiateNsRequest();

        NsInstantiateReq oRequest = new NsInstantiateReq();
        oRequest.setNsInstanceId(nsInstanceId);
        NsParameters nsParameters = segInput.getNsParameters();

        ArrayList<VnfLocationConstraint> vnfLocationConstraints = new ArrayList<VnfLocationConstraint>();
        for (LocationConstraint locationConstraint : nsParameters.getLocationConstraints()) {
            VnfLocationConstraint vnfLocationConstraint = new VnfLocationConstraint();
            vnfLocationConstraint.setVnfProfileId(locationConstraint.getVnfProfileId());
            vnfLocationConstraint.setLocationConstraints(null);
            vnfLocationConstraints.add(vnfLocationConstraint);

        }
        instantiateNsRequest.setAditionalParamsForNs(nsParameters.getAdditionalParamForNs());
        // Setting FlavourID which is a mandatory paramater to default
        // as UUI is not sending this parameter to so
        instantiateNsRequest.setNsFlavourId("default");
        String instReq = JsonUtil.marshal(instantiateNsRequest);
        LOGGER.info("Request Payload for InstantiateNs: " + instReq);
        // Step2: prepare url and
        String url = getUrl(nsInstanceId, CommonConstant.Step.INSTANTIATE);
        String methodType = CommonConstant.MethodType.POST;
        instanceNfvoMapping = instanceNfvoMappingRepository.findOneByInstanceId(nsInstanceId);

        if (instanceNfvoMapping != null) {

            url = instanceNfvoMapping.getEndpoint() + instanceNfvoMapping.getApiRoot() + url;

        } else {
            throw new ApplicationException(HttpCode.INTERNAL_SERVER_ERROR, DriverExceptionID.FAIL_TO_INSTANTIATE_NS);

        }
        // Step3: prepare restful parameters and options
        Map<String, String> reqBody = new HashMap<>();
        reqBody.put("terminationTime", LocalDateTime.now().toString());
        RestfulResponse instRsp = restfulUtil.send(url, methodType, instReq);
        ResourceOperationStatus status = new ResourceOperationStatus(segInput.getNsOperationKey().getServiceId(),
                segInput.getNsOperationKey().getOperationId(), segInput.getNsOperationKey().getNodeTemplateUUID());
        ValidateUtil.assertObjectNotNull(instRsp);
        if (!HttpCode.isSucess(instRsp.getStatus())) {
            LOGGER.error("update segment operation status : fail to instantiate ns");
            status.setStatus(RequestsDbConstant.Status.ERROR);
            status.setErrorCode(String.valueOf(instRsp.getStatus()));
            status.setStatusDescription(CommonConstant.StatusDesc.INSTANTIATE_NS_FAILED);
            resourceOperationStatusRepository.save(status);
            throw new ApplicationException(HttpCode.INTERNAL_SERVER_ERROR, DriverExceptionID.FAIL_TO_INSTANTIATE_NS);
        }
        LOGGER.info("instantiate ns response status is : {}", instRsp.getStatus());
        LOGGER.info("response payload is {}", instRsp.getResponseContent());
        String jobId = null;
        if (instRsp.getStatus() == 202) {
            String jobUri = instRsp.getRespHeaderStr(CommonConstant.JOB_URI);
            LOGGER.info("JOB URI" + jobUri);
            jobId = jobUri.split("/")[4];
            if (ValidateUtil.isStrEmpty(jobId)) {
                LOGGER.error("Invalid jobId from instantiate operation");
                status.setStatus(RequestsDbConstant.Status.ERROR);
                status.setErrorCode(String.valueOf(instRsp.getStatus()));
                status.setStatusDescription(CommonConstant.StatusDesc.INSTANTIATE_NS_FAILED);
                resourceOperationStatusRepository.save(status);
                throw new ApplicationException(HttpCode.INTERNAL_SERVER_ERROR,
                        DriverExceptionID.INVALID_RESPONSE_FROM_INSTANTIATE_OPERATION);
            }

        } else if (instRsp.getStatus() > 400 && instRsp.getStatus() < 600) {
            LOGGER.error("ERROR while executing instantiateNs request");
            ProblemDetails problemDetails = JsonUtil.unMarshal(instRsp.getResponseContent(), ProblemDetails.class);
            status.setStatus(RequestsDbConstant.Status.ERROR);
            status.setErrorCode(String.valueOf(instRsp.getStatus()));
            status.setStatusDescription(CommonConstant.StatusDesc.INSTANTIATE_NS_FAILED + problemDetails.getDetail());
            resourceOperationStatusRepository.save(status);
            if (instRsp.getStatus() == 406) {
                throw new ApplicationException(HttpCode.NOT_ACCEPTABLE, DriverExceptionID.FAIL_TO_INSTANTIATE_NS);
            } else if (instRsp.getStatus() == 400) {
                throw new ApplicationException(HttpCode.BAD_REQUEST, DriverExceptionID.FAIL_TO_INSTANTIATE_NS);
            } else if (instRsp.getStatus() == 404) {
                throw new ApplicationException(HttpCode.NOT_FOUND, DriverExceptionID.FAIL_TO_INSTANTIATE_NS);
            } else if (instRsp.getStatus() == 409) {
                LOGGER.error("The operation cannot be executed currently,\n"
                        + "due to a conflict with the state of the resource");
                throw new ApplicationException(HttpCode.RESPOND_CONFLICT, DriverExceptionID.FAIL_TO_INSTANTIATE_NS);
            } else if (instRsp.getStatus() == 500) {
                throw new ApplicationException(HttpCode.INTERNAL_SERVER_ERROR,
                        DriverExceptionID.FAIL_TO_INSTANTIATE_NS);
            } else {
                throw new ApplicationException(HttpCode.INTERNAL_SERVER_ERROR,
                        DriverExceptionID.FAIL_TO_INSTANTIATE_NS);
            }

        }
        LOGGER.info("Job id is " + jobId);
        LOGGER.info("Nfvo Details" + instanceNfvoMapping.toString());
        LOGGER.info("instantiate ns -> end");
        // Step 3: update segment operation job id
        LOGGER.info("update resource operation status job id -> begin");
        status.setJobId(jobId);
        status.setProgress("100");
        status.setStatusDescription("NS initiation completed.");
        resourceOperationStatusRepository.save(status);
        instanceNfvoMapping.setJobId(jobId);
        instanceNfvoMappingRepository.save(instanceNfvoMapping);
        LOGGER.info("update segment operation job id -> end" + instanceNfvoMapping.toString());
        return instRsp;
    }

    /**
     * terminate network service <br>
     *
     * @param nsOperationKey The operation key for NS resource
     * @param nsInstanceId The NS instance id
     * @return
     * @since ONAP Dublin Release
     */
    public RestfulResponse terminateNs(NsOperationKey nsOperationKey, String nsInstanceId) throws ApplicationException {
        // Step1: save segment operation info for delete process
        LOGGER.info("save segment operation for delete process");
        ResourceOperationStatus status = new ResourceOperationStatus(nsOperationKey.getServiceId(),
                nsOperationKey.getOperationId(), nsOperationKey.getNodeTemplateUUID());
        status.setStatus(RequestsDbConstant.Status.PROCESSING);
        resourceOperationStatusRepository.save(status);

        LOGGER.info("SOL005 terminate ns -> begin");
        // Step2: prepare url and method type
        String url = getUrl(nsInstanceId, CommonConstant.Step.TERMINATE);
        String methodType = CommonConstant.MethodType.POST;

        instanceNfvoMapping = instanceNfvoMappingRepository.findOneByInstanceId(nsInstanceId);

        if (instanceNfvoMapping != null) {

            url = instanceNfvoMapping.getEndpoint() + instanceNfvoMapping.getApiRoot() + url;

        } else {
            throw new ApplicationException(HttpCode.INTERNAL_SERVER_ERROR, DriverExceptionID.FAIL_TO_TERMINATE_NS);
        }
        // Step3: prepare restful parameters and options
        Map<String, String> reqBody = new HashMap<>();
        reqBody.put("terminationTime", LocalDateTime.now().toString());

        // Step4: Call the NFVO or SDNO service to terminate service
        LOGGER.info("request body for terminate NS" + JsonUtil.marshal(reqBody));
        RestfulResponse terminateRsp = restfulUtil.send(url, methodType, JsonUtil.marshal(reqBody));
        ValidateUtil.assertObjectNotNull(terminateRsp);
        LOGGER.info("terminate ns response status is : {}", terminateRsp.getStatus());
        LOGGER.info("terminate ns response content is : {}", terminateRsp.getResponseContent());
        // Step 3: update segment operation
        if (!HttpCode.isSucess(terminateRsp.getStatus())) {
            LOGGER.error("fail to terminate ns");
            status.setStatus(RequestsDbConstant.Status.ERROR);
            status.setErrorCode(String.valueOf(terminateRsp.getStatus()));
            status.setStatusDescription(CommonConstant.StatusDesc.TERMINATE_NS_FAILED);
            resourceOperationStatusRepository.save(status);

            throw new ApplicationException(HttpCode.INTERNAL_SERVER_ERROR, DriverExceptionID.FAIL_TO_TERMINATE_NS);
        }
        // @SuppressWarnings("unchecked")
        String jobId = null;
        Map<String, String> rsp = new HashMap<>();
        if (terminateRsp.getStatus() == 202) {
            String jobUri = terminateRsp.getRespHeaderStr(CommonConstant.JOB_URI);
            jobId = jobUri.split("/")[4];
            jobId.split("/");
            if (ValidateUtil.isStrEmpty(jobId)) {
                LOGGER.error("Invalid jobId from instantiate operation");
                status.setStatus(RequestsDbConstant.Status.ERROR);
                status.setErrorCode(String.valueOf(terminateRsp.getStatus()));
                status.setStatusDescription(CommonConstant.StatusDesc.INSTANTIATE_NS_FAILED);
                resourceOperationStatusRepository.save(status);
                throw new ApplicationException(HttpCode.INTERNAL_SERVER_ERROR,
                        DriverExceptionID.INVALID_RESPONSE_FROM_INSTANTIATE_OPERATION);
            }
            rsp.put(CommonConstant.JOB_ID, jobId);
            LOGGER.info("terminate ns -> end");
            LOGGER.info("update segment job id -> begin");
            status.setProgress("60");
            status.setStatusDescription("NS is termination completed");
            status.setJobId(jobId);
            resourceOperationStatusRepository.save(status);
            LOGGER.info("update segment job id -> end");
        } else if (terminateRsp.getStatus() > 400 && terminateRsp.getStatus() < 600) {
            LOGGER.error("ERROR while executing instantiateNs request");
            ProblemDetails problemDetails = JsonUtil.unMarshal(terminateRsp.getResponseContent(), ProblemDetails.class);
            status.setStatus(RequestsDbConstant.Status.ERROR);
            status.setErrorCode(String.valueOf(terminateRsp.getStatus()));
            status.setStatusDescription(CommonConstant.StatusDesc.TERMINATE_NS_FAILED + problemDetails.getDetail());
            resourceOperationStatusRepository.save(status);
            if (terminateRsp.getStatus() == 406) {
                throw new ApplicationException(HttpCode.NOT_ACCEPTABLE, DriverExceptionID.FAIL_TO_TERMINATE_NS);
            } else if (terminateRsp.getStatus() == 400) {
                throw new ApplicationException(HttpCode.BAD_REQUEST, DriverExceptionID.FAIL_TO_TERMINATE_NS);
            } else if (terminateRsp.getStatus() == 404) {
                throw new ApplicationException(HttpCode.NOT_FOUND, DriverExceptionID.FAIL_TO_TERMINATE_NS);
            } else if (terminateRsp.getStatus() == 409) {
                LOGGER.error("The operation cannot be executed currently,\n"
                        + "due to a conflict with the state of the resource");
                throw new ApplicationException(HttpCode.RESPOND_CONFLICT, DriverExceptionID.FAIL_TO_TERMINATE_NS);
            } else if (terminateRsp.getStatus() == 500) {
                throw new ApplicationException(HttpCode.INTERNAL_SERVER_ERROR, DriverExceptionID.FAIL_TO_TERMINATE_NS);
            } else {
                throw new ApplicationException(HttpCode.INTERNAL_SERVER_ERROR, DriverExceptionID.FAIL_TO_TERMINATE_NS);
            }

        }
        instanceNfvoMapping.setJobId(jobId);
        instanceNfvoMappingRepository.save(instanceNfvoMapping);
        terminateRsp.setResponseContent(rsp.toString());
        return terminateRsp;
    }

    /**
     * get ns progress by job Id <br>
     *
     * @param nsOperationKey The OperationKey for NS resource
     * @param jobId the job id
     * @return
     * @since ONAP Dublin Release
     */
    public RestfulResponse getNsProgress(NsOperationKey nsOperationKey, String jobId) throws ApplicationException {

        ValidateUtil.assertObjectNotNull(jobId);
        // Step 1: query the current resource operation status
        ResourceOperationStatus status = new ResourceOperationStatus(nsOperationKey.getServiceId(),
                nsOperationKey.getOperationId(), nsOperationKey.getNodeTemplateUUID());
        // status = resourceOperationStatusRepository.findOne(Example.of(status))
        // .orElseThrow(() -> new ApplicationException(404, "Cannot Find Operation Status"));

        // Get NFVO details
        instanceNfvoMapping = instanceNfvoMappingRepository.findOneByJobId(jobId);

        // Step 2: start query
        LOGGER.info("SOL005 query ns status -> begin");
        String url = getUrl(jobId, CommonConstant.Step.QUERY);
        String methodType = CommonConstant.MethodType.GET;
        if (instanceNfvoMapping != null) {

            url = instanceNfvoMapping.getEndpoint() + instanceNfvoMapping.getApiRoot() + url;

        } else {
            throw new ApplicationException(HttpCode.INTERNAL_SERVER_ERROR, DriverExceptionID.FAIL_TO_QUERY_JOB_STATUS);
        }

        // prepare restful parameters and options
        RestfulResponse rsp = restfulUtil.send(url, methodType, "");
        ValidateUtil.assertObjectNotNull(rsp);
        LOGGER.info("query ns progress response status is : {}", rsp.getStatus());
        LOGGER.info("query ns progress response content is : {}", rsp.getResponseContent());
        // Step 3:check the response staus
        if (!HttpCode.isSucess(rsp.getStatus())) {
            LOGGER.info("fail to query job status");
            ProblemDetails problemDetails = JsonUtil.unMarshal(rsp.getResponseContent(), ProblemDetails.class);
            status.setErrorCode(String.valueOf(rsp.getStatus()));
            status.setStatus(RequestsDbConstant.Status.ERROR);
            status.setStatusDescription(CommonConstant.StatusDesc.QUERY_JOB_STATUS_FAILED + problemDetails.getDetail());
            resourceOperationStatusRepository.save(status);
            throw new ApplicationException(HttpCode.INTERNAL_SERVER_ERROR, DriverExceptionID.FAIL_TO_QUERY_JOB_STATUS);
        }
        // Step 4: Process Network Service Instantiate Response
        NsLcmOpOcc nsProgress = JsonUtil.unMarshal(rsp.getResponseContent(), NsLcmOpOcc.class);
        if (CommonConstant.operationState.FAILED.equals(nsProgress.getOperationState())) {
            LOGGER.info("NS instantiate fails");
            status.setErrorCode(String.valueOf(rsp.getStatus()));
            status.setStatus(RequestsDbConstant.Status.ERROR);
            status.setStatusDescription(
                    CommonConstant.StatusDesc.INSTANTIATE_NS_FAILED + nsProgress.getError().getDetail());
            resourceOperationStatusRepository.save(status);
            throw new ApplicationException(HttpCode.INTERNAL_SERVER_ERROR, DriverExceptionID.FAIL_TO_QUERY_JOB_STATUS);
        }
        // Step 5: update segment operation progress

        if (nsProgress.getOperationState().equals(CommonConstant.operationState.PROCESSING)) {
            status.setProgress("40");
            status.setStatus(RequestsDbConstant.Status.PROCESSING);
            status.setStatusDescription("NS operation is in progress");
            resourceOperationStatusRepository.save(status);
            updateOperationStatusBasedOnResourceStatus(status);
        } else if (nsProgress.getOperationState().equals(CommonConstant.operationState.PARTIALLY_COMPLETED)) {
            status.setProgress("60");
            status.setStatus(RequestsDbConstant.Status.PROCESSING);
            status.setStatusDescription("NS operation is partially completed");
            resourceOperationStatusRepository.save(status);
        } else if (nsProgress.getOperationState().equals(CommonConstant.operationState.COMPLETED)) {
            status.setStatus(RequestsDbConstant.Status.FINISHED);
            status.setProgress("100");
            status.setStatusDescription("NS operation is Completed");
            resourceOperationStatusRepository.save(status);
            updateOperationStatusBasedOnResourceStatus(status);

        } else if (nsProgress.getOperationState().equals(CommonConstant.operationState.FAILED)
                || nsProgress.getOperationState().equals(CommonConstant.operationState.FAILED_TEMP)) {
            status.setStatus(RequestsDbConstant.Status.ERROR);
            status.setProgress("0");
            status.setStatusDescription("NS operation Failed");
            resourceOperationStatusRepository.save(status);
            updateOperationStatusBasedOnResourceStatus(status);
            throw new ApplicationException(HttpCode.INTERNAL_SERVER_ERROR, DriverExceptionID.JOB_STATUS_ERROR);
        } else {
            LOGGER.error("unexcepted response status");

        }
        return rsp;
    }

    /**
     * get url for the operation <br>
     *
     * @param variable variable should be put in the url
     * @param step step of the operation (terminate,query,delete)
     * @return
     * @since ONAP Dublin Release
     */
    private String getUrl(String variable, String step) {

        String url;
        String originalUrl;
        originalUrl = nfvoUrlMap.get(step);
        url = String.format(originalUrl, variable);
        return url;

    }

    private void updateOperationStatusBasedOnResourceStatus(ResourceOperationStatus operStatus) {
        String serviceId = operStatus.getServiceId();
        String operationId = operStatus.getOperationId();

        LOGGER.debug("Request database - update Operation Status Based On Resource Operation Status with service Id: "
                + "{}, operationId: {}", serviceId, operationId);

        List<ResourceOperationStatus> lstResourceStatus =
                resourceOperationStatusRepository.findByServiceIdAndOperationId(serviceId, operationId);
        if (lstResourceStatus == null) {
            LOGGER.error("Unable to retrieve resourceOperStatus Object by ServiceId: {} operationId: {}", serviceId,
                    operationId);
            return;
        }

        // count the total progress
        int resourceCount = lstResourceStatus.size();
        int progress = 0;
        boolean isFinished = true;
        for (ResourceOperationStatus lstResourceStatu : lstResourceStatus) {
            progress = progress + Integer.valueOf(lstResourceStatu.getProgress()) / resourceCount;
            if (RequestsDbConstant.Status.PROCESSING.equals(lstResourceStatu.getStatus())) {
                isFinished = false;
            }
        }

        OperationStatus serviceOperStatus =
                operationStatusRepository.findOneByServiceIdAndOperationId(serviceId, operationId);
        if (serviceOperStatus == null) {
            String error = "Entity not found. Unable to retrieve OperationStatus Object ServiceId: " + serviceId
                    + " operationId: " + operationId;
            LOGGER.error(error);

            serviceOperStatus = new OperationStatus();
            serviceOperStatus.setOperationId(operationId);
            serviceOperStatus.setServiceId(serviceId);
        }

        progress = progress > 100 ? 100 : progress;
        serviceOperStatus.setProgress(String.valueOf(progress));
        serviceOperStatus.setOperationContent(operStatus.getStatusDescription());
        // if current resource failed. service failed.
        if (RequestsDbConstant.Status.ERROR.equals(operStatus.getStatus())) {
            serviceOperStatus.setResult(RequestsDbConstant.Status.ERROR);
            serviceOperStatus.setReason(operStatus.getStatusDescription());
        } else if (isFinished) {
            // if finished
            serviceOperStatus.setResult(RequestsDbConstant.Status.FINISHED);
            serviceOperStatus.setProgress(RequestsDbConstant.Progress.ONE_HUNDRED);
        }

        operationStatusRepository.save(serviceOperStatus);
    }
}
