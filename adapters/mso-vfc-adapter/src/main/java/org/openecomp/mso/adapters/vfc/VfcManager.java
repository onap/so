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

package org.openecomp.mso.adapters.vfc;

import java.util.HashMap;
import java.util.Map;

import org.openecomp.mso.adapters.vfc.constant.CommonConstant;
import org.openecomp.mso.adapters.vfc.constant.CommonConstant.Step;
import org.openecomp.mso.adapters.vfc.constant.DriverExceptionID;
import org.openecomp.mso.adapters.vfc.constant.HttpCode;
import org.openecomp.mso.adapters.vfc.exceptions.ApplicationException;
import org.openecomp.mso.adapters.vfc.model.CustomerModel;
import org.openecomp.mso.adapters.vfc.model.NSResourceInputParameter;
import org.openecomp.mso.adapters.vfc.model.NsCreateReq;
import org.openecomp.mso.adapters.vfc.model.NsInstantiateReq;
import org.openecomp.mso.adapters.vfc.model.NsOperationKey;
import org.openecomp.mso.adapters.vfc.model.NsParameters;
import org.openecomp.mso.adapters.vfc.model.NsProgressStatus;
import org.openecomp.mso.adapters.vfc.model.ResponseDescriptor;
import org.openecomp.mso.adapters.vfc.model.RestfulResponse;
import org.openecomp.mso.adapters.vfc.util.JsonUtil;
import org.openecomp.mso.adapters.vfc.util.RestfulUtil;
import org.openecomp.mso.adapters.vfc.util.ValidateUtil;
import org.openecomp.mso.requestsdb.RequestsDatabase;
import org.openecomp.mso.requestsdb.RequestsDbConstant;
import org.openecomp.mso.requestsdb.ResourceOperationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * VF-C Manager <br>
 * <p>
 * </p>
 * 
 * @author
 * @version ONAP Amsterdam Release 2017-08-28
 */
