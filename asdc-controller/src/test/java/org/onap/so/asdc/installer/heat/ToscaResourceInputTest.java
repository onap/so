/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Huawei Intellectual Property. All rights reserved.
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

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.onap.sdc.tosca.parser.api.IEntityDetails;
import org.onap.sdc.tosca.parser.api.ISdcCsarHelper;
import org.onap.sdc.tosca.parser.elements.queries.EntityQuery;
import org.onap.sdc.tosca.parser.elements.queries.TopologyTemplateQuery;
import org.onap.sdc.tosca.parser.enums.EntityTemplateType;
import org.onap.sdc.tosca.parser.enums.SdcTypes;
import org.onap.sdc.toscaparser.api.NodeTemplate;
import org.onap.sdc.toscaparser.api.Property;
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.onap.sdc.toscaparser.api.functions.GetInput;
import org.onap.sdc.toscaparser.api.parameters.Input;
import org.onap.so.asdc.client.exceptions.ArtifactInstallerException;
import org.onap.so.asdc.installer.ToscaResourceStructure;
import org.onap.so.db.catalog.beans.Service;
import java.util.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class ToscaResourceInputTest {
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    ISdcCsarHelper sdcCsarHelper;

    @Mock
    NodeTemplate nodeTemplate;

    @Mock
    IEntityDetails entityDetails;

    @Mock
    Property property;

    @Mock
    GetInput getInput;

    @Mock
    Input input;

    @Mock
    ToscaResourceInstaller toscaInstaller;

    @Mock
    ToscaResourceStructure toscaStructure;

    @Test
    public void getListResourceInput() {
        ToscaResourceInstaller toscaResourceInstaller = new ToscaResourceInstaller();
        LinkedHashMap<String, Property> hashMap = new LinkedHashMap<>();
        hashMap.put("key1", property);
        Map<String, Object> map = new HashMap<>();
        map.put("customizationUUID", "69df3303-d2b3-47a1-9d04-41604d3a95fd");
        Metadata metadata = new Metadata(map);
        when(entityDetails.getProperties()).thenReturn(hashMap);
        when(property.getValue()).thenReturn(getInput);
        when(getInput.getInputName()).thenReturn("nameKey");
        when(input.getName()).thenReturn("nameKey");
        when(input.getDefault()).thenReturn("defaultValue");
        when(getInput.toString()).thenReturn("getinput:[sites,INDEX,role]");
        when(entityDetails.getMetadata()).thenReturn(metadata);
        List<Input> inputs = new ArrayList<>();
        inputs.add(input);
        String resourceInput = toscaResourceInstaller.getVnfcResourceInput(entityDetails, inputs);
        assertEquals("{\\\"key1\\\":\\\"[sites,INDEX,role]|defaultValue\\\"}", resourceInput);
    }

    @Test
    public void processResourceSequenceTest() {
        ToscaResourceInstaller toscaResourceInstaller = new ToscaResourceInstaller();
        ToscaResourceStructure toscaResourceStructure = new ToscaResourceStructure();
        toscaResourceStructure.setSdcCsarHelper(sdcCsarHelper);
        ArrayList<Input> inputs = new ArrayList<>();
        Service service = new Service();

        HashMap<String, Object> hashMap = new HashMap();
        hashMap.put("name", "node1");

        Metadata metadata = new Metadata(hashMap);
        when(entityDetails.getMetadata()).thenReturn(metadata);
        when(sdcCsarHelper.getServiceInputs()).thenReturn(inputs);
        when(toscaResourceInstaller.getEntityDetails(toscaResourceStructure,
                EntityQuery.newBuilder(EntityTemplateType.NODE_TEMPLATE),
                TopologyTemplateQuery.newBuilder(SdcTypes.SERVICE), false)).thenReturn(Arrays.asList(entityDetails));


        when(entityDetails.getRequirements()).thenReturn(null);


        toscaResourceInstaller.processResourceSequence(toscaResourceStructure, service);
        assertEquals(service.getResourceOrder(), "");
    }

    @Test
    public void resouceInputTest() throws ArtifactInstallerException {
        ToscaResourceInstaller toscaResourceInstaller = new ToscaResourceInstaller();
        ToscaResourceStructure toscaResourceStructure = new ToscaResourceStructure();

        toscaResourceStructure.setSdcCsarHelper(sdcCsarHelper);

        Map hashMap = new HashMap();
        hashMap.put("customizationUUID", "id1");
        Metadata metadata = new Metadata(hashMap);

        Map<String, Property> propertyMap = new HashMap<String, Property>();
        propertyMap.put("prop1", property);

        when(toscaResourceInstaller.getEntityDetails(toscaResourceStructure,
                EntityQuery.newBuilder(EntityTemplateType.NODE_TEMPLATE),
                TopologyTemplateQuery.newBuilder(SdcTypes.SERVICE), false)).thenReturn(Arrays.asList(entityDetails));

        when(entityDetails.getMetadata()).thenReturn(metadata);
        when(entityDetails.getProperties()).thenReturn(propertyMap);
        when(property.getValue()).thenReturn("value1");

        String resourceInput = toscaResourceInstaller.getResourceInput(toscaResourceStructure, "id1");
        assertEquals("{}", resourceInput);
    }

    @Test
    public void resouceInputGetInputTest() throws ArtifactInstallerException {
        ToscaResourceInstaller toscaResourceInstaller = new ToscaResourceInstaller();
        ToscaResourceStructure toscaResourceStructure = new ToscaResourceStructure();

        toscaResourceStructure.setSdcCsarHelper(sdcCsarHelper);

        HashMap hashMap = new HashMap();
        hashMap.put("customizationUUID", "id1");
        Metadata metadata = new Metadata(hashMap);

        Map<String, Property> propertyMap = new HashMap<String, Property>();
        propertyMap.put("prop1", property);

        when(toscaResourceInstaller.getEntityDetails(toscaResourceStructure,
                EntityQuery.newBuilder(EntityTemplateType.NODE_TEMPLATE),
                TopologyTemplateQuery.newBuilder(SdcTypes.SERVICE), false)).thenReturn(Arrays.asList(entityDetails));
        when(sdcCsarHelper.getServiceInputs()).thenReturn(Arrays.asList(input));
        when(entityDetails.getMetadata()).thenReturn(metadata);
        when(entityDetails.getProperties()).thenReturn(propertyMap);
        when(property.getValue()).thenReturn(getInput);
        when(getInput.getInputName()).thenReturn("res_key");
        when(input.getName()).thenReturn("res_key");
        when(input.getDefault()).thenReturn("default_value");

        String resourceInput = toscaResourceInstaller.getResourceInput(toscaResourceStructure, "id1");
        assertEquals("{}", resourceInput);
    }

    @Test
    public void resouceInputGetInputDefaultIntegerTest() throws ArtifactInstallerException {
        ToscaResourceInstaller toscaResourceInstaller = new ToscaResourceInstaller();
        ToscaResourceStructure toscaResourceStructure = new ToscaResourceStructure();

        toscaResourceStructure.setSdcCsarHelper(sdcCsarHelper);

        HashMap hashMap = new HashMap();
        hashMap.put("customizationUUID", "id1");
        Metadata metadata = new Metadata(hashMap);

        Map<String, Property> propertyMap = new HashMap<String, Property>();
        propertyMap.put("prop1", property);

        when(toscaResourceInstaller.getEntityDetails(toscaResourceStructure,
                EntityQuery.newBuilder(EntityTemplateType.NODE_TEMPLATE),
                TopologyTemplateQuery.newBuilder(SdcTypes.SERVICE), false)).thenReturn(Arrays.asList(entityDetails));
        when(sdcCsarHelper.getServiceInputs()).thenReturn(Arrays.asList(input));
        when(entityDetails.getMetadata()).thenReturn(metadata);
        when(entityDetails.getProperties()).thenReturn(propertyMap);
        when(property.getValue()).thenReturn(getInput);
        when(getInput.getInputName()).thenReturn("res_key");
        when(input.getName()).thenReturn("res_key");
        Integer integer = Integer.valueOf(10);
        when(input.getDefault()).thenReturn(integer);


        String resourceInput = toscaResourceInstaller.getResourceInput(toscaResourceStructure, "id1");
        assertEquals("{}", resourceInput);
    }

    @Test
    public void resouceInputGetInputNoPropertyTest() throws ArtifactInstallerException {
        ToscaResourceInstaller toscaResourceInstaller = new ToscaResourceInstaller();
        ToscaResourceStructure toscaResourceStructure = new ToscaResourceStructure();

        toscaResourceStructure.setSdcCsarHelper(sdcCsarHelper);

        HashMap hashMap = new HashMap();
        hashMap.put("customizationUUID", "id1");
        Metadata metadata = new Metadata(hashMap);

        LinkedHashMap propertyMap = new LinkedHashMap();

        when(sdcCsarHelper.getServiceInputs()).thenReturn(Arrays.asList(input));
        when(nodeTemplate.getMetaData()).thenReturn(metadata);
        when(nodeTemplate.getProperties()).thenReturn(propertyMap);

        String resourceInput = toscaResourceInstaller.getResourceInput(toscaResourceStructure, "id1");
        assertEquals("{}", resourceInput);
    }
}
