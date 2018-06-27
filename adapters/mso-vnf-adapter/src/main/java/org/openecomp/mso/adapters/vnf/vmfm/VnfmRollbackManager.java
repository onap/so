/*
 * Copyright 2016-2017, Nokia Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.mso.adapters.vnf.vmfm;


import com.google.gson.Gson;
import javax.xml.ws.Holder;
import org.onap.vnfmadapter.so.model.OriginalVnfProperties;
import org.onap.vnfmadapter.so.model.SoV2RollbackVnfUpdate;
import org.onap.vnfmadapter.so.model.SoV2VnfDeleteRequest;
import org.openecomp.mso.entity.MsoRequest;
import org.openecomp.mso.openstack.beans.VnfRollback;

import static org.openecomp.mso.adapters.vnf.vmfm.MsoVnfmAdapterImpl.buildMsoRequest;

/**
 * Responsible for rolling back an operation on the VNFM side
 */
public class VnfmRollbackManager {
    private VnfmRestApiProvider vnfmRestApiProvider = new VnfmRestApiProvider();

    static void prepare(Holder<VnfRollback> rollback, String vnfIdInAai, String cloudSite, MsoRequest request) {
        rollback.value.setVnfCreated(true);
        rollback.value.setTenantCreated(true);
        rollback.value.setMsoRequest(request);
        rollback.value.setCloudSiteId(cloudSite);
        rollback.value.setVnfId(vnfIdInAai);
    }

    void rollback(VnfRollback rollback) {
        switch (RequestType.valueOf(rollback.getRequestType())) {
            case CREATE_VNF:
                SoV2VnfDeleteRequest request = new SoV2VnfDeleteRequest();
                request.setMsoRequest(buildMsoRequest(rollback.getMsoRequest()));
                vnfmRestApiProvider.getVnfmApi().vnfDelete(rollback.getVnfId(), request);
                break;
            case UPDATE_VNF:
                SoV2RollbackVnfUpdate rollbackVnfUpdateRequest = new SoV2RollbackVnfUpdate();
                rollbackVnfUpdateRequest.setOriginalVnfProperties(new Gson().fromJson(rollback.getBaseGroupHeatStackId(), OriginalVnfProperties.class));
                vnfmRestApiProvider.getVnfmApi().rollback(rollback.getVnfId(), rollbackVnfUpdateRequest);
                break;
            case CREATE_VF_MODULE:
                SoV2VnfDeleteRequest vfModuleDeleteRequest = new SoV2VnfDeleteRequest();
                vfModuleDeleteRequest.setMsoRequest(buildMsoRequest(rollback.getMsoRequest()));
                vnfmRestApiProvider.getVnfmApi().vfModuleDelete(rollback.getVnfId(), rollback.getBaseGroupHeatStackId(), vfModuleDeleteRequest);
                break;
            case UPDATE_VF_MODULE:
                SoV2RollbackVnfUpdate vfModuleUpdateRollback = new SoV2RollbackVnfUpdate();
                vfModuleUpdateRollback.setOriginalVnfProperties(new Gson().fromJson(rollback.getBaseGroupHeatStackId(), OriginalVnfProperties.class));
                vnfmRestApiProvider.getVnfmApi().vfModuleUpdateRollback(rollback.getVnfId(), rollback.getVfModuleStackId(), vfModuleUpdateRollback);
                break;
            default:
        }
    }

    enum RequestType {
        CREATE_VNF,
        UPDATE_VNF,
        CREATE_VF_MODULE,
        UPDATE_VF_MODULE
    }


}