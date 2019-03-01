/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.adapters.vnf;

import org.onap.so.adapters.vnfrest.CreateVolumeGroupRequest;
import org.onap.so.adapters.vnfrest.DeleteVolumeGroupRequest;
import org.onap.so.adapters.vnfrest.RollbackVolumeGroupRequest;
import org.onap.so.adapters.vnfrest.UpdateVolumeGroupRequest;
import org.onap.so.adapters.vnfrest.VolumeGroupRollback;
import org.onap.so.entity.MsoRequest;

import java.util.HashMap;
import java.util.Map;

public class VolumeGroupAdapterCommon extends BaseRestTestUtils {



    protected static final String MSO_REQUEST_ID = "62265093-277d-4388-9ba6-449838ade586";
    protected static final String MSO_SERVICE_INSTANCE_ID = "4147e06f-1b89-49c5-b21f-4faf8dc9805a";
    protected static final String CLOUDSITE_ID = "mtn13";
    protected static final String TENANT_ID = "0422ffb57ba042c0800a29dc85ca70f8";
    protected static final String VOUME_GROUP_NAME = "DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001";
    protected static final String VOLUME_GROUP_ID = "0422ffb57ba042c0800a29dc85ca70v1";
    protected static final String VNF_TYPE = "TestVnfType";
    protected static final String VNF_VERSION = "1.0";
    protected static final String VF_MODULE_TYPE = "TestModule-0";
    protected static final String MODEL_CUSTOMIZATION_UUID = "9b339a61-69ca-465f-86b8-1c72c582b8e8";

    protected UpdateVolumeGroupRequest buildUpdateVolumeGroupRequest() {
        UpdateVolumeGroupRequest request = new UpdateVolumeGroupRequest();
        request.setCloudSiteId(CLOUDSITE_ID);
        request.setTenantId(TENANT_ID);
        request.setVolumeGroupId(VOLUME_GROUP_ID);
        request.setVnfType(VNF_TYPE);
        request.setVnfVersion(VNF_VERSION);
        request.setVfModuleType(VF_MODULE_TYPE);
        request.setModelCustomizationUuid(MODEL_CUSTOMIZATION_UUID);
        request.setSkipAAI(true);
        request.setVolumeGroupStackId(VOUME_GROUP_NAME);
        Map<String, Object> volumeGroupParams = getVolumeGroupParamsMap();
        request.setVolumeGroupParams(volumeGroupParams);
        MsoRequest msoReq = getMsoRequest();
        request.setMsoRequest(msoReq);
        return request;
    }

    protected RollbackVolumeGroupRequest buildRollbackVolumeGroupRequest() {
        RollbackVolumeGroupRequest request = new RollbackVolumeGroupRequest();
        VolumeGroupRollback volumeGroupRollback = new VolumeGroupRollback();
        volumeGroupRollback.setCloudSiteId(CLOUDSITE_ID);
        volumeGroupRollback.setMessageId(MSO_REQUEST_ID);
        volumeGroupRollback.setTenantId(TENANT_ID);
        MsoRequest msoReq = getMsoRequest();
        volumeGroupRollback.setVolumeGroupCreated(true);
        volumeGroupRollback.setVolumeGroupId(VOLUME_GROUP_ID);
        volumeGroupRollback.setVolumeGroupStackId(VOLUME_GROUP_ID);
        volumeGroupRollback.setMsoRequest(msoReq);
        request.setVolumeGroupRollback(volumeGroupRollback);
        return request;
    }

    protected DeleteVolumeGroupRequest buildDeleteVolumeGroupRequest() {
        DeleteVolumeGroupRequest request = new DeleteVolumeGroupRequest();
        request.setCloudSiteId(CLOUDSITE_ID);
        request.setTenantId(TENANT_ID);
        request.setVolumeGroupStackId("testStackId");
        request.setVolumeGroupId(VOLUME_GROUP_ID);
        MsoRequest msoReq = getMsoRequest();
        request.setMsoRequest(msoReq);
        return request;
    }

    protected CreateVolumeGroupRequest buildCreateVfModuleRequest() {
        CreateVolumeGroupRequest request = new CreateVolumeGroupRequest();
        request.setCloudSiteId(CLOUDSITE_ID);
        request.setTenantId(TENANT_ID);
        request.setVolumeGroupName(VOUME_GROUP_NAME);
        request.setVolumeGroupId(VOLUME_GROUP_ID);
        request.setVnfType(VNF_TYPE);
        request.setVnfVersion(VNF_VERSION);
        request.setVfModuleType(VF_MODULE_TYPE);
        request.setModelCustomizationUuid(MODEL_CUSTOMIZATION_UUID);
        request.setSkipAAI(true);
        request.setFailIfExists(false);
        request.setFailIfExists(true);
        request.setEnableBridge(false);
        request.setSuppressBackout(true);
        Map<String, Object> volumeGroupParams = getVolumeGroupParamsMap();
        request.setVolumeGroupParams(volumeGroupParams);
        MsoRequest msoReq = getMsoRequest();
        request.setMsoRequest(msoReq);

        return request;
    }

    private Map<String, Object> getVolumeGroupParamsMap() {
        Map<String, Object> volumeGroupParams =  new HashMap<>();
        volumeGroupParams.put("fsb_volume_type_0","volume_type");
        volumeGroupParams.put("fsb_volume_image_name_1","vol_img_1");
        volumeGroupParams.put("fsb_volume_image_name_0","vol_img_0");
        volumeGroupParams.put("fsb_volume_size_0","100");
        return volumeGroupParams;
    }

    private MsoRequest getMsoRequest() {
        MsoRequest msoReq = new MsoRequest();
        msoReq.setRequestId(MSO_REQUEST_ID);
        msoReq.setServiceInstanceId(MSO_SERVICE_INSTANCE_ID);
        return msoReq;
    }
}
