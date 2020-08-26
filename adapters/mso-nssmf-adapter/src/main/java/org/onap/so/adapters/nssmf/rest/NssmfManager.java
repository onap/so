/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.adapters.nssmf.rest;

import org.onap.so.adapters.nssmf.entity.RestResponse;
import org.onap.so.adapters.nssmf.enums.JobStatus;
import org.onap.so.adapters.nssmf.exceptions.ApplicationException;
import org.onap.so.adapters.nssmf.util.RestUtil;
import org.onap.so.beans.nsmf.ActDeActNssi;
import org.onap.so.beans.nsmf.AllocateAnNssi;
import org.onap.so.beans.nsmf.AllocateCnNssi;
import org.onap.so.beans.nsmf.AllocateTnNssi;
import org.onap.so.beans.nsmf.CreateCnNssi;
import org.onap.so.beans.nsmf.DeAllocateNssi;
import org.onap.so.beans.nsmf.EsrInfo;
import org.onap.so.beans.nsmf.JobStatusRequest;
import org.onap.so.beans.nsmf.JobStatusResponse;
import org.onap.so.beans.nsmf.NetworkType;
import org.onap.so.beans.nsmf.NssiActDeActRequest;
import org.onap.so.beans.nsmf.NssiAllocateRequest;
import org.onap.so.beans.nsmf.NssiCreateRequest;
import org.onap.so.beans.nsmf.NssiDeAllocateRequest;
import org.onap.so.beans.nsmf.NssiResponse;
import org.onap.so.beans.nsmf.NssiTerminateRequest;
import org.onap.so.beans.nsmf.NssiUpdateRequest;
import org.onap.so.beans.nsmf.NssiUpdateRequestById;
import org.onap.so.beans.nsmf.ResponseDescriptor;
import org.onap.so.beans.nsmf.TerminateNssi;
import org.onap.so.beans.nsmf.UpdateCnNssi;
import org.onap.so.beans.nsmf.UpdateCnNssiById;
import org.onap.so.db.request.beans.ResourceOperationStatus;
import org.onap.so.db.request.data.repository.ResourceOperationStatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;
import static java.lang.String.valueOf;
import static org.onap.so.adapters.nssmf.enums.HttpMethod.DELETE;
import static org.onap.so.adapters.nssmf.enums.HttpMethod.GET;
import static org.onap.so.adapters.nssmf.enums.HttpMethod.POST;
import static org.onap.so.adapters.nssmf.enums.HttpMethod.PUT;
import static org.onap.so.adapters.nssmf.enums.JobStatus.ERROR;
import static org.onap.so.adapters.nssmf.enums.JobStatus.FINISHED;
import static org.onap.so.adapters.nssmf.enums.JobStatus.PROCESSING;
import static org.onap.so.adapters.nssmf.enums.JobStatus.STARTED;
import static org.onap.so.adapters.nssmf.enums.JobStatus.fromString;
import static org.onap.so.adapters.nssmf.util.NssmfAdapterUtil.StatusDesc.ACTIVATE_NSS_SUCCESS;
import static org.onap.so.adapters.nssmf.util.NssmfAdapterUtil.StatusDesc.ALLOCATE_NSS_SUCCESS;
import static org.onap.so.adapters.nssmf.util.NssmfAdapterUtil.StatusDesc.CREATE_NSS_SUCCESS;
import static org.onap.so.adapters.nssmf.util.NssmfAdapterUtil.StatusDesc.DEACTIVATE_NSS_SUCCESS;
import static org.onap.so.adapters.nssmf.util.NssmfAdapterUtil.StatusDesc.DEALLOCATE_NSS_SUCCESS;
import static org.onap.so.adapters.nssmf.util.NssmfAdapterUtil.StatusDesc.QUERY_JOB_STATUS_FAILED;
import static org.onap.so.adapters.nssmf.util.NssmfAdapterUtil.StatusDesc.QUERY_JOB_STATUS_SUCCESS;
import static org.onap.so.adapters.nssmf.util.NssmfAdapterUtil.assertObjectNotNull;
import static org.onap.so.adapters.nssmf.util.NssmfAdapterUtil.marshal;
import static org.onap.so.adapters.nssmf.util.NssmfAdapterUtil.unMarshal;
import static org.onap.so.beans.nsmf.ActDeActNssi.ACT_URL;
import static org.onap.so.beans.nsmf.ActDeActNssi.DE_ACT_URL;

