/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

package org.onap.so.asdc.installer.heat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.onap.sdc.tosca.parser.api.IEntityDetails;
import org.onap.sdc.tosca.parser.api.ISdcCsarHelper;
import org.onap.sdc.tosca.parser.impl.SdcPropertyNames;
import org.onap.sdc.toscaparser.api.CapabilityAssignment;
import org.onap.sdc.toscaparser.api.Property;
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.onap.so.asdc.installer.ToscaResourceStructure;
import org.onap.so.db.catalog.beans.AllottedResource;
import org.onap.so.db.catalog.beans.AllottedResourceCustomization;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.data.repository.AllottedResourceCustomizationRepository;
import org.onap.so.db.catalog.data.repository.AllottedResourceRepository;

public class AllottedResourceInstallerTest {

    private AllottedResourceRepository allottedRepo;
    private AllottedResourceCustomizationRepository allottedCustomizationRepo;
    private AllottedResourceInstaller installer;
    private ToscaResourceStructure toscaResourceStruct;

    @Before
    public void setUp() {
        allottedRepo = mock(AllottedResourceRepository.class);
        allottedCustomizationRepo = mock(AllottedResourceCustomizationRepository.class);
        installer = new AllottedResourceInstaller(allottedRepo, allottedCustomizationRepo);
        toscaResourceStruct = new ToscaResourceStructure();
        toscaResourceStruct.setSdcCsarHelper(mock(ISdcCsarHelper.class));
    }

    @Test
    public void existingAllottedResourceIsReused() throws Exception {
        Metadata metadata = mock(Metadata.class);
        when(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID)).thenReturn("ar-cust-uuid");
        when(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID)).thenReturn("ar-model-uuid");
        IEntityDetails arEntity = mock(IEntityDetails.class);
        when(arEntity.getMetadata()).thenReturn(metadata);
        AllottedResource existing = new AllottedResource();
        existing.setModelUUID("ar-model-uuid");
        when(allottedRepo.findResourceByModelUUID("ar-model-uuid")).thenReturn(existing);
        Service service = new Service();
        List<IEntityDetails> arEntities = new ArrayList<>();
        arEntities.add(arEntity);

        installer.processAllottedResources(arEntities, toscaResourceStruct, service);

        assertEquals(1, service.getAllottedCustomizations().size());
        AllottedResourceCustomization customization = service.getAllottedCustomizations().get(0);
        assertSame(existing, customization.getAllottedResource());
        assertSame(existing, toscaResourceStruct.getAllottedResource());
    }

    @Test
    public void emptyEntityListTouchesNoRepository() throws Exception {
        Service service = new Service();

        installer.processAllottedResources(new ArrayList<>(), toscaResourceStruct, service);

        assertEquals(0, service.getAllottedCustomizations().size());
        verifyNoInteractions(allottedRepo, allottedCustomizationRepo);
    }

    @Test
    public void scalableCapabilitySetsMinAndMaxInstances() {
        AllottedResourceCustomization customization = installer
                .createAllottedResourceCustomization(arEntityWithScalableCapability("1", "5"), toscaResourceStruct);

        assertEquals(Integer.valueOf(1), customization.getMinInstances());
        assertEquals(Integer.valueOf(5), customization.getMaxInstances());
    }

    @Test
    public void blankOrNonNumericScalableValuesAreStoredAsNull() {
        AllottedResourceCustomization customization = installer.createAllottedResourceCustomization(
                arEntityWithScalableCapability("", "UNBOUNDED"), toscaResourceStruct);

        assertNull(customization.getMinInstances());
        assertNull(customization.getMaxInstances());
    }

    private IEntityDetails arEntityWithScalableCapability(String minInstances, String maxInstances) {
        CapabilityAssignment scalable = mock(CapabilityAssignment.class);
        when(scalable.getName()).thenReturn("scalable");
        LinkedHashMap<String, Property> properties = new LinkedHashMap<>();
        properties.put(SdcPropertyNames.PROPERTY_NAME_MININSTANCES, property(minInstances));
        properties.put(SdcPropertyNames.PROPERTY_NAME_MAXINSTANCES, property(maxInstances));
        when(scalable.getProperties()).thenReturn(properties);
        IEntityDetails arEntity = mock(IEntityDetails.class);
        when(arEntity.getMetadata()).thenReturn(mock(Metadata.class));
        when(arEntity.getCapabilities()).thenReturn(List.of(scalable));
        return arEntity;
    }

    private static Property property(Object value) {
        Property property = mock(Property.class);
        when(property.getValue()).thenReturn(value);
        return property;
    }
}
