/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Nokia
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

package org.onap.so.adapters.vdu.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Lists;
import java.util.List;
import org.junit.Test;
import org.onap.so.adapters.vdu.VduArtifact;
import org.onap.so.adapters.vdu.VduArtifact.ArtifactType;
import org.onap.so.adapters.vdu.VduModelInfo;
import org.onap.so.db.catalog.beans.HeatEnvironment;
import org.onap.so.db.catalog.beans.HeatFiles;
import org.onap.so.db.catalog.beans.HeatTemplate;
import org.onap.so.db.catalog.beans.VfModule;
import org.onap.so.db.catalog.beans.VfModuleCustomization;

public class VfModuleCustomizationToVduMapperTest {

    private VfModuleCustomizationToVduMapper vfModuleCustomizationToVduMapper = new VfModuleCustomizationToVduMapper();

    @Test
    public void mapVfModuleCustomizationToVdu_successful() {
        // GIVEN
        VfModuleCustomization vfModuleCustomization = createVfModuleCustomization();
        vfModuleCustomization.setHeatEnvironment(createHeatEnvironment("heatEnvName", "heatEnvContent"));
        VfModule vfModule = createVfModule();
        vfModule.setModuleHeatTemplate(createHeatTemplate(
            "moduleHeatTemplateName", "moduleHeatTemplateBody",
            "nestedTemplateName", "nestedTemplateBody"));
        vfModuleCustomization.setVfModule(vfModule);

        // WHEN
        VduModelInfo vduModelInfo = vfModuleCustomizationToVduMapper
            .mapVfModuleCustomizationToVdu(vfModuleCustomization);

        // THEN
        assertThat(vduModelInfo.getModelCustomizationUUID()).isEqualTo("modelCustomizationUUID");
        assertThat(vduModelInfo.getTimeoutMinutes()).isEqualTo(66);
        assertThat(vduModelInfo.getArtifacts()).containsExactlyElementsOf(createExpectedVduArtifacts());
    }

    @Test
    public void mapVfModuleCustVolumeToVdu_successful() {
        // GIVEN
        VfModuleCustomization vfModuleCustomization = createVfModuleCustomization();
        vfModuleCustomization.setVolumeHeatEnv(createHeatEnvironment("heatEnvName", "heatEnvContent"));
        VfModule vfModule = createVfModule();
        vfModule.setVolumeHeatTemplate(createHeatTemplate(
            "moduleHeatTemplateName", "moduleHeatTemplateBody",
            "nestedTemplateName", "nestedTemplateBody"));
        vfModuleCustomization.setVfModule(vfModule);

        // WHEN
        VduModelInfo vduModelInfo = vfModuleCustomizationToVduMapper
            .mapVfModuleCustVolumeToVdu(vfModuleCustomization);

        // THEN
        assertThat(vduModelInfo.getModelCustomizationUUID()).isEqualTo("modelCustomizationUUID");
        assertThat(vduModelInfo.getTimeoutMinutes()).isEqualTo(66);
        assertThat(vduModelInfo.getArtifacts()).containsExactlyElementsOf(createExpectedVduArtifacts());
    }

    private VfModuleCustomization createVfModuleCustomization() {
        VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
        vfModuleCustomization.setModelCustomizationUUID("modelCustomizationUUID");
        return vfModuleCustomization;
    }

    private HeatEnvironment createHeatEnvironment(String volHeatEnvName, String volHeatEnvContent) {
        HeatEnvironment heatEnvironment = new HeatEnvironment();
        heatEnvironment.setName(volHeatEnvName);
        heatEnvironment.setEnvironment(volHeatEnvContent);
        return heatEnvironment;
    }

    private VfModule createVfModule() {
        VfModule vfModule = new VfModule();
        vfModule.setModelInvariantUUID("vfModuleModelInvariantUUID");
        vfModule.setHeatFiles(createHeatFiles("cloudFileName", "cloudFileBody"));
        return vfModule;
    }

    private List<HeatFiles> createHeatFiles(String fileName, String fileBody) {
        HeatFiles heatFiles = new HeatFiles();
        heatFiles.setFileName(fileName);
        heatFiles.setFileBody(fileBody);
        return Lists.newArrayList(heatFiles);
    }

    private HeatTemplate createHeatTemplate(String moduleHeatTemplateName, String moduleHeatTemplateBody,
        String childTemplateName, String childTemplateBody) {
        HeatTemplate heatTemplate = new HeatTemplate();
        heatTemplate.setTemplateName(moduleHeatTemplateName);
        heatTemplate.setTemplateBody(moduleHeatTemplateBody);
        heatTemplate.setTimeoutMinutes(66);
        heatTemplate.setChildTemplates(createChildHeatTemplate(childTemplateName, childTemplateBody));
        return heatTemplate;
    }

    private List<HeatTemplate> createChildHeatTemplate(String moduleHeatTemplateName, String moduleHeatTemplateBody) {
        HeatTemplate heatTemplate = new HeatTemplate();
        heatTemplate.setTemplateName(moduleHeatTemplateName);
        heatTemplate.setTemplateBody(moduleHeatTemplateBody);
        return Lists.newArrayList(heatTemplate);
    }

    private List<VduArtifact> createExpectedVduArtifacts() {
        return Lists.newArrayList(
            new VduArtifact("moduleHeatTemplateName", "moduleHeatTemplateBody".getBytes(), ArtifactType.MAIN_TEMPLATE),
            new VduArtifact("nestedTemplateName", "nestedTemplateBody".getBytes(), ArtifactType.NESTED_TEMPLATE),
            new VduArtifact("cloudFileName", "cloudFileBody".getBytes(), ArtifactType.TEXT_FILE),
            new VduArtifact("heatEnvName", "heatEnvContent".getBytes(), ArtifactType.ENVIRONMENT));
    }
}