@Component
@Primary
@Deprecated
public class NssmfManager {

    private static final Logger logger = LoggerFactory.getLogger(NssmfManager.class);

    public final static String QUERY = "/api/rest/provMns/v1/NSS" + "/SliceProfiles/{sliceProfileId}";

    public final static String QUERY_BY_ID = "/api/rest/provMns/v1/NSS/nssi" + "/{nssiId}";

    @Autowired
    private ResourceOperationStatusRepository rscOperStatusRepo;

    @Autowired
    private RestUtil restUtil;


    public RestResponse allocateNssi(NssiAllocateRequest nssmiAllocate) throws ApplicationException {

        assertObjectNotNull(nssmiAllocate.getEsrInfo());
        assertObjectNotNull(nssmiAllocate.getEsrInfo().getNetworkType());
        assertObjectNotNull(nssmiAllocate.getEsrInfo().getVendor());

        String nsiId = null;
        String allocateReq = null;
        String allocateUrl = null;
        logger.info("Allocate Nssi for " + nssmiAllocate.getEsrInfo().getNetworkType() + " Network has begun");

        switch (nssmiAllocate.getEsrInfo().getNetworkType()) {

            case CORE:
                AllocateCnNssi cn = nssmiAllocate.getAllocateCnNssi();
                assertObjectNotNull(cn);
                assertObjectNotNull(cn.getNsiInfo());
                assertObjectNotNull(cn.getNsiInfo().getNsiId());
                nsiId = cn.getNsiInfo().getNsiId();
                assertObjectNotNull(nsiId);
                allocateReq = marshal(cn);
                allocateUrl = AllocateCnNssi.URL;
                break;

            case ACCESS:
                AllocateAnNssi an = nssmiAllocate.getAllocateAnNssi();
                assertObjectNotNull(an);
                assertObjectNotNull(an.getNsiInfo());
                assertObjectNotNull(an.getNsiInfo().getNsiId());
                nsiId = an.getNsiInfo().getNsiId();
                assertObjectNotNull(nsiId);
                allocateReq = marshal(an);
                allocateUrl = AllocateAnNssi.URL;
                break;

            case TRANSPORT:
                AllocateTnNssi tn = nssmiAllocate.getAllocateTnNssi();
                assertObjectNotNull(tn);
                // assertObjectNotNull(tn.getNsiInfo());
                // assertObjectNotNull(tn.getNsiInfo().getNsiId());
                // nsiId = tn.getNsiInfo().getNsiId();
                allocateReq = marshal(tn);
                // allocateUrl = AllocateTnNssi.URL;
                break;

        }

        /**
         * 内部的，调用 workflow 外部的，访问第三方api
         */
        RestResponse rsp = restUtil.sendRequest(allocateUrl, POST, allocateReq, nssmiAllocate.getEsrInfo());
        assertObjectNotNull(rsp);

        if (valueOf(rsp.getStatus()).startsWith("2")) {
            NssiResponse allocateRes = unMarshal(rsp.getResponseContent(), NssiResponse.class);

            ResourceOperationStatus status =
                    new ResourceOperationStatus(allocateRes.getNssiId(), allocateRes.getJobId(), nsiId);
            logger.info("save segment and operaton info -> begin");
            updateDbStatus(status, rsp.getStatus(), STARTED, ALLOCATE_NSS_SUCCESS);
            logger.info("save segment and operation info -> end");
        }
        return rsp;
    }



