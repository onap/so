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
import org.onap.so.adapters.nssmf.consts.NssmfAdapterConsts;
import org.onap.so.adapters.nssmf.entity.RestResponse;
import org.onap.so.adapters.nssmf.enums.JobStatus;
import org.onap.so.adapters.nssmf.enums.SelectionType;
import org.onap.so.adapters.nssmf.exceptions.ApplicationException;
import org.onap.so.beans.nsmf.*;
import org.onap.so.db.request.beans.ResourceOperationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.onap.so.adapters.nssmf.enums.JobStatus.PROCESSING;
import static org.onap.so.adapters.nssmf.util.NssmfAdapterUtil.marshal;

public abstract class InternalNssmfManager extends BaseNssmfManager {

    private static final Logger logger = LoggerFactory.getLogger(InternalNssmfManager.class);

    @Override
    protected String wrapAllocateReqBody(NssmfAdapterNBIRequest nbiRequest) throws ApplicationException {
        return doWrapAllocateReqBody(nbiRequest);
    }

    protected abstract String doWrapAllocateReqBody(NssmfAdapterNBIRequest nbiRequest) throws ApplicationException;

    @Override
    protected String wrapReqBody(Object object) throws ApplicationException {
        NssmfRequest nssmfRequest = new NssmfRequest(serviceInfo, esrInfo.getNetworkType(), object);
        return marshal(nssmfRequest);
    }


    @Override
    protected String wrapActDeActReqBody(ActDeActNssi actDeActNssi) throws ApplicationException {

        return wrapReqBody(actDeActNssi);
    }


    @Override
    protected String wrapDeAllocateReqBody(DeAllocateNssi deAllocateNssi) throws ApplicationException {
        return wrapReqBody(deAllocateNssi);
    }


    @Override
    protected RestResponse doQueryJobStatus(ResourceOperationStatus status) throws ApplicationException {
        return responseDBStatus(status);
    }

    private RestResponse responseDBStatus(ResourceOperationStatus status) throws ApplicationException {
        ResponseDescriptor descriptor = new ResponseDescriptor();
        if (status == null) {
            descriptor.setProgress(0);
            descriptor.setStatus(PROCESSING.name());
            descriptor.setStatusDescription("Initiating Nssi Instance");
            return restUtil.createResponse(200, marshal(descriptor));
        }
        descriptor.setStatus(status.getStatus());
        descriptor.setStatusDescription(status.getStatusDescription());
        descriptor.setProgress(Integer.parseInt(status.getProgress()));
        // descriptor.setResponseId(status.getOperationId());
        return restUtil.createResponse(200, marshal(descriptor));
    }

    @Override
    protected RestResponse sendRequest(String content) {
        return sendInternalRequest(content);
    }

    @Override
    protected void afterRequest() {
        //
    }

    @Override
    protected void afterQueryJobStatus(ResourceOperationStatus status) {
        // internal
    }

    // internal
    private RestResponse sendInternalRequest(String content) {
        Header header = new BasicHeader("X-Auth-Token", adapterConfig.getInfraAuth());
        this.nssmfUrl = adapterConfig.getInfraEndpoint() + this.nssmfUrl;
        return restUtil.send(this.nssmfUrl, this.httpMethod, content, header);
    }

    @Override
    protected String getApiVersion() {
        return NssmfAdapterConsts.CURRENT_INTERNAL_NSSMF_API_VERSION;
    }


    @Override
    protected SelectionType doQueryNSSISelectionCapability() {
        return SelectionType.NSSMF;
    }

    @Override
    protected String wrapModifyReqBody(NssmfAdapterNBIRequest nbiRequest) throws ApplicationException {
        return doWrapModifyReqBody(nbiRequest);
    }

    protected abstract String doWrapModifyReqBody(NssmfAdapterNBIRequest nbiRequest) throws ApplicationException;

    @Override
    protected RestResponse doQuerySubnetCapability(String req) throws ApplicationException {
        // handler
        return sendRequest(req);
    }
}
