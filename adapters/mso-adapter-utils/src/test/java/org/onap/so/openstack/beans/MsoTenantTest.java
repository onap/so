/*
* ============LICENSE_START=======================================================
 * ONAP : SO
 * ================================================================================
 * Copyright (C) 2018 TechMahindra
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

package org.onap.so.openstack.beans;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class MsoTenantTest {
 MsoTenant ms = new MsoTenant();
    
    @Test
    public void test() {
       Map<String, String> map = new HashMap<>();
       map.put("id","name");
       ms.setTenantId("tenantId");
       ms.setTenantName("tenantName");
       ms.setMetadata(map);
       assert(ms.getMetadata().equals(map));
       assert(ms.getTenantId().equals("tenantId"));
       assert(ms.getTenantName().equals("tenantName"));
    }
}
