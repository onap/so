/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
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

package org.openecomp.mso.db.catalog.test;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.mso.db.catalog.beans.AllottedResource;
import org.openecomp.mso.db.catalog.beans.AllottedResourceCustomization;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.Timestamp;

public class AllottedResourceCustomizationTest {
    @Test
    public void test(){
        AllottedResource allottedResource = new AllottedResource();
        allottedResource.setModelUuid("ModelUuid");
        allottedResource.setCreated(new Timestamp(System.currentTimeMillis()));
        allottedResource.setModelVersion("ModelVersion");
        allottedResource.setDescription("Description");
        allottedResource.setModelInvariantUuid("ModelInvariantUuid");
        allottedResource.setModelName("ModelName");
        allottedResource.setSubcategory("Subcategory");
        allottedResource.setToscaNodeType("ToscaNodeType");
        allottedResource.setVersion("Version");

        AllottedResourceCustomization allottedResourceCustomization = new AllottedResourceCustomization();
        allottedResourceCustomization.setCreated(new Timestamp(System.currentTimeMillis()));
        allottedResourceCustomization.setAllottedResource(allottedResource);
        allottedResourceCustomization.setVersion("Version");
        allottedResourceCustomization.setArModelUuid("ArModelUuid");
        allottedResourceCustomization.setMaxInstances(100);
        allottedResourceCustomization.setMinInstances(1);
        allottedResourceCustomization.setModelCustomizationUuid("ModelCustomizationUuid");
        allottedResourceCustomization.setModelInstanceName("ModelInstanceName");
        allottedResourceCustomization.setNfFunction("NfFunction");
        allottedResourceCustomization.setNfNamingCode("NfNamingCode");
        allottedResourceCustomization.setNfRole("NfRole");
        allottedResourceCustomization.setNfType("NfType");
        allottedResourceCustomization.setTargetNetworkRole("TargetNetworkRole");
        allottedResourceCustomization.setProvidingServiceModelUuid("ProvidingServiceModelUuid");
        allottedResourceCustomization.setProvidingServiceModelInvariantUuid("ProvidingServiceModelInvariantUuid");
        allottedResourceCustomization.setProvidingServiceModelName("ProvidingServiceModelName");

        assertNotNull(allottedResource.getModelUuid());
        assertNotNull(allottedResource.getCreated());
        assertNotNull(allottedResource.getModelVersion());
        assertNotNull(allottedResource.getDescription());
        assertNotNull(allottedResource.getModelInvariantUuid());
        assertNotNull(allottedResource.getModelName());
        assertNotNull(allottedResource.getSubcategory());
        assertNotNull(allottedResource.getToscaNodeType());
        assertNotNull(allottedResource.getVersion());

        assertNotNull(allottedResourceCustomization.getAllottedResource());
        assertNotNull(allottedResourceCustomization.getVersion());
        assertNotNull(allottedResourceCustomization.getCreated());
        assertNotNull(allottedResourceCustomization.getArModelUuid());
        assertNotNull(allottedResourceCustomization.getMaxInstances());
        assertNotNull(allottedResourceCustomization.getMinInstances());
        assertNotNull(allottedResourceCustomization.getModelCustomizationUuid());
        assertNotNull(allottedResourceCustomization.getModelInstanceName());
        assertNotNull(allottedResourceCustomization.getNfFunction());
        assertNotNull(allottedResourceCustomization.getNfNamingCode());
        assertNotNull(allottedResourceCustomization.getNfRole());
        assertNotNull(allottedResourceCustomization.getNfType());
        assertNotNull(allottedResourceCustomization.getTargetNetworkRole());
        assertNotNull(allottedResourceCustomization.getProvidingServiceModelUuid());
        assertNotNull(allottedResourceCustomization.getProvidingServiceModelInvariantUuid());
        assertNotNull(allottedResourceCustomization.getProvidingServiceModelName());
    }
}