    public RestResponse createNssi(NssiCreateRequest nssiCreate) throws ApplicationException {

        assertObjectNotNull(nssiCreate.getEsrInfo());
        assertObjectNotNull(nssiCreate.getEsrInfo().getNetworkType());
        assertObjectNotNull(nssiCreate.getEsrInfo().getVendor());

        String nsiId = null;
        String createReq = null;
        String createUrl = null;
        logger.info("Create Nssi for " + nssiCreate.getEsrInfo().getNetworkType() + " Network has begun");

        switch (nssiCreate.getEsrInfo().getNetworkType()) {
            case CORE:
                CreateCnNssi cn = nssiCreate.getCreateCnNssi();
                nsiId = cn.getNsiInfo().getNsiId();
                assertObjectNotNull(nsiId);
                createReq = marshal(cn);
                createUrl = AllocateCnNssi.URL;
                break;

            case ACCESS:
            case TRANSPORT:
                throw new ApplicationException(1, "Create Nssi doesn't " + "support the Network type:"
                        + nssiCreate.getEsrInfo().getNetworkType());
        }
        RestResponse rsp = restUtil.sendRequest(createUrl, POST, createReq, nssiCreate.getEsrInfo());
        assertObjectNotNull(rsp);

        if (valueOf(rsp.getStatus()).startsWith("2")) {
            NssiResponse allocateRes = unMarshal(rsp.getResponseContent(), NssiResponse.class);

            ResourceOperationStatus status =
                    new ResourceOperationStatus(allocateRes.getNssiId(), allocateRes.getJobId(), nsiId);
            logger.info("save segment and operaton info -> begin");
            updateDbStatus(status, rsp.getStatus(), STARTED, CREATE_NSS_SUCCESS);
            logger.info("save segment and operaton info -> end");
        }
        return rsp;
    }

    @Deprecated
    public RestResponse deAllocateNssi(NssiDeAllocateRequest nssiDeallocate, String sliceId)
            throws ApplicationException {

        assertObjectNotNull(nssiDeallocate.getEsrInfo());
        assertObjectNotNull(nssiDeallocate.getEsrInfo().getNetworkType());
        assertObjectNotNull(nssiDeallocate.getEsrInfo().getVendor());

        DeAllocateNssi deAllocate = nssiDeallocate.getDeAllocateNssi();

        assertObjectNotNull(sliceId);
        assertObjectNotNull(deAllocate.getNssiId());
        assertObjectNotNull(deAllocate.getNsiId());

        String deallocateUrl = formTnAndAnUrl(nssiDeallocate.getEsrInfo(), DeAllocateNssi.URL, sliceId);
        String deAllocateReq = marshal(deAllocate);

        logger.info("Deallocate Nssi has begun");

        RestResponse rsp = restUtil.sendRequest(deallocateUrl, DELETE, deAllocateReq, nssiDeallocate.getEsrInfo());
        assertObjectNotNull(rsp);

        if (valueOf(rsp.getStatus()).startsWith("2")) {
            NssiResponse res = unMarshal(rsp.getResponseContent(), NssiResponse.class);

            ResourceOperationStatus status =
                    new ResourceOperationStatus(deAllocate.getNssiId(), res.getJobId(), deAllocate.getNsiId());
            logger.info("save segment and operaton info -> begin");
            updateDbStatus(status, rsp.getStatus(), STARTED, DEALLOCATE_NSS_SUCCESS);
            logger.info("save segment and operaton info -> end");
        }
        return rsp;
    }

    private String formTnAndAnUrl(EsrInfo esrInfo, String origUrl, String variable) {

        origUrl = formatUrl(origUrl, variable);
        String[] val;

        switch (esrInfo.getNetworkType()) {

            case TRANSPORT:
                val = origUrl.split("v1");
                return val[0] + "v1/tn" + val[1];

            case ACCESS:
                val = origUrl.split("v1");
                return val[0] + "v1/an" + val[1];

            case CORE:
                return origUrl;
        }
        return origUrl;
    }

    private String formatUrl(String origUrl, String variable) {

        if (variable != null) {
            origUrl = String.format(origUrl, variable);
        }
        return origUrl;
    }


