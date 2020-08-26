/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 # Copyright (c) 2020, CMCC Technologies Co., Ltd.
 #
 # Licensed under the Apache License, Version 2.0 (the "License")
 # you may not use this file except in compliance with the License.
 # You may obtain a copy of the License at
 #
 #       http://www.apache.org/licenses/LICENSE-2.0
 #
 # Unless required by applicable law or agreed to in writing, software
 # distributed under the License is distributed on an "AS IS" BASIS,
 # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 # See the License for the specific language governing permissions and
 # limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.adapters.nssmf.manager.impl;

import org.onap.so.adapters.nssmf.config.NssmfAdapterConfig;
import org.onap.so.adapters.nssmf.consts.NssmfAdapterConsts;
import org.onap.so.adapters.nssmf.entity.NssmfUrlInfo;
import org.onap.so.adapters.nssmf.enums.*;
import org.onap.so.adapters.nssmf.exceptions.ApplicationException;
import org.onap.so.adapters.nssmf.entity.RestResponse;
import org.onap.so.adapters.nssmf.manager.NssmfManager;
import org.onap.so.adapters.nssmf.util.RestUtil;
import org.onap.so.beans.nsmf.*;
import org.onap.so.db.request.beans.ResourceOperationStatus;
import org.onap.so.db.request.data.repository.ResourceOperationStatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Example;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import static org.onap.so.adapters.nssmf.util.NssmfAdapterUtil.StatusDesc.ALLOCATE_NSS_SUCCESS;
import static org.onap.so.adapters.nssmf.util.NssmfAdapterUtil.StatusDesc.MODIFY_NSS_SUCCESS;
import static org.onap.so.adapters.nssmf.util.NssmfAdapterUtil.marshal;

public abstract class BaseNssmfManager implements NssmfManager {

    private static final Logger logger = LoggerFactory.getLogger(BaseNssmfManager.class);

    protected RestUtil restUtil;

    protected ResourceOperationStatusRepository repository;

    protected NssmfAdapterConfig adapterConfig;

    protected ActionType actionType;

    protected EsrInfo esrInfo;

    protected String nssmfUrl;

    protected HttpMethod httpMethod;

    protected String initStatus;

    protected ServiceInfo serviceInfo;

    protected RestResponse restResponse;

    private ExecutorType executorType = ExecutorType.INTERNAL;

    private Map<String, String> params = new HashMap<>(); // request params

    @Override
    public RestResponse allocateNssi(NssmfAdapterNBIRequest nbiRequest) throws ApplicationException {

        this.params.clear();
        this.urlHandler();
        String requestBody = wrapAllocateReqBody(nbiRequest);

        this.restResponse = sendRequest(requestBody);

        this.afterRequest();

        return restResponse;
    }

    protected abstract String wrapAllocateReqBody(NssmfAdapterNBIRequest nbiRequest) throws ApplicationException;

    @Override
    public RestResponse modifyNssi(NssmfAdapterNBIRequest modifyRequest) throws ApplicationException {
        this.params.clear();
        this.urlHandler();
        String requestBody = wrapModifyReqBody(modifyRequest);

        this.restResponse = sendRequest(requestBody);

        this.afterRequest();

        return restResponse;
    }

    protected abstract String wrapModifyReqBody(NssmfAdapterNBIRequest nbiRequest) throws ApplicationException;

    @Override
    public RestResponse deAllocateNssi(NssmfAdapterNBIRequest nbiRequest, String sliceId) throws ApplicationException {
        this.params.clear();
        this.params.put("sliceProfileId", sliceId);

        this.urlHandler();

        String reqBody = wrapDeAllocateReqBody(nbiRequest.getDeAllocateNssi());

        this.restResponse = sendRequest(reqBody);

        this.afterRequest();

        return restResponse;
    }

    protected abstract String wrapDeAllocateReqBody(DeAllocateNssi deAllocateNssi) throws ApplicationException;

    protected abstract String wrapReqBody(Object object) throws ApplicationException;

    @Override
    public RestResponse activateNssi(NssmfAdapterNBIRequest nbiRequest, String snssai) throws ApplicationException {
        this.params.clear();
        this.params.put("snssai", snssai);

        this.urlHandler();

        String reqBody = wrapActDeActReqBody(nbiRequest.getActDeActNssi());

        this.restResponse = sendRequest(reqBody);

        this.afterRequest();

        return restResponse;
    }

