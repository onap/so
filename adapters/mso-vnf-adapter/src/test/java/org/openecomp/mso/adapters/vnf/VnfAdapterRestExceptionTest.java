/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Intellectual Property. All rights reserved.
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
package org.openecomp.mso.adapters.vnf;

import org.junit.Test;
import org.openecomp.mso.adapters.vnfrest.CreateVfModuleRequest;
import org.openecomp.mso.adapters.vnfrest.DeleteVfModuleRequest;
import org.openecomp.mso.adapters.vnfrest.RollbackVfModuleRequest;
import org.openecomp.mso.adapters.vnfrest.UpdateVfModuleRequest;

public class VnfAdapterRestExceptionTest {

    VnfAdapterRest vnfAdapterRest = new VnfAdapterRest();

    @Test(expected = ClassFormatError.class)
    public void healthcheckClassFormatError() throws Exception {
        vnfAdapterRest.healthcheck();
    }

    @Test(expected = ClassFormatError.class)
    public void deleteVfModuleClassFormatError() throws Exception {
        DeleteVfModuleRequest deleteVfModuleRequest = new DeleteVfModuleRequest();
        deleteVfModuleRequest.setVfModuleId("moduleid");
        deleteVfModuleRequest.setVnfId("vnfid");
        vnfAdapterRest.deleteVfModule("vnfid", "moduleid", deleteVfModuleRequest);
    }

    @Test(expected = NullPointerException.class)
    public void queryVfModuleNullPointerException() throws Exception {
        vnfAdapterRest.queryVfModule("vnfid", "vfmoduleid", "cloudid", "tenantid", "modulename", true, "req-id", "sinstanceid");
    }

    @Test(expected = ClassFormatError.class)
    public void createVfModuleClassFormatError() throws Exception {
        CreateVfModuleRequest createVfModuleRequest = new CreateVfModuleRequest();
        createVfModuleRequest.setVnfId("vnfid");
        vnfAdapterRest.createVfModule("vnfid", createVfModuleRequest);
    }

    @Test(expected = ClassFormatError.class)
    public void updateVfModuleClassFormatError() throws Exception {
        UpdateVfModuleRequest updateVfModuleRequest = new UpdateVfModuleRequest();
        updateVfModuleRequest.setVnfId("vnfid");
        updateVfModuleRequest.setVfModuleId("moduleid");
        vnfAdapterRest.updateVfModule("vnfid", "moduleid", updateVfModuleRequest);
    }

    @Test(expected = NullPointerException.class)
    public void rollbackVfModuleNullPointerException() throws Exception {
        RollbackVfModuleRequest rollbackVfModuleRequest = new RollbackVfModuleRequest();
        vnfAdapterRest.rollbackVfModule("vnfid", "moduleid", rollbackVfModuleRequest);
    }

}