    public RestResponse terminateNssi(NssiTerminateRequest nssiTerminate, String nssiId) throws ApplicationException {

        assertObjectNotNull(nssiTerminate.getEsrInfo());
        assertObjectNotNull(nssiTerminate.getEsrInfo().getNetworkType());
        assertObjectNotNull(nssiTerminate.getEsrInfo().getVendor());

        TerminateNssi terminate = nssiTerminate.getTerminateNssi();

        assertObjectNotNull(nssiId);
        assertObjectNotNull(terminate.getNsiId());

        logger.info("Terminate Nssi has begun");

        String terminateUrl = formTnAndAnUrl(nssiTerminate.getEsrInfo(), TerminateNssi.URL, nssiId);
        String terminateReq = marshal(terminate);

        RestResponse rsp = restUtil.sendRequest(terminateUrl, DELETE, terminateReq, nssiTerminate.getEsrInfo());
        assertObjectNotNull(rsp);

        if (valueOf(rsp.getStatus()).startsWith("2")) {
            NssiResponse res = unMarshal(rsp.getResponseContent(), NssiResponse.class);

            ResourceOperationStatus status = new ResourceOperationStatus(nssiId, res.getJobId(), terminate.getNsiId());
            logger.info("save segment and operaton info -> begin");
            updateDbStatus(status, rsp.getStatus(), STARTED, DEALLOCATE_NSS_SUCCESS);
            logger.info("save segment and operaton info -> end");
        }
        return rsp;
    }

    public RestResponse activateNssi(NssiActDeActRequest nssiActivate, String snssai) throws ApplicationException {

        assertObjectNotNull(nssiActivate.getEsrInfo());
        assertObjectNotNull(nssiActivate.getEsrInfo().getNetworkType());
        assertObjectNotNull(nssiActivate.getEsrInfo().getVendor());

        ActDeActNssi activate = nssiActivate.getActDeActNssi();

        assertObjectNotNull(snssai);
        assertObjectNotNull(activate.getNssiId());
        assertObjectNotNull(activate.getNsiId());

        logger.info("Activate Nssi has begun");

        String activateUrl = formTnAndAnUrl(nssiActivate.getEsrInfo(), ACT_URL, snssai);
        String activateReq = marshal(activate);

        RestResponse rsp = restUtil.sendRequest(activateUrl, PUT, activateReq, nssiActivate.getEsrInfo());
        assertObjectNotNull(rsp);

        if (valueOf(rsp.getStatus()).startsWith("2")) {
            NssiResponse activateRes = unMarshal(rsp.getResponseContent(), NssiResponse.class);

            ResourceOperationStatus status =
                    new ResourceOperationStatus(activate.getNssiId(), activateRes.getJobId(), activate.getNsiId());
            logger.info("save segment and operaton info -> begin");
            updateDbStatus(status, rsp.getStatus(), STARTED, ACTIVATE_NSS_SUCCESS);
            logger.info("save segment and operaton info -> end");
        }
        return rsp;
    }

    public RestResponse deActivateNssi(NssiActDeActRequest nssiDeActivate, String snssai) throws ApplicationException {

        assertObjectNotNull(nssiDeActivate.getEsrInfo());
        assertObjectNotNull(nssiDeActivate.getEsrInfo().getNetworkType());
        assertObjectNotNull(nssiDeActivate.getEsrInfo().getVendor());

        logger.info("Deactivate Nssi has begun");

        ActDeActNssi deActivate = nssiDeActivate.getActDeActNssi();

        assertObjectNotNull(snssai);
        assertObjectNotNull(deActivate.getNssiId());
        assertObjectNotNull(deActivate.getNsiId());

        String deActivateUrl = formTnAndAnUrl(nssiDeActivate.getEsrInfo(), DE_ACT_URL, snssai);
        String deActivateReq = marshal(deActivate);

        RestResponse rsp = restUtil.sendRequest(deActivateUrl, PUT, deActivateReq, nssiDeActivate.getEsrInfo());
        assertObjectNotNull(rsp);

        if (valueOf(rsp.getStatus()).startsWith("2")) {
            NssiResponse deActivateRes = unMarshal(rsp.getResponseContent(), NssiResponse.class);

            ResourceOperationStatus status = new ResourceOperationStatus(deActivate.getNssiId(),
                    deActivateRes.getJobId(), deActivate.getNsiId());
            logger.info("save segment and operaton info -> begin");
            updateDbStatus(status, rsp.getStatus(), STARTED, DEACTIVATE_NSS_SUCCESS);
            logger.info("save segment and operaton info -> end");
        }
        return rsp;
    }