    @Override
    public RestResponse deActivateNssi(NssmfAdapterNBIRequest nbiRequest, String snssai) throws ApplicationException {
        return activateNssi(nbiRequest, snssai);
    }

    protected abstract String wrapActDeActReqBody(ActDeActNssi actDeActNssi) throws ApplicationException;

    @Override
    public RestResponse queryJobStatus(NssmfAdapterNBIRequest jobReq, String jobId) throws ApplicationException {
        this.params.clear();
        this.params.put("jobId", jobId);
        this.params.put("responseId", jobReq.getResponseId());
        this.urlHandler();

        /**
         * find by jobId and nsiId jobId -> OperationId nsiId -> ServiceId serviceUuid -> resourceTemplateUUID
         */
        ResourceOperationStatus status =
                getOperationStatus(serviceInfo.getNsiId(), jobId, serviceInfo.getServiceUuid());

        this.restResponse = doQueryJobStatus(status);

        afterQueryJobStatus(status);
        return restResponse;
    }

    protected abstract RestResponse doQueryJobStatus(ResourceOperationStatus status) throws ApplicationException;


    protected abstract void afterQueryJobStatus(ResourceOperationStatus status);

    private ResourceOperationStatus getOperationStatus(String nsiId, String jobId, String serviceUuid) {

        ResourceOperationStatus status = new ResourceOperationStatus(nsiId, jobId, serviceUuid);

        Optional<ResourceOperationStatus> optional = repository.findOne(Example.of(status));

        return optional.orElse(null);
    }

    @Override
    public RestResponse queryNSSISelectionCapability(NssmfAdapterNBIRequest nbiRequest) throws ApplicationException {
        SelectionType res = doQueryNSSISelectionCapability();
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("selection", res.name());
        RestResponse restResponse = new RestResponse();
        restResponse.setStatus(200);
        restResponse.setResponseContent(marshal(hashMap));
        return restResponse;
    }

    protected abstract SelectionType doQueryNSSISelectionCapability();

    @Override
    public RestResponse querySubnetCapability(NssmfAdapterNBIRequest nbiRequest) throws ApplicationException {
        this.params.clear();
        this.urlHandler();

        return doQuerySubnetCapability(nbiRequest.getSubnetCapabilityQuery());
    }

    protected abstract RestResponse doQuerySubnetCapability(String req) throws ApplicationException;

    /**
     * send request to nssmf
     * 
     * @param content request body
     * @return response
     * @throws ApplicationException
     */
    protected abstract RestResponse sendRequest(String content) throws ApplicationException;

    /**
     * handle the url before request to nssmf, include get the nssmf request url, replace the path variable
     */
    private void urlHandler() {
        NssmfUrlInfo nssmfUrlInfo =
                NssmfAdapterConsts.getNssmfUrlInfo(this.executorType, this.esrInfo.getNetworkType(), actionType);
        this.nssmfUrl = nssmfUrlInfo.getUrl();
        this.httpMethod = nssmfUrlInfo.getHttpMethod();
        this.nssmfUrl = nssmfUrl.replaceAll("\\{apiVersion}", getApiVersion());
        this.params.forEach((k, v) -> this.nssmfUrl = this.nssmfUrl.replaceAll("\\{" + k + "}", v));
    }

    /**
     * after request
     */
    protected abstract void afterRequest() throws ApplicationException;

    protected abstract String getApiVersion();

    public RestUtil getRestUtil() {
        return restUtil;
    }

    public BaseNssmfManager setEsrInfo(EsrInfo esrInfo) {
        this.esrInfo = esrInfo;
        return this;
    }

    public BaseNssmfManager setExecutorType(ExecutorType executorType) {
        this.executorType = executorType;
        return this;
    }

    public BaseNssmfManager setRestUtil(RestUtil restUtil) {
        this.restUtil = restUtil;
        return this;
    }

    public BaseNssmfManager setActionType(ActionType actionType) {
        this.actionType = actionType;
        return this;
    }

    public BaseNssmfManager setRepository(ResourceOperationStatusRepository repository) {
        this.repository = repository;
        return this;
    }

    public BaseNssmfManager setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
        return this;
    }

    public BaseNssmfManager setInitStatus(String initStatus) {
        this.initStatus = initStatus;
        return this;
    }

    public BaseNssmfManager setAdapterConfig(NssmfAdapterConfig adapterConfig) {
        this.adapterConfig = adapterConfig;
        return this;
    }
}
