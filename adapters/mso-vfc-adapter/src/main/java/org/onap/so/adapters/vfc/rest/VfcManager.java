/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
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

package org.onap.so.adapters.vfc.rest;

import java.util.HashMap;
import java.util.Map;
import org.onap.so.adapters.vfc.constant.CommonConstant;
import org.onap.so.adapters.vfc.constant.CommonConstant.Step;
import org.onap.so.adapters.vfc.constant.DriverExceptionID;
import org.onap.so.adapters.vfc.constant.HttpCode;
import org.onap.so.adapters.vfc.exceptions.ApplicationException;
import org.onap.so.adapters.vfc.model.CustomerModel;
import org.onap.so.adapters.vfc.model.NSResourceInputParameter;
import org.onap.so.adapters.vfc.model.NsCreateReq;
import org.onap.so.adapters.vfc.model.NsInstantiateReq;
import org.onap.so.adapters.vfc.model.NsOperationKey;
import org.onap.so.adapters.vfc.model.NsParameters;
import org.onap.so.adapters.vfc.model.NsProgressStatus;
import org.onap.so.adapters.vfc.model.NsScaleParameters;
import org.onap.so.adapters.vfc.model.ResponseDescriptor;
import org.onap.so.adapters.vfc.model.RestfulResponse;
import org.onap.so.adapters.vfc.model.VFCScaleData;
import org.onap.so.adapters.vfc.util.JsonUtil;
import org.onap.so.adapters.vfc.util.RestfulUtil;
import org.onap.so.adapters.vfc.util.ValidateUtil;
import org.onap.so.db.request.beans.ResourceOperationStatus;
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
public class VfcManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(VfcManager.class);

    /**
     * nfvo url map
     */
    private Map<String, String> nfvoUrlMap;

    @Autowired
    private ResourceOperationStatusRepository resourceOperationStatusRepository;

    @Autowired
    private RestfulUtil restfulUtil;


    public VfcManager() {
        nfvoUrlMap = new HashMap<>();
        nfvoUrlMap.put(Step.CREATE, CommonConstant.NFVO_CREATE_URL);
        nfvoUrlMap.put(Step.INSTANTIATE, CommonConstant.NFVO_INSTANTIATE_URL);
        nfvoUrlMap.put(Step.TERMINATE, CommonConstant.NFVO_TERMINATE_URL);
        nfvoUrlMap.put(Step.DELETE, CommonConstant.NFVO_DELETE_URL);
        nfvoUrlMap.put(Step.QUERY, CommonConstant.NFVO_QUERY_URL);
        nfvoUrlMap.put(Step.SCALE, CommonConstant.NFVO_SCALE_URL);
    }

    /**
     * create network service <br>
     * 
     * @param segInput input parameters for current node from http request
     * @return
     * @since ONAP Amsterdam Release
     */
    public RestfulResponse createNs(NSResourceInputParameter segInput) throws ApplicationException {

        // Step1: get service template by node type
        String csarId = segInput.getNsServiceModelUUID();
        // nsdId for NFVO is "id" in the response, while for SDNO is "servcice template id"
        LOGGER.info("serviceTemplateId is {}, id is {}", csarId, csarId);

        LOGGER.info("create ns -> begin");
        // Step2: Prepare url and method type
        String url = getUrl(null, CommonConstant.Step.CREATE);
        String methodType = CommonConstant.MethodType.POST;

        // Step3: Prepare restful parameters and options
        NsCreateReq oRequest = new NsCreateReq();
        oRequest.setCsarId(csarId);
        oRequest.setNsName(segInput.getNsServiceName());
        oRequest.setDescription(segInput.getNsServiceDescription());
        CustomerModel context = new CustomerModel();
        context.setGlobalCustomerId(segInput.getNsOperationKey().getGlobalSubscriberId());
        context.setServiceType(segInput.getNsOperationKey().getServiceType());
        oRequest.setContext(context);
        String createReq = JsonUtil.marshal(oRequest);

        // Step4: Call NFVO or SDNO lcm to create ns
        RestfulResponse createRsp = restfulUtil.send(url, methodType, createReq);
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
            status.setStatusDescription("NS is created");
            status.setStatus(RequestsDbConstant.Status.ERROR);
            status.setErrorCode(String.valueOf(createRsp.getStatus()));
            resourceOperationStatusRepository.save(status);
            throw new ApplicationException(HttpCode.INTERNAL_SERVER_ERROR, DriverExceptionID.FAIL_TO_CREATE_NS);
        }
        @SuppressWarnings("unchecked")
        Map<String, String> rsp = JsonUtil.unMarshal(createRsp.getResponseContent(), Map.class);
        String nsInstanceId = rsp.get(CommonConstant.NS_INSTANCE_ID);
        if (ValidateUtil.isStrEmpty(nsInstanceId)) {
            LOGGER.error("Invalid instanceId from create operation");
            throw new ApplicationException(HttpCode.INTERNAL_SERVER_ERROR,
                    DriverExceptionID.INVALID_RESPONSEE_FROM_CREATE_OPERATION);
        }
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
     * @since ONAP Amsterdam Release
     */
    public RestfulResponse deleteNs(NsOperationKey nsOperationKey, String nsInstanceId) throws ApplicationException {
        LOGGER.info("delete ns -> begin");
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
     * @since ONAP Amsterdam Release
     */
    public RestfulResponse instantiateNs(String nsInstanceId, NSResourceInputParameter segInput)
            throws ApplicationException {
        // Call the NFVO or SDNO service to instantiate service
        LOGGER.info("instantiate ns -> begin");

        // Step1: Prepare restful parameters and options
        NsInstantiateReq oRequest = new NsInstantiateReq();
        oRequest.setNsInstanceId(nsInstanceId);
        NsParameters nsParameters = segInput.getNsParameters();
        oRequest.setLocationConstraints(nsParameters.getLocationConstraints());
        oRequest.setAdditionalParamForNs(nsParameters.getAdditionalParamForNs());
        String instReq = JsonUtil.marshal(oRequest);
        // Step2: prepare url and
        String url = getUrl(nsInstanceId, CommonConstant.Step.INSTANTIATE);
        String methodType = CommonConstant.MethodType.POST;

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
        LOGGER.info("instantiate ns response content is : {}", instRsp.getResponseContent());
        ValidateUtil.assertObjectNotNull(instRsp.getResponseContent());
        @SuppressWarnings("unchecked")
        Map<String, String> rsp = JsonUtil.unMarshal(instRsp.getResponseContent(), Map.class);
        String jobId = rsp.get(CommonConstant.JOB_ID);
        if (ValidateUtil.isStrEmpty(jobId)) {
            LOGGER.error("Invalid jobId from instantiate operation");
            status.setStatus(RequestsDbConstant.Status.ERROR);
            status.setErrorCode(String.valueOf(instRsp.getStatus()));
            status.setStatusDescription(CommonConstant.StatusDesc.INSTANTIATE_NS_FAILED);
            resourceOperationStatusRepository.save(status);
            throw new ApplicationException(HttpCode.INTERNAL_SERVER_ERROR,
                    DriverExceptionID.INVALID_RESPONSE_FROM_INSTANTIATE_OPERATION);
        }
        LOGGER.info("instantiate ns -> end");
        // Step 3: update segment operation job id
        LOGGER.info("update resource operation status job id -> begin");
        status.setJobId(jobId);
        status.setProgress("100");
        status.setStatusDescription("NS initiation completed.");
        resourceOperationStatusRepository.save(status);
        LOGGER.info("update segment operation job id -> end");

        return instRsp;
    }

    /**
     * terminate network service <br>
     * 
     * @param nsOperationKey The operation key for NS resource
     * @param nsInstanceId The NS instance id
     * @return
     * @since ONAP Amsterdam Release
     */
    public RestfulResponse terminateNs(NsOperationKey nsOperationKey, String nsInstanceId) throws ApplicationException {
        // Step1: save segment operation info for delete process
        LOGGER.info("save segment operation for delete process");
        ResourceOperationStatus status = new ResourceOperationStatus(nsOperationKey.getServiceId(),
                nsOperationKey.getOperationId(), nsOperationKey.getNodeTemplateUUID());
        status.setStatus(RequestsDbConstant.Status.PROCESSING);
        resourceOperationStatusRepository.save(status);

        LOGGER.info("terminate ns -> begin");
        // Step2: prepare url and method type
        String url = getUrl(nsInstanceId, CommonConstant.Step.TERMINATE);
        String methodType = CommonConstant.MethodType.POST;

        // Step3: prepare restful parameters and options
        Map<String, String> reqBody = new HashMap<>();
        reqBody.put("nsInstanceId", nsInstanceId);
        reqBody.put("terminationType", "graceful");
        reqBody.put("gracefulTerminationTimeout", "60");

        // Step4: Call the NFVO or SDNO service to terminate service
        RestfulResponse terminateRsp = restfulUtil.send(url, methodType, JsonUtil.marshal(reqBody));
        ValidateUtil.assertObjectNotNull(terminateRsp);
        LOGGER.info("terminate ns response status is : {}", terminateRsp.getStatus());
        LOGGER.info("terminate ns response content is : {}", terminateRsp.getResponseContent());
        // Step 3: update segment operation
        if (!HttpCode.isSucess(terminateRsp.getStatus())) {
            LOGGER.error("fail to instantiate ns");
            status.setStatus(RequestsDbConstant.Status.ERROR);
            status.setErrorCode(String.valueOf(terminateRsp.getStatus()));
            status.setStatusDescription(CommonConstant.StatusDesc.TERMINATE_NS_FAILED);
            resourceOperationStatusRepository.save(status);

            throw new ApplicationException(HttpCode.INTERNAL_SERVER_ERROR, DriverExceptionID.FAIL_TO_TERMINATE_NS);
        }
        @SuppressWarnings("unchecked")
        Map<String, String> rsp = JsonUtil.unMarshal(terminateRsp.getResponseContent(), Map.class);
        String jobId = rsp.get(CommonConstant.JOB_ID);
        if (ValidateUtil.isStrEmpty(jobId)) {
            LOGGER.error("Invalid jobId from terminate operation");
            status.setStatus(RequestsDbConstant.Status.ERROR);
            status.setErrorCode(String.valueOf(terminateRsp.getStatus()));
            status.setStatusDescription(CommonConstant.StatusDesc.TERMINATE_NS_FAILED);
            resourceOperationStatusRepository.save(status);
            throw new ApplicationException(HttpCode.INTERNAL_SERVER_ERROR,
                    DriverExceptionID.INVALID_RESPONSE_FROM_TERMINATE_OPERATION);
        }
        LOGGER.info("terminate ns -> end");

        LOGGER.info("update segment job id -> begin");
        status.setProgress("60");
        status.setStatusDescription("NS is termination completed");
        status.setJobId(jobId);
        resourceOperationStatusRepository.save(status);
        LOGGER.info("update segment job id -> end");

        return terminateRsp;
    }

    /**
     * get ns progress by job Id <br>
     * 
     * @param nsOperationKey The OperationKey for NS resource
     * @param jobId the job id
     * @return
     * @since ONAP Amsterdam Release
     */
    public RestfulResponse getNsProgress(NsOperationKey nsOperationKey, String jobId) throws ApplicationException {

        ValidateUtil.assertObjectNotNull(jobId);
        // Step 1: query the current resource operation status
        ResourceOperationStatus status = new ResourceOperationStatus(nsOperationKey.getServiceId(),
                nsOperationKey.getOperationId(), nsOperationKey.getNodeTemplateUUID());
        status = resourceOperationStatusRepository.findOne(Example.of(status))
                .orElseThrow(() -> new ApplicationException(404, "Cannot Find Operation Status"));
        // Step 2: start query
        LOGGER.info("query ns status -> begin");
        String url = getUrl(jobId, CommonConstant.Step.QUERY);
        String methodType = CommonConstant.MethodType.GET;
        // prepare restful parameters and options
        RestfulResponse rsp = restfulUtil.send(url, methodType, "");
        ValidateUtil.assertObjectNotNull(rsp);
        LOGGER.info("query ns progress response status is : {}", rsp.getStatus());
        LOGGER.info("query ns progress response content is : {}", rsp.getResponseContent());
        // Step 3:check the response staus
        if (!HttpCode.isSucess(rsp.getStatus())) {
            LOGGER.info("fail to query job status");
            status.setErrorCode(String.valueOf(rsp.getStatus()));
            status.setStatus(RequestsDbConstant.Status.ERROR);
            status.setStatusDescription(CommonConstant.StatusDesc.QUERY_JOB_STATUS_FAILED);
            resourceOperationStatusRepository.save(status);
            throw new ApplicationException(HttpCode.INTERNAL_SERVER_ERROR, DriverExceptionID.FAIL_TO_QUERY_JOB_STATUS);
        }
        // Step 4: Process Network Service Instantiate Response
        NsProgressStatus nsProgress = JsonUtil.unMarshal(rsp.getResponseContent(), NsProgressStatus.class);
        ResponseDescriptor rspDesc = nsProgress.getResponseDescriptor();
        // Step 5: update segment operation progress

        status.setProgress(rspDesc.getProgress());
        status.setStatusDescription(rspDesc.getStatusDescription());
        resourceOperationStatusRepository.save(status);

        // Step 6: update segment operation status
        if (RequestsDbConstant.Progress.ONE_HUNDRED.equals(rspDesc.getProgress())
                && RequestsDbConstant.Status.FINISHED.equals(rspDesc.getStatus())) {
            LOGGER.info("job result is succeeded, operType is {}", status.getOperType());
            status.setErrorCode(String.valueOf(rsp.getStatus()));
            status.setStatusDescription(CommonConstant.StatusDesc.QUERY_JOB_STATUS_FAILED);

            if (RequestsDbConstant.OperationType.CREATE.equalsIgnoreCase(status.getOperType())
                    || "createInstance".equalsIgnoreCase(status.getOperType())) {
                status.setStatus(RequestsDbConstant.Status.FINISHED);
            }
            resourceOperationStatusRepository.save(status);
        } else if (RequestsDbConstant.Status.ERROR.equals(rspDesc.getStatus())) {
            LOGGER.error("job result is failed, operType is {}", status.getOperType());
            status.setErrorCode(String.valueOf(rsp.getStatus()));
            status.setStatusDescription(CommonConstant.StatusDesc.QUERY_JOB_STATUS_FAILED);
            status.setStatus(RequestsDbConstant.Status.ERROR);
            resourceOperationStatusRepository.save(status);
            throw new ApplicationException(HttpCode.INTERNAL_SERVER_ERROR, DriverExceptionID.JOB_STATUS_ERROR);
        } else {
            LOGGER.error("unexcepted response status");
        }
        LOGGER.info("query ns status -> end");

        return rsp;
    }

    /**
     * Scale NS instance <br>
     * 
     * @param nsInstanceId The NS instance id
     * @param segInput input parameters for current node from http request
     * @return
     * @since ONAP Amsterdam Release
     */
    public RestfulResponse scaleNs(String nsInstanceId, NSResourceInputParameter segInput) throws ApplicationException {
        // Call the NFVO to scale service
        LOGGER.info("scale ns -> begin");

        // Step1: Prepare restful parameters and options
        VFCScaleData oRequest = new VFCScaleData();
        oRequest.setNsInstanceId(nsInstanceId);
        NsScaleParameters nsScaleParameters = segInput.getNsScaleParameters();
        oRequest.setScaleType(nsScaleParameters.getScaleType());
        oRequest.setScaleNsData(nsScaleParameters.getScaleNsByStepsData());
        String scaleReq = JsonUtil.marshal(oRequest);

        // Step2: prepare url and method type
        String url = getUrl(nsInstanceId, CommonConstant.Step.SCALE);
        String methodType = CommonConstant.MethodType.POST;
        LOGGER.info("scale ns request is {}", scaleReq);
        // Step3: Call NFVO lcm to scale ns
        RestfulResponse scaleRsp = restfulUtil.send(url, methodType, scaleReq);

        ResourceOperationStatus status = new ResourceOperationStatus(segInput.getNsOperationKey().getServiceId(),
                segInput.getNsOperationKey().getOperationId(), segInput.getNsOperationKey().getNodeTemplateUUID());
        ResourceOperationStatus nsOperInfo = resourceOperationStatusRepository.findOne(Example.of(status))
                .orElseThrow(() -> new ApplicationException(404, "Cannot Find Operation Status"));
        ValidateUtil.assertObjectNotNull(scaleRsp);
        if (!HttpCode.isSucess(scaleRsp.getStatus())) {
            LOGGER.error("update segment operation status : fail to scale ns");
            nsOperInfo.setStatus(RequestsDbConstant.Status.ERROR);
            nsOperInfo.setErrorCode(String.valueOf(scaleRsp.getStatus()));
            nsOperInfo.setStatusDescription(CommonConstant.StatusDesc.SCALE_NS_FAILED);
            resourceOperationStatusRepository.save(nsOperInfo);
            throw new ApplicationException(HttpCode.INTERNAL_SERVER_ERROR, DriverExceptionID.FAIL_TO_SCALE_NS);
        }
        LOGGER.info("scale ns response status is {}", scaleRsp.getStatus());
        LOGGER.info("scale ns response content is {}", scaleRsp.getResponseContent());

        ValidateUtil.assertObjectNotNull(scaleRsp.getResponseContent());
        @SuppressWarnings("unchecked")
        Map<String, String> rsp = JsonUtil.unMarshal(scaleRsp.getResponseContent(), Map.class);
        String jobId = rsp.get(CommonConstant.JOB_ID);
        if (ValidateUtil.isStrEmpty(jobId)) {
            LOGGER.error("Invalid jobId from scale operation");
            nsOperInfo.setStatus(RequestsDbConstant.Status.ERROR);
            nsOperInfo.setErrorCode(String.valueOf(scaleRsp.getStatus()));
            nsOperInfo.setStatusDescription(CommonConstant.StatusDesc.SCALE_NS_FAILED);
            resourceOperationStatusRepository.save(nsOperInfo);
            throw new ApplicationException(HttpCode.INTERNAL_SERVER_ERROR,
                    DriverExceptionID.INVALID_RESPONSE_FROM_SCALE_OPERATION);
        }

        LOGGER.info("update resource operation status job id -> begin");
        // Step 4: update segment operation job id
        nsOperInfo.setJobId(jobId);
        resourceOperationStatusRepository.save(nsOperInfo);
        LOGGER.info("update segment operation job id -> end");
        LOGGER.info("scale ns -> end");

        return scaleRsp;
    }

    /**
     * get url for the operation <br>
     * 
     * @param variable variable should be put in the url
     * @param step step of the operation (terminate,query,delete)
     * @return
     * @since ONAP Amsterdam Release
     */
    private String getUrl(String variable, String step) {

        String url;
        String originalUrl;
        originalUrl = nfvoUrlMap.get(step);
        url = String.format(originalUrl, variable);
        return url;

    }

}
