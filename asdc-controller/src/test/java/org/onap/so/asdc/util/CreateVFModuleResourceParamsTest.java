/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Samsung Electronics Co., Ltd. All rights reserved.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.asdc.util;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.util.HashSet;
import org.junit.Test;
import org.onap.sdc.toscaparser.api.Group;
import org.onap.sdc.toscaparser.api.NodeTemplate;
import org.onap.so.asdc.client.test.emulators.JsonVfModuleMetaData;
import org.onap.so.asdc.installer.ToscaResourceStructure;
import org.onap.so.asdc.installer.VfResourceStructure;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;

public class CreateVFModuleResourceParamsTest {

    @Test
    public void testEmptyParams() {

        CreateVFModuleResourceParams params = new CreateVFModuleResourceParams();

        assertNull(params.getGroup());
        assertNull(params.getVfTemplate());
        assertNull(params.getToscaResourceStructure());
        assertNull(params.getVfResourceStructure());
        assertNull(params.getVfModuleData());
        assertNull(params.getVnfResource());
        assertNull(params.getService());
        assertNull(params.getExistingVnfcSet());
        assertNull(params.getExistingCvnfcSet());
    }

    @Test
    public void testFullParams() {

        CreateVFModuleResourceParams params = new CreateVFModuleResourceParams()
            .withGroup(mock(Group.class))
            .withVfTemplate(mock(NodeTemplate.class))
            .withToscaResourceStructure(new ToscaResourceStructure())
            .withVfResourceStructure(mock(VfResourceStructure.class))
            .withVfModuleData(new JsonVfModuleMetaData())
            .withVnfResource(new VnfResourceCustomization())
            .withService(new Service())
            .withExistingCvnfcSet(new HashSet<>())
            .withExistingVnfcSet(new HashSet<>());

        assertNotNull(params.getGroup());
        assertNotNull(params.getVfTemplate());
        assertNotNull(params.getToscaResourceStructure());
        assertNotNull(params.getVfResourceStructure());
        assertNotNull(params.getVfModuleData());
        assertNotNull(params.getVnfResource());
        assertNotNull(params.getService());
        assertNotNull(params.getExistingVnfcSet());
        assertNotNull(params.getExistingCvnfcSet());
    }
}
