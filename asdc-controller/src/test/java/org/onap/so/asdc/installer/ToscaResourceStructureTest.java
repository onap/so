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

package org.onap.so.asdc.installer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import java.util.ArrayList;
import java.util.HashMap;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.sdc.tosca.parser.impl.SdcCsarHelperImpl;
import org.onap.sdc.toscaparser.api.NodeTemplate;
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.onap.so.asdc.client.exceptions.ASDCDownloadException;
import org.onap.so.asdc.client.test.emulators.ArtifactInfoImpl;
import org.onap.so.db.catalog.beans.AllottedResource;
import org.onap.so.db.catalog.beans.AllottedResourceCustomization;
import org.onap.so.db.catalog.beans.NetworkResource;
import org.onap.so.db.catalog.beans.NetworkResourceCustomization;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.TempNetworkHeatTemplateLookup;
import org.onap.so.db.catalog.beans.ToscaCsar;
import org.onap.so.db.catalog.beans.VfModule;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;

public class ToscaResourceStructureTest {
    private ArtifactInfoImpl artifactInfo;
    private SdcCsarHelperImpl sdcCsarHelper;

    private ToscaResourceStructure toscaResourceStructure;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void toscaResourceStructureBeanTest() {
        artifactInfo = mock(ArtifactInfoImpl.class);
        sdcCsarHelper = mock(SdcCsarHelperImpl.class);

        toscaResourceStructure = new ToscaResourceStructure();
        toscaResourceStructure.setHeatTemplateUUID("heatTemplateUUID");
        toscaResourceStructure.setAllottedList(new ArrayList<NodeTemplate>());
        toscaResourceStructure.setSdcCsarHelper(sdcCsarHelper);
        toscaResourceStructure.setServiceMetadata(new Metadata(new HashMap<>()));
        toscaResourceStructure.setCatalogService(new Service());
        toscaResourceStructure.setNetworkTypes(new ArrayList<>());
        toscaResourceStructure.setVfTypes(new ArrayList<>());
        toscaResourceStructure.setCatalogResourceCustomization(new AllottedResourceCustomization());
        toscaResourceStructure.setCatalogNetworkResourceCustomization(new NetworkResourceCustomization());
        toscaResourceStructure.setCatalogNetworkResource(new NetworkResource());
        toscaResourceStructure.setCatalogVfModule(new VfModule());
        toscaResourceStructure.setVnfResourceCustomization(new VnfResourceCustomization());
        toscaResourceStructure.setVfModuleCustomization(new VfModuleCustomization());
        toscaResourceStructure.setAllottedResource(new AllottedResource());
        toscaResourceStructure.setAllottedResourceCustomization(new AllottedResourceCustomization());
        toscaResourceStructure.setCatalogTempNetworkHeatTemplateLookup(new TempNetworkHeatTemplateLookup());
        toscaResourceStructure.setHeatFilesUUID("heatFilesUUID");
        toscaResourceStructure.setToscaArtifact(artifactInfo);
        toscaResourceStructure.setToscaCsar(new ToscaCsar());
        toscaResourceStructure.setVolHeatTemplateUUID("volHeatTemplateUUID");
        toscaResourceStructure.setEnvHeatTemplateUUID("envHeatTemplateUUID");
        toscaResourceStructure.setServiceVersion("serviceVersion");
        toscaResourceStructure.setWorkloadPerformance("workloadPerformance");
        toscaResourceStructure.setVfModule(new VfModule());
        toscaResourceStructure.setTempNetworkHeatTemplateLookup(new TempNetworkHeatTemplateLookup());
        toscaResourceStructure.setSuccessfulDeployment();

        assertEquals("heatTemplateUUID", toscaResourceStructure.getHeatTemplateUUID());
        assertThat(toscaResourceStructure.getAllottedList()).usingRecursiveComparison()
                .isEqualTo(new ArrayList<NodeTemplate>());
        assertEquals(sdcCsarHelper, toscaResourceStructure.getSdcCsarHelper());
        assertThat(toscaResourceStructure.getServiceMetadata()).usingRecursiveComparison()
                .isEqualTo(new Metadata(new HashMap<>()));
        assertThat(toscaResourceStructure.getCatalogService()).usingRecursiveComparison().isEqualTo(new Service());
        assertThat(toscaResourceStructure.getNetworkTypes()).usingRecursiveComparison().isEqualTo(new ArrayList<>());
        assertThat(toscaResourceStructure.getVfTypes()).usingRecursiveComparison().isEqualTo(new ArrayList<>());
        assertThat(toscaResourceStructure.getCatalogResourceCustomization()).usingRecursiveComparison()
                .isEqualTo(new AllottedResourceCustomization());
        assertThat(toscaResourceStructure.getCatalogNetworkResourceCustomization()).usingRecursiveComparison()
                .isEqualTo(new NetworkResourceCustomization());
        assertThat(toscaResourceStructure.getCatalogNetworkResource()).usingRecursiveComparison()
                .isEqualTo(new NetworkResource());
        assertThat(toscaResourceStructure.getCatalogVfModule()).usingRecursiveComparison().isEqualTo(new VfModule());
        assertThat(toscaResourceStructure.getVnfResourceCustomization()).usingRecursiveComparison()
                .isEqualTo(new VnfResourceCustomization());
        assertThat(toscaResourceStructure.getVfModuleCustomization()).usingRecursiveComparison()
                .isEqualTo(new VfModuleCustomization());
        assertThat(toscaResourceStructure.getAllottedResource()).usingRecursiveComparison()
                .isEqualTo(new AllottedResource());
        assertThat(toscaResourceStructure.getAllottedResourceCustomization()).usingRecursiveComparison()
                .isEqualTo(new AllottedResourceCustomization());
        assertThat(toscaResourceStructure.getCatalogTempNetworkHeatTemplateLookup()).usingRecursiveComparison()
                .isEqualTo(new TempNetworkHeatTemplateLookup());
        assertEquals("heatFilesUUID", toscaResourceStructure.getHeatFilesUUID());
        assertEquals(artifactInfo, toscaResourceStructure.getToscaArtifact());
        assertThat(toscaResourceStructure.getToscaCsar()).usingRecursiveComparison().isEqualTo(new ToscaCsar());
        assertEquals("volHeatTemplateUUID", toscaResourceStructure.getVolHeatTemplateUUID());
        assertEquals("envHeatTemplateUUID", toscaResourceStructure.getEnvHeatTemplateUUID());
        assertEquals("serviceVersion", toscaResourceStructure.getServiceVersion());
        assertEquals("workloadPerformance", toscaResourceStructure.getWorkloadPerformance());
        assertThat(toscaResourceStructure.getVfModule()).usingRecursiveComparison().isEqualTo(new VfModule());
        assertThat(toscaResourceStructure.getTempNetworkHeatTemplateLookup()).usingRecursiveComparison()
                .isEqualTo(new TempNetworkHeatTemplateLookup());
        assertEquals(true, toscaResourceStructure.isDeployedSuccessfully());
    }

    @Test
    public void updateResourceStructureExceptionTest() throws Exception {
        expectedException.expect(ASDCDownloadException.class);

        artifactInfo = mock(ArtifactInfoImpl.class);
        toscaResourceStructure = new ToscaResourceStructure();

        doReturn("artifactName").when(artifactInfo).getArtifactName();

        toscaResourceStructure.updateResourceStructure(artifactInfo);
    }
}
