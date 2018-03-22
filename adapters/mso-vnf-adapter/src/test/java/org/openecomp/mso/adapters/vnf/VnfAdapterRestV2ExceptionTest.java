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

public class VnfAdapterRestV2ExceptionTest {

    VnfAdapterRestV2 vnfAdapterRestV2 = new VnfAdapterRestV2();

    @Test(expected = ClassFormatError.class)
    public void healthcheck() throws Exception {
        vnfAdapterRestV2.healthcheck();
    }

    @Test(expected = ClassFormatError.class)
    public void deleteVfModuleClassFormatError() throws Exception {
        DeleteVfModuleRequest deleteVfModuleRequest = new DeleteVfModuleRequest();
        deleteVfModuleRequest.setVnfId("vnfid");
        deleteVfModuleRequest.setVfModuleId("moduleid");
        vnfAdapterRestV2.deleteVfModule("vnfid", "moduleid", "mode", deleteVfModuleRequest);
    }

    @Test(expected = NullPointerException.class)
    public void queryVfModuleNullPointerException() throws Exception {
        vnfAdapterRestV2.queryVfModule("vnfid", "moduleid", "cloudid", "teanantid", "vfmodulename", true, "requestid", "serviceinstanceid", "mode");
    }

    @Test(expected = ClassFormatError.class)
    public void createVfModuleClassFormatError() throws Exception {
        vnfAdapterRestV2.createVfModule("vnfid", "create", new CreateVfModuleRequest());
    }

    @Test(expected = ClassFormatError.class)
    public void updateVfModulClassFormatErrore() throws Exception {
        vnfAdapterRestV2.updateVfModule("vnfid", "moduleid", "mode", new UpdateVfModuleRequest());
    }

    @Test(expected = NullPointerException.class)
    public void rollbackVfModuleNullPointerException() throws Exception {
        vnfAdapterRestV2.rollbackVfModule("vnfid", "moduleid", new RollbackVfModuleRequest());
    }

}