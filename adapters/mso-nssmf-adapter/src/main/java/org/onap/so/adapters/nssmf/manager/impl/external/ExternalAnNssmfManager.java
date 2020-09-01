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
import org.onap.so.beans.nsmf.DeAllocateNssi;
import org.onap.so.beans.nsmf.NssiResponse;
import org.onap.so.beans.nsmf.NssmfAdapterNBIRequest;
import org.onap.so.db.request.beans.ResourceOperationStatus;
import java.util.HashMap;
import java.util.Map;
import static org.onap.so.adapters.nssmf.util.NssmfAdapterUtil.marshal;
import static org.onap.so.adapters.nssmf.util.NssmfAdapterUtil.unMarshal;


public class ExternalAnNssmfManager extends ExternalNssmfManager {

    @Override
    protected String doWrapExtAllocateReqBody(NssmfAdapterNBIRequest nbiRequest) throws ApplicationException {
        Map<String, Object> request = new HashMap<>();
        request.put("attributeListIn", nbiRequest.getAllocateAnNssi().getSliceProfile());
        return marshal(request);
    }

    @Override
    protected void doAfterRequest() throws ApplicationException {
        if (ActionType.ALLOCATE.equals(actionType) || ActionType.DEALLOCATE.equals(actionType)) {
            @SuppressWarnings("unchecked")
            Map<String, String> response = unMarshal(restResponse.getResponseContent(), Map.class);

            String nssiId = response.get("nSSId");

            NssiResponse resp = new NssiResponse();
            resp.setJobId(nssiId);
            resp.setNssiId(nssiId);

            RestResponse returnRsp = new RestResponse();

            returnRsp.setStatus(202);
            returnRsp.setResponseContent(marshal(resp));
            restResponse = returnRsp;

            ResourceOperationStatus status =
                    new ResourceOperationStatus(serviceInfo.getNsiId(), nssiId, serviceInfo.getServiceUuid());
            status.setResourceInstanceID(nssiId);

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
    protected SelectionType doQueryNSSISelectionCapability() {
        return SelectionType.NSSMF;
    }
}
