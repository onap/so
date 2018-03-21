/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
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
package org.openecomp.mso.openstack.beans;

import org.junit.Test;
import org.openecomp.mso.entity.MsoRequest;

public class VnfRollbackTest {

    VnfRollback vnfRollback = new VnfRollback();

    @Test
    public void getVnfId() throws Exception {
        vnfRollback.getVnfId();
    }

    @Test
    public void setVnfId() throws Exception {
        vnfRollback.setVnfId("test");
    }

    @Test
    public void getTenantId() throws Exception {
        vnfRollback.getTenantId();
    }

    @Test
    public void setTenantId() throws Exception {
        vnfRollback.setTenantId("test");
    }

    @Test
    public void getCloudSiteId() throws Exception {
        vnfRollback.getCloudSiteId();
    }

    @Test
    public void setCloudSiteId() throws Exception {
        vnfRollback.setCloudSiteId("test");
    }

    @Test
    public void getTenantCreated() throws Exception {
        vnfRollback.getTenantCreated();
    }

    @Test
    public void setTenantCreated() throws Exception {
        vnfRollback.setTenantCreated(true);
    }

    @Test
    public void getVnfCreated() throws Exception {
        vnfRollback.getVnfCreated();
    }

    @Test
    public void setVnfCreated() throws Exception {
        vnfRollback.setVnfCreated(true);
    }

    @Test
    public void getMsoRequest() throws Exception {
        vnfRollback.getMsoRequest();
    }

    @Test
    public void setMsoRequest() throws Exception {
        vnfRollback.setMsoRequest(new MsoRequest());
    }

    @Test
    public void getVolumeGroupName() throws Exception {
        vnfRollback.getVolumeGroupName();
    }

    @Test
    public void setVolumeGroupName() throws Exception {
        vnfRollback.setVolumeGroupName("test");
    }

    @Test
    public void getVolumeGroupId() throws Exception {
        vnfRollback.getVolumeGroupId();
    }

    @Test
    public void setVolumeGroupId() throws Exception {
        vnfRollback.setVolumeGroupId("test");
    }

    @Test
    public void getRequestType() throws Exception {
        vnfRollback.getRequestType();
    }

    @Test
    public void setRequestType() throws Exception {
        vnfRollback.setRequestType("test");
    }

    @Test
    public void getVolumeGroupHeatStackId() throws Exception {
        vnfRollback.getVolumeGroupHeatStackId();
    }

    @Test
    public void setVolumeGroupHeatStackId() throws Exception {
        vnfRollback.setVolumeGroupHeatStackId("test");
    }

    @Test
    public void getBaseGroupHeatStackId() throws Exception {
        vnfRollback.getBaseGroupHeatStackId();
    }

    @Test
    public void setBaseGroupHeatStackId() throws Exception {
        vnfRollback.setBaseGroupHeatStackId("test");
    }

    @Test
    public void isBase() throws Exception {
        vnfRollback.isBase();
    }

    @Test
    public void setIsBase() throws Exception {
        vnfRollback.setIsBase(true);
    }

    @Test
    public void getVfModuleStackId() throws Exception {
        vnfRollback.getVfModuleStackId();
    }

    @Test
    public void setVfModuleStackId() throws Exception {
        vnfRollback.setVfModuleStackId("test");
    }

    @Test
    public void getModelCustomizationUuid() throws Exception {
        vnfRollback.getModelCustomizationUuid();
    }

    @Test
    public void setModelCustomizationUuid() throws Exception {
        vnfRollback.setModelCustomizationUuid("test");
    }

    @Test
    public void getMode() throws Exception {
        vnfRollback.getMode();
    }

    @Test
    public void setMode() throws Exception {
        vnfRollback.setMode("test");
    }

}