    public RestResponse queryJobStatus(JobStatusRequest jobReq, String jobId) throws ApplicationException {

        assertObjectNotNull(jobReq.getEsrInfo());
        assertObjectNotNull(jobReq.getEsrInfo().getNetworkType());
        assertObjectNotNull(jobReq.getEsrInfo().getVendor());
        assertObjectNotNull(jobId);
        assertObjectNotNull(jobReq.getNssiId());
        assertObjectNotNull(jobReq.getNsiId());

        logger.info("Query job status has begun");

        ResourceOperationStatus status = new ResourceOperationStatus(jobReq.getNssiId(), jobId, jobReq.getNsiId());
        status = rscOperStatusRepo.findOne(Example.of(status))
                .orElseThrow(() -> new ApplicationException(404, "Cannot Find Operation Status"));

        String statusUrl = formatUrl(JobStatusRequest.URL, jobId);
        if (jobReq.getResponseId() != null) {
            statusUrl = statusUrl + "?responseId=" + jobReq.getResponseId();
        }

        RestResponse rsp = restUtil.sendRequest(statusUrl, GET, "", jobReq.getEsrInfo());
        assertObjectNotNull(rsp);

        if (!valueOf(rsp.getStatus()).startsWith("2")) {
            updateDbStatus(status, rsp.getStatus(), ERROR, QUERY_JOB_STATUS_FAILED);
            throw new ApplicationException(500, QUERY_JOB_STATUS_FAILED);
        }

        ResponseDescriptor rspDesc =
                unMarshal(rsp.getResponseContent(), JobStatusResponse.class).getResponseDescriptor();
        logger.info("save segment and operaton info -> begin");
        updateRequestDbJobStatus(rspDesc, status, rsp);
        logger.info("save segment and operaton info -> end");
        return rsp;
    }


    public RestResponse updateNssi(NssiUpdateRequest nssiUpdate, String sliceId) throws ApplicationException {

        assertObjectNotNull(nssiUpdate.getEsrInfo());
        assertObjectNotNull(nssiUpdate.getEsrInfo().getNetworkType());
        assertObjectNotNull(nssiUpdate.getEsrInfo().getVendor());
        assertObjectNotNull(sliceId);

        String nsiId = null;
        String nssiId = null;
        String updateReq = null;
        String updateUrl = null;
        logger.info("Update Nssi for " + nssiUpdate.getEsrInfo().getNetworkType() + " Network has begun");

        switch (nssiUpdate.getEsrInfo().getNetworkType()) {
            case CORE:
                UpdateCnNssi cn = nssiUpdate.getUpdateCnNssi();
                nsiId = cn.getNsiInfo().getNsiId();
                nssiId = cn.getNssiId();
                assertObjectNotNull(nsiId);
                assertObjectNotNull(nssiId);
                updateReq = marshal(cn);
                updateUrl = formatUrl(UpdateCnNssi.URL, sliceId);
                break;

            case ACCESS:
            case TRANSPORT:
                throw new ApplicationException(1, "Update Nssi doesn't " + "support the Network type:"
                        + nssiUpdate.getEsrInfo().getNetworkType());
        }

        RestResponse rsp = restUtil.sendRequest(updateUrl, PUT, updateReq, nssiUpdate.getEsrInfo());
        assertObjectNotNull(rsp);

        if (valueOf(rsp.getStatus()).startsWith("2")) {
            NssiResponse allocateRes = unMarshal(rsp.getResponseContent(), NssiResponse.class);

            ResourceOperationStatus status = new ResourceOperationStatus(nssiId, allocateRes.getJobId(), nsiId);
            logger.info("save segment and operaton info -> begin");
            updateDbStatus(status, rsp.getStatus(), STARTED, ALLOCATE_NSS_SUCCESS);
            logger.info("save segment and operaton info -> end");
        }
        return rsp;
    }

