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
import java.util.Map;
import javax.xml.ws.Holder;
import org.onap.vnfmadapter.so.model.*;
import org.openecomp.mso.entity.MsoRequest;
import org.openecomp.mso.openstack.beans.VnfRollback;

import static org.openecomp.mso.adapters.vnf.vmfm.MsoVnfmAdapterImpl.buildMsoRequest;

/**
 * Responsible for executing VF modules level operations on the VNFM.
 * The VF module operations always result in scale operation.
 */
public class VnfmVfModuleManager {
    private VnfmRestApiProvider vnfmRestApiProvider = new VnfmRestApiProvider();

    void createVfModule(String vnfId, String vfModuleId, String aspectId, Map<String, String> inputs, MsoRequest msoRequest, Boolean failIfExists, Boolean backout) {
        SoV2VfModuleCreateRequest request = new SoV2VfModuleCreateRequest();
        request.setMsoRequest(buildMsoRequest(msoRequest));
        SoInput soInputs = new SoInput();
        soInputs.putAll(inputs);
        request.setInputs(soInputs);
        request.setScalingAspectId(aspectId);
        request.setFailIfExists(failIfExists);
        if (backout == null) {
            backout = true;
        }
        request.setDeleteUponFailure(!backout);
        vnfmRestApiProvider.getVnfmApi().vfModuleCreate(vnfId, vfModuleId, request).blockingFirst();
    }

    public void updateVfModule(String vnfId, String vfModuleId, Map<String, String> inputs, MsoRequest msoRequest, Holder<VnfRollback> rollback) {
        SoV2VnfUpdateRequest request = new SoV2VnfUpdateRequest();
        request.setInputs(new SoInput());
        request.getInputs().putAll(inputs);
        request.setMsoRequest(buildMsoRequest(msoRequest));
        SoV2VnfUpdateResponse response = vnfmRestApiProvider.getVnfmApi().vfModuleUpdate(vnfId, vfModuleId, request).blockingFirst();
        rollback.value.setRequestType(VnfmRollbackManager.RequestType.UPDATE_VF_MODULE.name());
        rollback.value.setVfModuleStackId(vfModuleId);
        rollback.value.setBaseGroupHeatStackId(new Gson().toJson(response.getOriginalVnfProperties()));
        rollback.value.setIsBase(false);
    }

    public void deleteVfModule(String vnfId, String vfModuleId, MsoRequest msoRequest) {
        SoV2VnfDeleteRequest request = new SoV2VnfDeleteRequest();
        request.setMsoRequest(buildMsoRequest(msoRequest));
        vnfmRestApiProvider.getVnfmApi().vfModuleDelete(vnfId, vfModuleId, request).blockingFirst();
    }
}