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
import org.openecomp.mso.openstack.beans.VnfStatus;

/**
 * Responsible for executing VNF level operations on the VNFM
 */
public class VnfmVnfManager {
    private VnfmRestApiProvider vnfmRestApiProvider = new VnfmRestApiProvider();

    /**
     * Creates and instantiates the VNF using the external VNFM
     * <p>
     * - in case of rollback
     * - if the VNF is instantiated the VNF is terminated
     * - if the VNF exists it is deleted
     *
     * @param cloudOwner   the owner of the cloud to which the VNF is to be instantiated into
     * @param regionId     the region into which the VNF is to be instantiated into
     * @param tenantId     the identifier of the tenant into which the VNF is to be instantiated into
     * @param vnfId        the identifier of the VNF in A&AI
     * @param vnfName      the name of the VNF (the name of the VNF is unique in ONAP)
     * @param inputs       the inputs of the VNF
     * @param msoRequest   the SO request used to track requests
     * @param csarId       the identifier of the VNF package in SDC
     * @param failIfExists fail if the VNF already exists
     * @param backout      terminate and delete the VNF upon failure
     */
    public void createVnf(String cloudOwner, String regionId, String tenantId, String vnfId, String vnfName, Map<String, String> inputs, MsoRequest msoRequest, String csarId, Boolean failIfExists, Boolean backout) {
        SoV2VnfCreateRequest request = new SoV2VnfCreateRequest();
        request.setDeleteUponFailure(!backout);
        request.setFailIfExists(failIfExists);
        SoInput soInputs = new SoInput();
        soInputs.putAll(inputs);
        request.setInputs(soInputs);
        request.setMsoRequest(MsoVnfmAdapterImpl.buildMsoRequest(msoRequest));
        request.setCloudOwner(cloudOwner);
        request.setName(vnfName);
        request.setTenantId(tenantId);
        vnfmRestApiProvider.getVnfmApi().vnfCreate(vnfId, request).blockingFirst();
    }

    /**
     * Updates the VNF in the VNFM.
     * - Executes VNF modify attributes if required
     * - Executes VIM info change (updated every time)
     * - this operation can not be rolled back
     *
     * @param vnfId               the identifier of the VNF in A&AI
     * @param vnfModelVersionUuid the desired version of the VNF. This version may differ from the actual version of the
     *                            VNF stored in A&AI in the modelVersionUuid attribute
     * @param inputs              the inputs of the update operation
     * @param msoRequest          the SO request. Used for tracking the request
     */
    public void updateVnf(String vnfId, String vnfModelVersionUuid, Map<String, String> inputs, MsoRequest msoRequest, Holder<VnfRollback> rollback) {
        rollback.value.setRequestType(VnfmRollbackManager.RequestType.UPDATE_VNF.name());
        rollback.value.setIsBase(true);
        SoV2VnfUpdateRequest request = new SoV2VnfUpdateRequest();
        request.setInputs(new SoInput());
        request.getInputs().putAll(inputs);
        request.setMsoRequest(MsoVnfmAdapterImpl.buildMsoRequest(msoRequest));
        SoV2VnfUpdateResponse response = vnfmRestApiProvider.getVnfmApi().vnfUpdate(vnfId, request).blockingFirst();
        rollback.value.setBaseGroupHeatStackId(new Gson().toJson(response.getOriginalVnfProperties()));
    }

    /**
     * Terminates and deletes the VNF in the VNFM.
     *
     * @param vnfId      the identifier of the VNF
     * @param msoRequest the SO request. Used for tracking the request
     */
    public void deleteVnf(String vnfId, MsoRequest msoRequest) {
        SoV2VnfDeleteRequest request = new SoV2VnfDeleteRequest();
        request.setMsoRequest(MsoVnfmAdapterImpl.buildMsoRequest(msoRequest));
        vnfmRestApiProvider.getVnfmApi().vnfDelete(vnfId, request);
    }

    /**
     * Query the current state of the VNF
     *
     * @param vnfExists   is the VNF created in the VNFM
     * @param heatStackId the identifier of the Heat stack if the VNF is instantiated, if not it is null
     * @param status      the status of the VNF
     *                    - NOTFOUND if the VNF is not created
     *                    - FAILED if the VNF is instantiated, but the last operation failed
     *                    - ACTIVE if the VNF is instantiated and the last operation succesful
     *                    - UNKNOWN every other case
     */
    public void query(String vnfId, MsoRequest msoRequest, Holder<Boolean> vnfExists, Holder<String> heatStackId, Holder<VnfStatus> status) {
        SoV2VnfQueryRequest request = new SoV2VnfQueryRequest();
        request.setMsoRequest(MsoVnfmAdapterImpl.buildMsoRequest(msoRequest));
        SoV2VnfQueryResponse response = vnfmRestApiProvider.getVnfmApi().vnfQuery(vnfId, request).blockingFirst();
        heatStackId.value = "base";
        vnfExists.value = SoVnfStatus.NOTFOUND.equals(response.getStatus());
        status.value = VnfStatus.valueOf(response.getStatus().name());
    }

}