    public RestResponse updateNssiById(NssiUpdateRequestById nssiUpdateById, String nssiId)
            throws ApplicationException {

        assertObjectNotNull(nssiUpdateById.getEsrInfo());
        assertObjectNotNull(nssiUpdateById.getEsrInfo().getNetworkType());
        assertObjectNotNull(nssiUpdateById.getEsrInfo().getVendor());
        assertObjectNotNull(nssiId);

        String nsiId = null;
        String updateReq = null;
        String updateUrl = null;
        logger.info("Update Nssi by ID for " + nssiUpdateById.getEsrInfo().getNetworkType() + " Network has begun");

        switch (nssiUpdateById.getEsrInfo().getNetworkType()) {
            case CORE:
                UpdateCnNssiById cn = nssiUpdateById.getUpdateCnNssiById();
                nsiId = cn.getNsiInfo().getNsiId();
                assertObjectNotNull(nsiId);
                updateReq = marshal(cn);
                updateUrl = formatUrl(UpdateCnNssiById.URL, nssiId);
                break;

            case ACCESS:
            case TRANSPORT:
                throw new ApplicationException(1, "Update Nssi doesn't " + "support the Network type:"
                        + nssiUpdateById.getEsrInfo().getNetworkType());
        }

        RestResponse rsp = restUtil.sendRequest(updateUrl, PUT, updateReq, nssiUpdateById.getEsrInfo());
        assertObjectNotNull(rsp);

        if (valueOf(rsp.getStatus()).startsWith("2")) {
            NssiResponse allocateRes = unMarshal(rsp.getResponseContent(), NssiResponse.class);

            ResourceOperationStatus status = new ResourceOperationStatus(nssiId, allocateRes.getJobId(), nsiId);
            logger.info("save segment and operaton info -> begin");
            updateDbStatus(status, rsp.getStatus(), STARTED, ALLOCATE_NSS_SUCCESS);
            logger.info("save segment and operaton info -> end");
        }
        return rsp;
    }

    public RestResponse queryNssi(String vendor, String type, String sliceId) throws ApplicationException {

        logger.info("Query Nssi has begun");
        String getUrl = formatUrl(QUERY, sliceId);
        EsrInfo esr = new EsrInfo();
        esr.setVendor(vendor);
        esr.setNetworkType(NetworkType.valueOf(type));
        RestResponse rsp = restUtil.sendRequest(getUrl, GET, "", esr);
        assertObjectNotNull(rsp);
        return rsp;
    }

    public RestResponse queryNssiById(String vendor, String type, String nssiId) throws ApplicationException {

        logger.info("Query Nssi by ID has begun");
        String getUrl = formatUrl(QUERY_BY_ID, nssiId);
        EsrInfo esr = new EsrInfo();
        esr.setVendor(vendor);
        esr.setNetworkType(NetworkType.valueOf(type));
        RestResponse rsp = restUtil.sendRequest(getUrl, GET, "", esr);
        assertObjectNotNull(rsp);
        return rsp;
    }

    private void updateRequestDbJobStatus(ResponseDescriptor rspDesc, ResourceOperationStatus status, RestResponse rsp)
            throws ApplicationException {

        switch (fromString(rspDesc.getStatus())) {

            case STARTED:
                updateDbStatus(status, rsp.getStatus(), STARTED, QUERY_JOB_STATUS_SUCCESS);
                break;

            case ERROR:
                updateDbStatus(status, rsp.getStatus(), ERROR, QUERY_JOB_STATUS_FAILED);
                throw new ApplicationException(500, QUERY_JOB_STATUS_FAILED);

            case FINISHED:
                if (rspDesc.getProgress() == 100) {
                    updateDbStatus(status, rsp.getStatus(), FINISHED, QUERY_JOB_STATUS_SUCCESS);
                }
                break;

            case PROCESSING:
                updateDbStatus(status, rsp.getStatus(), PROCESSING, QUERY_JOB_STATUS_SUCCESS);
                break;
        }
    }

    private void updateDbStatus(ResourceOperationStatus status, int rspStatus, JobStatus jobStatus,
            String description) {
        status.setErrorCode(valueOf(rspStatus));
        status.setStatus(jobStatus.toString());
        status.setStatusDescription(description);
        logger.info("Updating DB status");
        rscOperStatusRepo.save(status);
        logger.info("Updating successful");
    }

    public void setRscOperStatusRepo(ResourceOperationStatusRepository rscOperStatusRepo) {
        this.rscOperStatusRepo = rscOperStatusRepo;
    }

    public void setRestUtil(RestUtil restUtil) {
        this.restUtil = restUtil;
    }
}
