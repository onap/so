package org.openecomp.mso.adapters.vdu.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.mso.adapters.vdu.VduArtifact;
import org.openecomp.mso.adapters.vdu.VduArtifact.ArtifactType;
import org.openecomp.mso.adapters.vdu.VduModelInfo;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.HeatEnvironment;
import org.openecomp.mso.db.catalog.beans.HeatFiles;
import org.openecomp.mso.db.catalog.beans.HeatTemplate;
import org.openecomp.mso.db.catalog.beans.VfModule;
import org.openecomp.mso.db.catalog.beans.VfModuleCustomization;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CatalogDatabase.class})
public class VfModuleCustomizationToVduMapperTest {

    private static final String HEAT_TEMPLATE_ARTIFACT_UUID = "testHeatTemplate";
    private static final String VF_MODULE_MODEL_UUID = "vfModuleUuid";
    private static final String HEAT_ENV_ARTIFACT_UUID = "heatEnvArtifactUuid";
    private static final String MAIN_TEMPLATE_NAME = "testTempName";
    private static final String MAIN_TEMPLATE_BODY = "testTempBody";
    private static final String NESTED_TEMPLATE_KEY = "testKey";
    private static final String NESTED_TEMPLATE_VALUE = "nestedTemplateTest";
    private static final String HEAT_FILE_NAME = "heatFileName";
    private static final String HEAT_FILE_BODY = "heatFileBodyTest";
    private static final String HEAT_ENV_NAME = "heatEnvName";
    private static final String HEAT_ENV = "heatEnv";
    private static final String VOL_HEAT_TEMPLATE_ARTIFACT = "volEnvArtifact";
    private static final String VOL_ENV_ARTIFACT_UUID = "volEnvArtifactUuid";

    private VfModuleCustomizationToVduMapper testedObject;
    private CatalogDatabase catalogDatabaseMock;

    @Before
    public void init() {
        PowerMockito.mockStatic(CatalogDatabase.class);
        catalogDatabaseMock = mock(CatalogDatabase.class);
        testedObject = new VfModuleCustomizationToVduMapper();
        PowerMockito.when(CatalogDatabase.getInstance()).thenReturn(catalogDatabaseMock);
        when(catalogDatabaseMock.getNestedTemplates(HEAT_TEMPLATE_ARTIFACT_UUID)).thenReturn(createNestedTemplates());
        when(catalogDatabaseMock.getHeatFilesForVfModule(VF_MODULE_MODEL_UUID)).thenReturn(createHeatFileMap());
    }

    @Test
    public void mapVfModuleCustomizationToVdu_successful() {
        when(catalogDatabaseMock.getHeatTemplateByArtifactUuid(HEAT_TEMPLATE_ARTIFACT_UUID))
                .thenReturn(createHeatTemplate());
        when(catalogDatabaseMock.getHeatEnvironmentByArtifactUuid(HEAT_ENV_ARTIFACT_UUID))
                .thenReturn(createHeatEnvironment());
        VduModelInfo result = testedObject.mapVfModuleCustomizationToVdu(createVfModuleCustomization());
        assertThat(result.getArtifacts()).containsExactly(createExpectedResultArray());
    }

    @Test
    public void mapVfModuleCustVolumeToVdu_successful() {
        when(catalogDatabaseMock.getHeatTemplateByArtifactUuid(VOL_HEAT_TEMPLATE_ARTIFACT))
                .thenReturn(createHeatTemplate());
        when(catalogDatabaseMock.getHeatEnvironmentByArtifactUuid(VOL_ENV_ARTIFACT_UUID))
                .thenReturn(createHeatEnvironment());
        VduModelInfo result = testedObject.mapVfModuleCustVolumeToVdu(createVfModuleCustomization());
        assertThat(result.getArtifacts()).containsExactly(createExpectedResultArray());
    }

    private VfModuleCustomization createVfModuleCustomization() {
        VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
        VfModule vfModule = new VfModule();
        vfModule.setHeatTemplateArtifactUUId(HEAT_TEMPLATE_ARTIFACT_UUID);
        vfModule.setVolHeatTemplateArtifactUUId(VOL_HEAT_TEMPLATE_ARTIFACT);
        vfModuleCustomization.setVfModule(vfModule);
        vfModuleCustomization.setVfModuleModelUuid(VF_MODULE_MODEL_UUID);
        vfModuleCustomization.setHeatEnvironmentArtifactUuid(HEAT_ENV_ARTIFACT_UUID);
        vfModuleCustomization.setVolEnvironmentArtifactUuid(VOL_ENV_ARTIFACT_UUID);
        return vfModuleCustomization;
    }

    private HeatTemplate createHeatTemplate() {
        HeatTemplate heatTemplate = new HeatTemplate();
        heatTemplate.setTemplateName(MAIN_TEMPLATE_NAME);
        heatTemplate.setTemplateBody(MAIN_TEMPLATE_BODY);
        heatTemplate.setArtifactUuid(HEAT_TEMPLATE_ARTIFACT_UUID);
        return heatTemplate;
    }

    private Map<String, Object> createNestedTemplates() {
        Map<String, Object> map = new HashMap<>();
        map.put(NESTED_TEMPLATE_KEY, NESTED_TEMPLATE_VALUE);
        return map;
    }

    private Map<String, HeatFiles> createHeatFileMap() {
        HeatFiles heatFiles = new HeatFiles();
        heatFiles.setFileName(HEAT_FILE_NAME);
        heatFiles.setFileBody(HEAT_FILE_BODY);
        Map<String, HeatFiles> map = new HashMap<>();
        map.put("heatFileKey", heatFiles);
        return map;
    }

    private HeatEnvironment createHeatEnvironment() {
        HeatEnvironment heatEnvironment = new HeatEnvironment();
        heatEnvironment.setName(HEAT_ENV_NAME);
        heatEnvironment.setEnvironment(HEAT_ENV);
        return heatEnvironment;
    }


    private VduArtifact[] createExpectedResultArray() {
        VduArtifact[] vduArtifactsArray = new VduArtifact[4];
        vduArtifactsArray[0] = new VduArtifact(MAIN_TEMPLATE_NAME, MAIN_TEMPLATE_BODY.getBytes(),
                ArtifactType.MAIN_TEMPLATE);
        vduArtifactsArray[1] = new VduArtifact(NESTED_TEMPLATE_KEY, NESTED_TEMPLATE_VALUE.getBytes(),
                ArtifactType.NESTED_TEMPLATE);
        vduArtifactsArray[2] = createVduArtifact(HEAT_FILE_NAME, HEAT_FILE_BODY, ArtifactType.TEXT_FILE);
        vduArtifactsArray[3] = createVduArtifact(HEAT_ENV_NAME, HEAT_ENV, ArtifactType.ENVIRONMENT);
        return vduArtifactsArray;
    }

    private VduArtifact createVduArtifact(String name, String content, ArtifactType artifactType) {
        VduArtifact vduArtifact = new VduArtifact();
        vduArtifact.setName(name);
        vduArtifact.setContent(content.getBytes());
        vduArtifact.setType(artifactType);
        return vduArtifact;
    }

}
