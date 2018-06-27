/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.openecomp.mso.adapters.vnf.vmfm;


import java.util.Map;
import javax.xml.ws.Holder;
import org.openecomp.mso.entity.MsoRequest;
import org.openecomp.mso.openstack.beans.VnfStatus;

/**
 * Represents the VNFM adapter functionality with clear interfaces.
 *
 * @see MsoVnfmAdapterImpl implements that #MsoVnfmAdapter API
 * // ToscaResourceInstaller
 *
 */
public class VnfmVnfManager {

    /**
     * Creates and instantiates the VNF using the external VNFM
     *
     * - in case of rollback
     *   - if the VNF is instantiated the VNF is terminated
     *   - if the VNF exists it is deleted
     *
     * @param cloudOwner the owner of the cloud to which the VNF is to be instantiated into
     * @param regionId the region into which the VNF is to be instantiated into
     * @param tenantId the identifier of the tenant into which the VNF is to be instantiated into
     * @param vnfId the identifier of the VNF in A&AI
     * @param vnfName the name of the VNF (the name of the VNF is unique in ONAP)
     * @param inputs the inputs of the VNF
     * @param msoRequest the SO request used to track requests
     * @param csarId the identifier of the VNF package in SDC
     * @param heatStackId the identifier of the Heat stack if the VNF was created
     * @param failIfExists fail if the VNF already exists
     * @param backout terminate and delete the VNF upon failure
     * @param rollback the object used to roll back the operation
     */
    public void createVnf(String cloudOwner, String regionId, String tenantId, String vnfId, String vnfName, Map<String, String> inputs, MsoRequest msoRequest, String csarId, Holder<String> heatStackId, Boolean failIfExists, Boolean backout, Rollback rollback) {
        //FIXME add the implementation
    }

    /**
     * Updates the VNF in the VNFM.
     *  - Executes VNF modify attributes if required
     *  - Executes VIM info change (updated every time)
     *  - this operation can not be rolled back
     *
     * @param vnfId the identifier of the VNF in A&AI
     * @param vnfModelVersionUuid the desired version of the VNF. This version may differ from the actual version of the
     *                            VNF stored in A&AI in the modelVersionUuid attribute
     * @param inputs   the inputs of the update operation
     * @param msoRequest the SO request. Used for tracking the request
     * @param rollback the rollback object to roll back the current operation.
     */
    public void updateVnf(String vnfId, String vnfModelVersionUuid, Map<String, String> inputs, MsoRequest msoRequest, Rollback rollback) {
        //FIXME add the implementation
    }

    /**
     * Terminates and deletes the VNF in the VNFM.
     * @param vnfId the identifier of the VNF
     * @param msoRequest the SO request. Used for tracking the request
     */
    public void deleteVnf(String vnfId, MsoRequest msoRequest) {
        //FIXME add the implementation
    }

    /**
     * Query the current state of the VNF
     *
     * @param vnfExists is the VNF created in the VNFM
     * @param heatStackId the identifier of the Heat stack if the VNF is instantiated, if not it is null
     * @param status the status of the VNF
     *               - NOTFOUND if the VNF is not created
     *               - FAILED if the VNF is instantiated, but the last operation failed
     *               - ACTIVE if the VNF is instantiated and the last operation succeded\
     *               - UNKNOWN every other case
     */
    public void query(Holder<Boolean> vnfExists, Holder<String> heatStackId, Holder<VnfStatus> status) {
        //FIXME add the implementation
    }
}