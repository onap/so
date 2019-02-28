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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.Set;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.onap.sdc.toscaparser.api.Group;
import org.onap.sdc.toscaparser.api.NodeTemplate;
import org.onap.so.asdc.installer.IVfModuleData;
import org.onap.so.asdc.installer.ToscaResourceStructure;
import org.onap.so.asdc.installer.VfResourceStructure;
import org.onap.so.db.catalog.beans.CvnfcCustomization;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.beans.VnfcCustomization;

public class CreateVFModuleResourceBuilderTest {
    private Group group;
    private NodeTemplate vfTemplate;
    private ToscaResourceStructure toscaResourceStructure;
    private VfResourceStructure vfResourceStructure;
    private VnfResourceCustomization vnfResource;
    private Service service;
    private Set<CvnfcCustomization> existingCvnfcSet;
    private Set<VnfcCustomization> existingVnfcSet;
    IVfModuleData vfMetadata;

    public class SelfReturningAnswer implements Answer<Object> {
        public Object answer(InvocationOnMock invocation) throws Throwable {
            Object mock = invocation.getMock();
            if (invocation.getMethod().getReturnType().isInstance(mock)) {
                return mock;
            } else {
                return RETURNS_DEFAULTS.answer(invocation);
            }
        }
    }

    @Test
    public void testBuilder() {
        CreateVFModuleResourceBuilder mockBuilder =
                mock(CreateVFModuleResourceBuilder.class, new SelfReturningAnswer());

        when(mockBuilder.setGroup(group).setVfTemplate(vfTemplate).setToscaResourceStructure(toscaResourceStructure)
                .setVfResourceStructure(vfResourceStructure).setVfModuleData(vfMetadata).setVnfResource(vnfResource)
                .setService(service).setExistingCvnfcSet(existingCvnfcSet).setExistingVnfcSet(existingVnfcSet))
                        .thenReturn(mockBuilder);

        CreateVFModuleResourceBuilder setExistingVnfcSet = mockBuilder.setGroup(group).setVfTemplate(vfTemplate)
                .setToscaResourceStructure(toscaResourceStructure).setVfResourceStructure(vfResourceStructure)
                .setVfModuleData(vfMetadata).setVnfResource(vnfResource).setService(service)
                .setExistingCvnfcSet(existingCvnfcSet).setExistingVnfcSet(existingVnfcSet);

        assertNotNull(setExistingVnfcSet);
        assertEquals(mockBuilder, setExistingVnfcSet);
    }
}
