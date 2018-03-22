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

public class VolumeAdapterRestExceptionTest {

    VolumeAdapterRest volumeAdapterRest = new VolumeAdapterRest();

    @Test(expected = ClassFormatError.class)
    public void createVNFVolumesClassFormatError() throws Exception {
        volumeAdapterRest.createVNFVolumes(new CreateVolumeGroupRequest());
    }

    @Test(expected = ClassFormatError.class)
    public void deleteVNFVolumesClassFormatError() throws Exception {
        volumeAdapterRest.deleteVNFVolumes("grpid", new DeleteVolumeGroupRequest());
    }

    @Test(expected = ClassFormatError.class)
    public void rollbackVNFVolumesClassFormatError() throws Exception {
        volumeAdapterRest.rollbackVNFVolumes("grpid", new RollbackVolumeGroupRequest());
    }

    @Test(expected = ClassFormatError.class)
    public void updateVNFVolumesClassFormatError() throws Exception {
        volumeAdapterRest.updateVNFVolumes("grpid", new UpdateVolumeGroupRequest());
    }

    @Test(expected = NullPointerException.class)
    public void queryVNFVolumesNullPointerException() throws Exception {
        volumeAdapterRest.queryVNFVolumes("grpid", "cloudid", "tenantid", "stackid", true, "requestid", "serviceid");
    }

    @Test
    public void testMap() throws Exception {
        volumeAdapterRest.testMap();
    }

}