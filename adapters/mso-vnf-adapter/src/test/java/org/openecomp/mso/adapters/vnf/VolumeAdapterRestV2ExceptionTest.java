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
import org.openecomp.mso.adapters.vnfrest.CreateVolumeGroupRequest;
import org.openecomp.mso.adapters.vnfrest.DeleteVolumeGroupRequest;
import org.openecomp.mso.adapters.vnfrest.RollbackVolumeGroupRequest;
import org.openecomp.mso.adapters.vnfrest.UpdateVolumeGroupRequest;
import org.openecomp.mso.adapters.vnfrest.VolumeGroupRollback;

public class VolumeAdapterRestV2ExceptionTest {

    VolumeAdapterRestV2 volumeAdapterRestV2 = new VolumeAdapterRestV2();

    @Test(expected = ClassFormatError.class)
    public void createVNFVolumesClassFormatError() throws Exception {
        volumeAdapterRestV2.createVNFVolumes("mode", new CreateVolumeGroupRequest());
    }

    @Test(expected = ClassFormatError.class)
    public void deleteVNFVolumesClassFormatError() throws Exception {
        volumeAdapterRestV2.deleteVNFVolumes("volumegrpid", "mode", new DeleteVolumeGroupRequest());
    }

    @Test(expected = ClassFormatError.class)
    public void rollbackVNFVolumesClassFormatError() throws Exception {
        RollbackVolumeGroupRequest rollbackVolumeGroupRequest = new RollbackVolumeGroupRequest();
        VolumeGroupRollback volumeGroupRollback = new VolumeGroupRollback();
        volumeGroupRollback.setVolumeGroupId("grpid");
        rollbackVolumeGroupRequest.setVolumeGroupRollback(volumeGroupRollback);
        volumeAdapterRestV2.rollbackVNFVolumes("grpid", rollbackVolumeGroupRequest);
    }

    @Test(expected = ClassFormatError.class)
    public void updateVNFVolumesClassFormatError() throws Exception {
        volumeAdapterRestV2.updateVNFVolumes("vgid", "mode", new UpdateVolumeGroupRequest());
    }

    @Test(expected = NullPointerException.class)
    public void queryVNFVolumesNullPointerException() throws Exception {
        volumeAdapterRestV2.queryVNFVolumes("vgid", "cloudid", "tenantid",
                "stackid", true, "test", "test", "test");
    }

}