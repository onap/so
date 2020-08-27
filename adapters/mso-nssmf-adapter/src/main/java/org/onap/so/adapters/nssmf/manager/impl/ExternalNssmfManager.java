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

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.so.adapters.nssmf.entity.NssmfInfo;
import org.onap.so.adapters.nssmf.entity.RestResponse;
import org.onap.so.adapters.nssmf.enums.JobStatus;
import org.onap.so.adapters.nssmf.exceptions.ApplicationException;
import org.onap.so.adapters.nssmf.util.NssmfAdapterUtil;
import org.onap.so.beans.nsmf.*;
import org.onap.so.db.request.beans.ResourceOperationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static java.lang.String.valueOf;
import static org.onap.so.adapters.nssmf.enums.JobStatus.*;
import static org.onap.so.adapters.nssmf.util.NssmfAdapterUtil.StatusDesc.*;
import static org.onap.so.adapters.nssmf.util.NssmfAdapterUtil.marshal;
import static org.onap.so.adapters.nssmf.util.NssmfAdapterUtil.unMarshal;

public abstract class ExternalNssmfManager extends BaseNssmfManager {

    private static final Logger logger = LoggerFactory.getLogger(ExternalNssmfManager.class);

    @Override
    protected String wrapAllocateReqBody(NssmfAdapterNBIRequest nbiRequest) throws ApplicationException {
        return doWrapExtAllocateReqBody(nbiRequest);
    }

    protected abstract String doWrapExtAllocateReqBody(NssmfAdapterNBIRequest nbiRequest) throws ApplicationException;

    @Override
    protected String wrapModifyReqBody(NssmfAdapterNBIRequest nbiRequest) throws ApplicationException {
        return doWrapModifyReqBody(nbiRequest);
    }

    protected abstract String doWrapModifyReqBody(NssmfAdapterNBIRequest nbiRequest) throws ApplicationException;

    @Override
    protected String wrapDeAllocateReqBody(DeAllocateNssi deAllocateNssi) throws ApplicationException {
        return doWrapDeAllocateReqBody(deAllocateNssi);
    }

    protected abstract String doWrapDeAllocateReqBody(DeAllocateNssi deAllocateNssi) throws ApplicationException;

    @Override
    protected void afterQueryJobStatus(ResourceOperationStatus status) {
        if (Integer.parseInt(status.getProgress()) == 100) {

            ServiceInstance nssiInstance = new ServiceInstance();
            nssiInstance.setServiceInstanceId(serviceInfo.getNssiId());
            nssiInstance.setServiceInstanceName(serviceInfo.getNssiName());
            nssiInstance.setServiceType(serviceInfo.getSST());

            nssiInstance.setOrchestrationStatus(initStatus);
            nssiInstance.setModelInvariantId(serviceInfo.getServiceInvariantUuid());
            nssiInstance.setModelVersionId(serviceInfo.getServiceUuid());
            nssiInstance.setServiceInstanceLocationId(serviceInfo.getPLMNIdList());
            nssiInstance.setEnvironmentContext(esrInfo.getNetworkType().getNetworkType());
            nssiInstance.setServiceRole("nssi");

            restUtil.createServiceInstance(nssiInstance, serviceInfo);
        }
    }



    @Override
    protected String wrapActDeActReqBody(ActDeActNssi actDeActNssi) throws ApplicationException {
        return marshal(actDeActNssi);
    }

    protected RestResponse doQueryJobStatus(ResourceOperationStatus status) throws ApplicationException {
        return doResponseStatus(status);
    }

    private RestResponse doResponseStatus(ResourceOperationStatus status) throws ApplicationException {
        RestResponse restResponse = sendRequest(null);
        ResponseDescriptor rspDesc =
                unMarshal(restResponse.getResponseContent(), JobStatusResponse.class).getResponseDescriptor();
        updateRequestDbJobStatus(rspDesc, status, restResponse);
        return restResponse;
    }

    @Override
    protected String wrapReqBody(Object object) throws ApplicationException {
        return marshal(object);
    }

    @Override
    protected RestResponse sendRequest(String content) throws ApplicationException {
        return sendExternalRequest(content);
    }

    protected void createStatus(JobStatus jobStatus) throws ApplicationException {
        if (valueOf(restResponse.getStatus()).startsWith("2")) {
            logger.info("save segment and operaton info -> begin");
            NssiResponse response = unMarshal(restResponse.getResponseContent(), NssiResponse.class);
            ResourceOperationStatus status = new ResourceOperationStatus(serviceInfo.getNsiId(), response.getJobId(),
                    serviceInfo.getServiceUuid());
            status.setResourceInstanceID(response.getNssiId());

            updateDbStatus(status, restResponse.getStatus(), jobStatus, NssmfAdapterUtil.getStatusDesc(actionType));
            logger.info("save segment and operaton info -> end");
        }
    }

    @Override
    protected String getApiVersion() {
        return "v1";
    }


    // external
    protected RestResponse sendExternalRequest(String content) throws ApplicationException {
        NssmfInfo nssmfInfo = restUtil.getNssmfHost(esrInfo);
        Header header = new BasicHeader("X-Auth-Token", restUtil.getToken(nssmfInfo));
        String nssmfUrl = nssmfInfo.getUrl() + this.nssmfUrl;
        return restUtil.send(nssmfUrl, this.httpMethod, content, header);
    }

    private void updateRequestDbJobStatus(ResponseDescriptor rspDesc, ResourceOperationStatus status, RestResponse rsp)
            throws ApplicationException {

        switch (fromString(rspDesc.getStatus())) {
            case STARTED:
                updateDbStatus(status, rsp.getStatus(), STARTED, QUERY_JOB_STATUS_SUCCESS);
                break;
            case PROCESSING:
                updateDbStatus(status, rsp.getStatus(), PROCESSING, QUERY_JOB_STATUS_SUCCESS);
                break;
            case FINISHED:
                if (rspDesc.getProgress() == 100) {
                    updateDbStatus(status, rsp.getStatus(), FINISHED, QUERY_JOB_STATUS_SUCCESS);
                }
                break;
            case ERROR:
                updateDbStatus(status, rsp.getStatus(), ERROR, QUERY_JOB_STATUS_FAILED);
                throw new ApplicationException(500, QUERY_JOB_STATUS_FAILED);
        }
    }

    protected void updateDbStatus(ResourceOperationStatus status, int rspStatus, JobStatus jobStatus,
            String description) {
        status.setErrorCode(valueOf(rspStatus));
        status.setStatus(jobStatus.toString());
        status.setStatusDescription(description);
        logger.info("Updating DB status");
        repository.save(status);
        logger.info("Updating successful");
    }

    @Override
    protected RestResponse doQuerySubnetCapability(String req) throws ApplicationException {
        RestResponse response = new RestResponse();
        response.setStatus(200);
        response.setResponseContent(null);
        return response;
    }

    /**
     * after request, if response code is 2XX, continue handle, else return
     */
    @Override
    protected void afterRequest() throws ApplicationException {
        if (valueOf(restResponse.getStatus()).startsWith("2")) {
            doAfterRequest();
        }
    }


    protected void doAfterRequest() throws ApplicationException {
        //
        NssiResponse response = unMarshal(restResponse.getResponseContent(), NssiResponse.class);
        ResourceOperationStatus status =
                new ResourceOperationStatus(serviceInfo.getNsiId(), response.getJobId(), serviceInfo.getServiceUuid());
        status.setResourceInstanceID(response.getNssiId());

        updateDbStatus(status, restResponse.getStatus(), STARTED, NssmfAdapterUtil.getStatusDesc(actionType));
    }
}