public class VfcManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(VfcManager.class);

  /**
   * nfvo url map
   */
  private static Map<String, String> nfvoUrlMap;

  static {
    nfvoUrlMap = new HashMap<>();
    nfvoUrlMap.put(Step.CREATE, CommonConstant.NFVO_CREATE_URL);
    nfvoUrlMap.put(Step.INSTANTIATE, CommonConstant.NFVO_INSTANTIATE_URL);
    nfvoUrlMap.put(Step.TERMINATE, CommonConstant.NFVO_TERMINATE_URL);
    nfvoUrlMap.put(Step.DELETE, CommonConstant.NFVO_DELETE_URL);
    nfvoUrlMap.put(Step.QUERY, CommonConstant.NFVO_QUERY_URL);
  }

  public VfcManager() {

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
    String csarId = segInput.getNsOperationKey().getNodeTemplateUUID();
    // nsdId for NFVO is "id" in the response, while for SDNO is "servcice template id"
    LOGGER.info("serviceTemplateId is {}, id is {}", csarId);

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
    RestfulResponse createRsp = RestfulUtil.send(url, methodType, createReq);
    ValidateUtil.assertObjectNotNull(createRsp);
    LOGGER.info("create ns response status is : {}", createRsp.getStatus());
    LOGGER.info("create ns response content is : {}", createRsp.getResponseContent());

    // Step 5: save resource operation information
    ResourceOperationStatus nsOperInfo = (RequestsDatabase.getInstance())
        .getResourceOperationStatus(segInput.getNsOperationKey().getServiceId(),
            segInput.getNsOperationKey().getOperationId(),
            segInput.getNsOperationKey().getNodeTemplateUUID());
    nsOperInfo.setStatus(RequestsDbConstant.Status.PROCESSING);
    (RequestsDatabase.getInstance()).updateResOperStatus(nsOperInfo);

    if (!HttpCode.isSucess(createRsp.getStatus())) {
      LOGGER.error("update segment operation status : fail to create ns");
      nsOperInfo.setStatus(RequestsDbConstant.Status.ERROR);
      nsOperInfo.setErrorCode(String.valueOf(createRsp.getStatus()));
      (RequestsDatabase.getInstance()).updateResOperStatus(nsOperInfo);
      throw new ApplicationException(HttpCode.INTERNAL_SERVER_ERROR,
          DriverExceptionID.FAIL_TO_CREATE_NS);
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
  public RestfulResponse deleteNs(NsOperationKey nsOperationKey, String nsInstanceId)
      throws ApplicationException {
    LOGGER.info("delete ns -> begin");
    // Step1: prepare url and methodType
    String url = getUrl(nsInstanceId, CommonConstant.Step.DELETE);
    String methodType = CommonConstant.MethodType.DELETE;

    // Step2: prepare restful parameters and options
    RestfulResponse deleteRsp = RestfulUtil.send(url, methodType, "");
    ValidateUtil.assertObjectNotNull(deleteRsp);
    LOGGER.info("delete ns response status is : {}", deleteRsp.getStatus());
    LOGGER.info("delete ns response content is : {}", deleteRsp.getResponseContent());
    LOGGER.info("delete ns -> end");
    ResourceOperationStatus nsOperInfo =
        (RequestsDatabase.getInstance()).getResourceOperationStatus(nsOperationKey.getServiceId(),
            nsOperationKey.getOperationId(), nsOperationKey.getNodeTemplateUUID());
    if (!HttpCode.isSucess(deleteRsp.getStatus())) {
      LOGGER.error("fail to delete ns");

      nsOperInfo.setStatus(RequestsDbConstant.Status.ERROR);
      nsOperInfo.setErrorCode(String.valueOf(deleteRsp.getStatus()));
      nsOperInfo.setStatusDescription(CommonConstant.StatusDesc.TERMINATE_NS_FAILED);
      (RequestsDatabase.getInstance()).updateResOperStatus(nsOperInfo);
      throw new ApplicationException(HttpCode.INTERNAL_SERVER_ERROR,
          DriverExceptionID.FAIL_TO_DELETE_NS);
    }

    // Step3: remove relation info between service and ns
    AaiUtil.removeRelation(nsOperationKey.getGlobalSubscriberId(), nsOperationKey.getServiceType(),
        nsOperationKey.getServiceId(), nsInstanceId);
    LOGGER.info("delete segment information -> end");

    // Step4: update service segment operation status
    nsOperInfo.setStatus(RequestsDbConstant.Status.FINISHED);
    nsOperInfo.setErrorCode(String.valueOf(deleteRsp.getStatus()));
    (RequestsDatabase.getInstance()).updateResOperStatus(nsOperInfo);
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

    RestfulResponse instRsp = RestfulUtil.send(url, methodType, instReq);    
    ResourceOperationStatus nsOperInfo = (RequestsDatabase.getInstance())
            .getResourceOperationStatus(segInput.getNsOperationKey().getServiceId(),
                segInput.getNsOperationKey().getOperationId(),
                segInput.getNsOperationKey().getNodeTemplateUUID());
    ValidateUtil.assertObjectNotNull(instRsp);
    if (!HttpCode.isSucess(instRsp.getStatus())) {
        LOGGER.error("update segment operation status : fail to instantiate ns");
        nsOperInfo.setStatus(RequestsDbConstant.Status.ERROR);
        nsOperInfo.setErrorCode(String.valueOf(instRsp.getStatus()));
        nsOperInfo.setStatusDescription(CommonConstant.StatusDesc.INSTANTIATE_NS_FAILED);
        (RequestsDatabase.getInstance()).updateResOperStatus(nsOperInfo);
        throw new ApplicationException(HttpCode.INTERNAL_SERVER_ERROR,
            DriverExceptionID.FAIL_TO_INSTANTIATE_NS);
      }
    LOGGER.info("instantiate ns response status is : {}", instRsp.getStatus());
    LOGGER.info("instantiate ns response content is : {}", instRsp.getResponseContent());
    ValidateUtil.assertObjectNotNull(instRsp.getResponseContent());    
    @SuppressWarnings("unchecked")
    Map<String, String> rsp = JsonUtil.unMarshal(instRsp.getResponseContent(), Map.class);
    String jobId = rsp.get(CommonConstant.JOB_ID);
    if (ValidateUtil.isStrEmpty(jobId)) {
      LOGGER.error("Invalid jobId from instantiate operation");
      nsOperInfo.setStatus(RequestsDbConstant.Status.ERROR);
      nsOperInfo.setErrorCode(String.valueOf(instRsp.getStatus()));
      nsOperInfo.setStatusDescription(CommonConstant.StatusDesc.INSTANTIATE_NS_FAILED);
      (RequestsDatabase.getInstance()).updateResOperStatus(nsOperInfo);
      throw new ApplicationException(HttpCode.INTERNAL_SERVER_ERROR,
          DriverExceptionID.INVALID_RESPONSE_FROM_INSTANTIATE_OPERATION);
    }
    LOGGER.info("instantiate ns -> end");
    // Step 3: update segment operation job id
    LOGGER.info("update resource operation status job id -> begin");
    nsOperInfo.setJobId(jobId);
    (RequestsDatabase.getInstance()).updateResOperStatus(nsOperInfo);
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
  public RestfulResponse terminateNs(NsOperationKey nsOperationKey, String nsInstanceId)
      throws ApplicationException {
    // Step1: save segment operation info for delete process
    LOGGER.info("save segment operation for delete process");
    ResourceOperationStatus nsOperInfo =
        (RequestsDatabase.getInstance()).getResourceOperationStatus(nsOperationKey.getServiceId(),
            nsOperationKey.getOperationId(), nsOperationKey.getNodeTemplateUUID());
    nsOperInfo.setStatus(RequestsDbConstant.Status.PROCESSING);
    (RequestsDatabase.getInstance()).updateResOperStatus(nsOperInfo);

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
    RestfulResponse terminateRsp = RestfulUtil.send(url, methodType, JsonUtil.marshal(reqBody));
    ValidateUtil.assertObjectNotNull(terminateRsp);
    LOGGER.info("terminate ns response status is : {}", terminateRsp.getStatus());
    LOGGER.info("terminate ns response content is : {}", terminateRsp.getResponseContent());
    // Step 3: update segment operation
    if (!HttpCode.isSucess(terminateRsp.getStatus())) {
      LOGGER.error("fail to instantiate ns");
      nsOperInfo.setStatus(RequestsDbConstant.Status.ERROR);
      nsOperInfo.setErrorCode(String.valueOf(terminateRsp.getStatus()));
      nsOperInfo.setStatusDescription(CommonConstant.StatusDesc.TERMINATE_NS_FAILED);
      (RequestsDatabase.getInstance()).updateResOperStatus(nsOperInfo);

      throw new ApplicationException(HttpCode.INTERNAL_SERVER_ERROR,
          DriverExceptionID.FAIL_TO_TERMINATE_NS);
    }
    @SuppressWarnings("unchecked")
    Map<String, String> rsp = JsonUtil.unMarshal(terminateRsp.getResponseContent(), Map.class);
    String jobId = rsp.get(CommonConstant.JOB_ID);
    if (ValidateUtil.isStrEmpty(jobId)) {
      LOGGER.error("Invalid jobId from terminate operation");
      nsOperInfo.setStatus(RequestsDbConstant.Status.ERROR);
      nsOperInfo.setErrorCode(String.valueOf(terminateRsp.getStatus()));
      nsOperInfo.setStatusDescription(CommonConstant.StatusDesc.TERMINATE_NS_FAILED);
      (RequestsDatabase.getInstance()).updateResOperStatus(nsOperInfo);
      throw new ApplicationException(HttpCode.INTERNAL_SERVER_ERROR,
          DriverExceptionID.INVALID_RESPONSE_FROM_TERMINATE_OPERATION);
    }
    LOGGER.info("terminate ns -> end");

    LOGGER.info("update segment job id -> begin");
    nsOperInfo.setJobId(jobId);
    (RequestsDatabase.getInstance()).updateResOperStatus(nsOperInfo);
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
  public RestfulResponse getNsProgress(NsOperationKey nsOperationKey, String jobId)
      throws ApplicationException {

    ValidateUtil.assertObjectNotNull(jobId);
    // Step 1: query the current resource operation status
    ResourceOperationStatus nsOperInfo =
        (RequestsDatabase.getInstance()).getResourceOperationStatus(nsOperationKey.getServiceId(),
            nsOperationKey.getOperationId(), nsOperationKey.getNodeTemplateUUID());

    // Step 2: start query
    LOGGER.info("query ns status -> begin");
    String url = getUrl(jobId, CommonConstant.Step.QUERY);
    String methodType = CommonConstant.MethodType.GET;
    // prepare restful parameters and options
    RestfulResponse rsp = RestfulUtil.send(url, methodType, "");
    ValidateUtil.assertObjectNotNull(rsp);
    LOGGER.info("query ns progress response status is : {}", rsp.getStatus());
    LOGGER.info("query ns progress response content is : {}", rsp.getResponseContent());
    // Step 3:check the response staus
    if (!HttpCode.isSucess(rsp.getStatus())) {
      LOGGER.info("fail to query job status");
      nsOperInfo.setErrorCode(String.valueOf(rsp.getStatus()));
      nsOperInfo.setStatus(RequestsDbConstant.Status.ERROR);
      nsOperInfo.setStatusDescription(CommonConstant.StatusDesc.QUERY_JOB_STATUS_FAILED);
      (RequestsDatabase.getInstance()).updateResOperStatus(nsOperInfo);
      throw new ApplicationException(HttpCode.INTERNAL_SERVER_ERROR,
          DriverExceptionID.FAIL_TO_QUERY_JOB_STATUS);
    }
    // Step 4: Process Network Service Instantiate Response
    NsProgressStatus nsProgress =
        JsonUtil.unMarshal(rsp.getResponseContent(), NsProgressStatus.class);
    ResponseDescriptor rspDesc = nsProgress.getResponseDescriptor();
    // Step 5: update segment operation progress

    nsOperInfo.setProgress(rspDesc.getProgress());
    nsOperInfo.setStatusDescription(rspDesc.getStatusDescription());
    (RequestsDatabase.getInstance()).updateResOperStatus(nsOperInfo);

    // Step 6: update segment operation status
    if (RequestsDbConstant.Progress.ONE_HUNDRED.equals(rspDesc.getProgress())
        && RequestsDbConstant.Status.FINISHED.equals(rspDesc.getStatus())) {
      LOGGER.info("job result is succeeded, operType is {}", nsOperInfo.getOperType());
      nsOperInfo.setErrorCode(String.valueOf(rsp.getStatus()));
      nsOperInfo.setStatusDescription(CommonConstant.StatusDesc.QUERY_JOB_STATUS_FAILED);

            if(RequestsDbConstant.OperationType.CREATE.equalsIgnoreCase(nsOperInfo.getOperType())) {
        nsOperInfo.setStatus(RequestsDbConstant.Status.FINISHED);
      }
      (RequestsDatabase.getInstance()).updateResOperStatus(nsOperInfo);
    } else if (RequestsDbConstant.Status.ERROR.equals(rspDesc.getStatus())) {
      LOGGER.error("job result is failed, operType is {}", nsOperInfo.getOperType());
      nsOperInfo.setErrorCode(String.valueOf(rsp.getStatus()));
      nsOperInfo.setStatusDescription(CommonConstant.StatusDesc.QUERY_JOB_STATUS_FAILED);
      nsOperInfo.setStatus(RequestsDbConstant.Status.ERROR);
      (RequestsDatabase.getInstance()).updateResOperStatus(nsOperInfo);
      throw new ApplicationException(HttpCode.INTERNAL_SERVER_ERROR,
          DriverExceptionID.JOB_STATUS_ERROR);
    } else {
      LOGGER.error("unexcepted response status");
    }
    LOGGER.info("query ns status -> end");

    return rsp;
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
    originalUrl = (String) nfvoUrlMap.get(step);
    url = String.format(originalUrl, variable);
    return url;

  }

}
