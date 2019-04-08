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

    private static final String MODEL_CUSTOMIZATION_UUID = "modelCustomizationUUID";
    private static final String HEAT_ENV_NAME = "heatEnvName";
    private static final String HEAT_ENV_CONTENT = "heatEnvContent";
    private static final String MODULE_HEAT_TEMPLATE_NAME = "moduleHeatTemplateName";
    private static final String MODULE_HEAT_TEMPLATE_BODY = "moduleHeatTemplateBody";
    private static final String NESTED_TEMPLATE_NAME = "nestedTemplateName";
    private static final String NESTED_TEMPLATE_BODY = "nestedTemplateBody";
    private static final int TIMEOUT_IN_MIN = 66;
    private static final String VF_MODULE_MODEL_INVARIANT_UUID = "vfModuleModelInvariantUUID";
    private static final String CLOUD_FILE_NAME = "cloudFileName";
    private static final String CLOUD_FILE_BODY = "cloudFileBody";


    @Test
    public void mapVfModuleCustomizationToVdu_successful() {
        // GIVEN
        VfModuleCustomization vfModuleCustomization = createVfModuleCustomization();
        vfModuleCustomization.setHeatEnvironment(createHeatEnvironment(HEAT_ENV_NAME, HEAT_ENV_CONTENT));
        VfModule vfModule = createVfModule();
        vfModule.setModuleHeatTemplate(createHeatTemplate(MODULE_HEAT_TEMPLATE_NAME, MODULE_HEAT_TEMPLATE_BODY,
                NESTED_TEMPLATE_NAME, NESTED_TEMPLATE_BODY));
        vfModuleCustomization.setVfModule(vfModule);

        // WHEN
        VduModelInfo vduModelInfo =
                new VfModuleCustomizationToVduMapper().mapVfModuleCustomizationToVdu(vfModuleCustomization);

        // THEN
        assertThat(vduModelInfo.getModelCustomizationUUID()).isEqualTo(MODEL_CUSTOMIZATION_UUID);
        assertThat(vduModelInfo.getTimeoutMinutes()).isEqualTo(TIMEOUT_IN_MIN);
        assertThat(vduModelInfo.getArtifacts()).containsExactlyElementsOf(createExpectedVduArtifacts());
    }

    @Test
    public void mapVfModuleCustVolumeToVdu_successful() {
        // GIVEN
        VfModuleCustomization vfModuleCustomization = createVfModuleCustomization();
        vfModuleCustomization.setVolumeHeatEnv(createHeatEnvironment(HEAT_ENV_NAME, HEAT_ENV_CONTENT));
        VfModule vfModule = createVfModule();
        vfModule.setVolumeHeatTemplate(createHeatTemplate(MODULE_HEAT_TEMPLATE_NAME, MODULE_HEAT_TEMPLATE_BODY,
                NESTED_TEMPLATE_NAME, NESTED_TEMPLATE_BODY));
        vfModuleCustomization.setVfModule(vfModule);

        // WHEN
        VduModelInfo vduModelInfo =
                new VfModuleCustomizationToVduMapper().mapVfModuleCustVolumeToVdu(vfModuleCustomization);

        // THEN
        assertThat(vduModelInfo.getModelCustomizationUUID()).isEqualTo(MODEL_CUSTOMIZATION_UUID);
        assertThat(vduModelInfo.getTimeoutMinutes()).isEqualTo(TIMEOUT_IN_MIN);
        assertThat(vduModelInfo.getArtifacts()).containsExactlyElementsOf(createExpectedVduArtifacts());
    }

    private VfModuleCustomization createVfModuleCustomization() {
        VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
        vfModuleCustomization.setModelCustomizationUUID(MODEL_CUSTOMIZATION_UUID);
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
        vfModule.setModelInvariantUUID(VF_MODULE_MODEL_INVARIANT_UUID);
        vfModule.setHeatFiles(createHeatFiles(CLOUD_FILE_NAME, CLOUD_FILE_BODY));
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
        heatTemplate.setTimeoutMinutes(TIMEOUT_IN_MIN);
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
                new VduArtifact(MODULE_HEAT_TEMPLATE_NAME, MODULE_HEAT_TEMPLATE_BODY.getBytes(),
                        ArtifactType.MAIN_TEMPLATE),
                new VduArtifact(NESTED_TEMPLATE_NAME, NESTED_TEMPLATE_BODY.getBytes(), ArtifactType.NESTED_TEMPLATE),
                new VduArtifact(CLOUD_FILE_NAME, CLOUD_FILE_BODY.getBytes(), ArtifactType.TEXT_FILE),
                new VduArtifact(HEAT_ENV_NAME, HEAT_ENV_CONTENT.getBytes(), ArtifactType.ENVIRONMENT));
    }
}
