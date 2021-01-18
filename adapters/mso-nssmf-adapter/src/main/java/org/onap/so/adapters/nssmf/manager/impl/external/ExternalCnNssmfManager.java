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

import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.so.adapters.nssmf.enums.ActionType;
import org.onap.so.adapters.nssmf.enums.SelectionType;
import org.onap.so.adapters.nssmf.exceptions.ApplicationException;
import org.onap.so.adapters.nssmf.manager.impl.ExternalNssmfManager;
import org.onap.so.beans.nsmf.DeAllocateNssi;
import org.onap.so.beans.nsmf.NssmfAdapterNBIRequest;
import org.onap.so.db.request.beans.ResourceOperationStatus;
import static org.onap.so.adapters.nssmf.util.NssmfAdapterUtil.marshal;

public class ExternalCnNssmfManager extends ExternalNssmfManager {

    @Override
    protected String doWrapExtAllocateReqBody(NssmfAdapterNBIRequest nbiRequest) throws ApplicationException {
        return marshal(nbiRequest.getAllocateCnNssi());
    }

    @Override
    protected String doWrapModifyReqBody(NssmfAdapterNBIRequest nbiRequest) throws ApplicationException {
        return marshal(nbiRequest.getAllocateCnNssi());
    }

    @Override
    protected String doWrapDeAllocateReqBody(DeAllocateNssi deAllocateNssi) throws ApplicationException {
        return marshal(deAllocateNssi);
    }

    @Override
    protected void afterQueryJobStatus(ResourceOperationStatus status) {
        super.afterQueryJobStatus(status);
        ActionType jobOperType = ActionType.valueOf(status.getOperType());
        if (Integer.parseInt(status.getProgress()) == 100) {
            if (ActionType.ACTIVATE.equals(jobOperType)) {
                ServiceInstance nssiInstance = restUtil.getServiceInstance(serviceInfo);
                nssiInstance.setOrchestrationStatus("activated");
                restUtil.updateServiceInstance(nssiInstance, serviceInfo);
            } else if (ActionType.DEACTIVATE.equals(jobOperType)) {
                ServiceInstance nssiInstance = restUtil.getServiceInstance(serviceInfo);
                nssiInstance.setOrchestrationStatus("deactivated");
                restUtil.updateServiceInstance(nssiInstance, serviceInfo);
            }
        }
    }

    @Override
    protected SelectionType doQueryNSSISelectionCapability() {

        return SelectionType.NSMF;
    }

}
