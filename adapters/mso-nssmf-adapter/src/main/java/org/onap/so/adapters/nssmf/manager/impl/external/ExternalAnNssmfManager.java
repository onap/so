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

package org.onap.so.adapters.nssmf.manager.impl.external;

import org.onap.so.adapters.nssmf.entity.RestResponse;
import org.onap.so.adapters.nssmf.enums.ActionType;
import org.onap.so.adapters.nssmf.enums.JobStatus;
import org.onap.so.adapters.nssmf.enums.SelectionType;
import org.onap.so.adapters.nssmf.exceptions.ApplicationException;
import org.onap.so.adapters.nssmf.manager.impl.ExternalNssmfManager;
import org.onap.so.adapters.nssmf.util.NssmfAdapterUtil;
import org.onap.so.beans.nsmf.AnSliceProfile;
import org.onap.so.beans.nsmf.DeAllocateNssi;
import org.onap.so.beans.nsmf.NssiResponse;
import org.onap.so.beans.nsmf.NssmfAdapterNBIRequest;
import org.onap.so.beans.nsmf.ResponseDescriptor;
import org.onap.so.beans.nsmf.JobStatusResponse;
import org.onap.so.db.request.beans.ResourceOperationStatus;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import static org.onap.so.adapters.nssmf.util.NssmfAdapterUtil.marshal;
import static org.onap.so.adapters.nssmf.util.NssmfAdapterUtil.unMarshal;


public class ExternalAnNssmfManager extends ExternalNssmfManager {

    private Map<String, String> bodyParams = new HashMap<>(); // request body params

    @Override
    protected String doWrapExtAllocateReqBody(NssmfAdapterNBIRequest nbiRequest) throws ApplicationException {
        Map<String, Object> request = new HashMap<>();

        AnSliceProfile anSliceProfile = nbiRequest.getAllocateAnNssi().getSliceProfile();

        RanSliceProfile ranSliceProfile = new RanSliceProfile();
        ranSliceProfile.setSliceProfileId(anSliceProfile.getSliceProfileId());
        ranSliceProfile.setSNSSAIList(anSliceProfile.getSNSSAIList());
        ranSliceProfile.setPLMNIdList(anSliceProfile.getPLMNIdList());
        ranSliceProfile.setPerfReq(anSliceProfile.getPerfReq());
        ranSliceProfile.setMaxNumberOfUEs(anSliceProfile.getMaxNumberOfUEs());
        ranSliceProfile.setCoverageAreaTAList(anSliceProfile.getCoverageAreaTAList());
        ranSliceProfile.setLatency(anSliceProfile.getLatency());
        ranSliceProfile.setUeMobilityLevel(anSliceProfile.getUeMobilityLevel());
        ranSliceProfile.setResourceSharingLevel(anSliceProfile.getResourceSharingLevel());

        request.put("attributeListIn", ranSliceProfile);
        return marshal(request);
    }

    @Override
    protected void doAfterRequest() throws ApplicationException {
        if (ActionType.ALLOCATE.equals(actionType) || ActionType.DEALLOCATE.equals(actionType)) {
            String nssiId;
            if (ActionType.ALLOCATE.equals(actionType)) {
                @SuppressWarnings("unchecked")
                Map<String, String> response = unMarshal(restResponse.getResponseContent(), Map.class);
                nssiId = response.get("href");
            } else {
                nssiId = this.bodyParams.get("nssiId");
            }

            NssiResponse resp = new NssiResponse();
            resp.setJobId(UUID.randomUUID().toString());
            resp.setNssiId(nssiId);

            RestResponse returnRsp = new RestResponse();

            returnRsp.setStatus(202);
            returnRsp.setResponseContent(marshal(resp));
            restResponse = returnRsp;

            ResourceOperationStatus status =
                    new ResourceOperationStatus(serviceInfo.getNsiId(), resp.getJobId(), serviceInfo.getServiceUuid());
            status.setResourceInstanceID(nssiId);
            status.setOperType(actionType.toString());

            updateDbStatus(status, restResponse.getStatus(), JobStatus.FINISHED,
                    NssmfAdapterUtil.getStatusDesc(actionType));
        }
        // todo
    }

    @Override
    protected String doWrapModifyReqBody(NssmfAdapterNBIRequest nbiRequest) throws ApplicationException {
        // TODO
        return null;
    }

    @Override
    protected String doWrapDeAllocateReqBody(DeAllocateNssi deAllocateNssi) throws ApplicationException {
        this.bodyParams.clear();
        this.bodyParams.put("nssiId", deAllocateNssi.getNssiId());

        Map<String, String> request = new HashMap<>();
        request.put("nSSId", deAllocateNssi.getNssiId());
        return marshal(request);
    }


    @Override
    public RestResponse modifyNssi(NssmfAdapterNBIRequest modifyRequest) throws ApplicationException {
        // TODO
        return null;
    }

    @Override
    public RestResponse activateNssi(NssmfAdapterNBIRequest nbiRequest, String snssai) throws ApplicationException {
        // TODO
        return null;
    }

    @Override
    protected RestResponse doQueryJobStatus(ResourceOperationStatus status) throws ApplicationException {
        ResponseDescriptor responseDescriptor = new ResponseDescriptor();
        responseDescriptor.setStatus(JobStatus.FINISHED.toString());
        responseDescriptor.setProgress(100);
        responseDescriptor.setStatusDescription("Finished");

        JobStatusResponse jobStatusResponse = new JobStatusResponse();
        jobStatusResponse.setResponseDescriptor(responseDescriptor);

        RestResponse restResponse = new RestResponse();
        restResponse.setStatus(200);
        restResponse.setResponseContent(marshal(jobStatusResponse));

        updateRequestDbJobStatus(responseDescriptor, status, restResponse);

        status.setProgress(Integer.toString(responseDescriptor.getProgress()));

        return restResponse;
    }

    @Override
    protected SelectionType doQueryNSSISelectionCapability() {
        return SelectionType.NSSMF;
    }
}
