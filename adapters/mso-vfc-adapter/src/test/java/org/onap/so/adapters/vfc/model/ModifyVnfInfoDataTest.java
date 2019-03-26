/*
 * Copyright (C) 2019 Verizon. All Rights Reserved Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.onap.so.adapters.vfc.model;

import org.junit.Test;
import java.util.HashMap;
import static org.junit.Assert.*;

public class ModifyVnfInfoDataTest {
    ModifyVnfInfoData modifyVnfInfoData = new ModifyVnfInfoData();

    @Test
    public void getVnfInstanceId() {
        modifyVnfInfoData.getVnfInstanceId();
    }

    @Test
    public void setVnfInstanceId() {
        modifyVnfInfoData.setVnfInstanceId("c9f0a95e-dea0-4698-96e5-5a79bc5a233d");
    }

    @Test
    public void getVnfInstanceName() {
        modifyVnfInfoData.getVnfInstanceName();
    }

    @Test
    public void setVnfInstanceName() {
        modifyVnfInfoData.setVnfInstanceName("Dummy vnfInstanceName");
    }

    @Test
    public void getVnfInstanceDescription() {
        modifyVnfInfoData.getVnfInstanceDescription();
    }

    @Test
    public void setVnfInstanceDescription() {
        modifyVnfInfoData.setVnfInstanceDescription("Dummy vnfInstanceDescription");
    }

    @Test
    public void getVnfPkgId() {
        modifyVnfInfoData.getVnfPkgId();
    }

    @Test
    public void setVnfPkgId() {
        modifyVnfInfoData.setVnfPkgId("c9f0a95e-dea0-4698-96e5-5a79bc5a233d");
    }

    @Test
    public void getVnfConfigurableProperties() {
        modifyVnfInfoData.getVnfConfigurableProperties();
    }

    @Test
    public void setVnfConfigurableProperties() {
        modifyVnfInfoData.setVnfConfigurableProperties(new HashMap<>());
    }

    @Test
    public void getMetadata() {
        modifyVnfInfoData.getMetadata();
    }

    @Test
    public void setMetadata() {
        modifyVnfInfoData.setMetadata(new HashMap<>());
    }

    @Test
    public void getExtensions() {
        modifyVnfInfoData.getExtensions();
    }

    @Test
    public void setExtensions() {
        modifyVnfInfoData.setExtensions(new HashMap<>());
    }
}
