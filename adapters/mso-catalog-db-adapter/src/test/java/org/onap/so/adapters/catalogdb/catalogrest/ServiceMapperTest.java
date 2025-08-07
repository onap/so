/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.adapters.catalogdb.catalogrest;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.onap.so.adapters.catalogdb.rest.ServiceMapper;
import org.onap.so.db.catalog.beans.HeatTemplate;
import org.onap.so.db.catalog.beans.HeatTemplateParam;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.rest.catalog.beans.Service;
import wiremock.com.fasterxml.jackson.core.JsonParseException;
import wiremock.com.fasterxml.jackson.databind.JsonMappingException;
import wiremock.com.fasterxml.jackson.databind.ObjectMapper;

public class ServiceMapperTest {

    private ServiceMapper serviceMapper = new ServiceMapper();

    @Test
    public void service_map_test() throws JsonParseException, JsonMappingException, IOException {
        Service actual = serviceMapper.mapService(getTestService(), 2);
        assertThat(actual, sameBeanAs(getExpectedService()));
    }

    private Service getExpectedService() throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(getJson("ExpectedService.json"), Service.class);
    }


    private org.onap.so.db.catalog.beans.Service getTestService() {
        org.onap.so.db.catalog.beans.Service testService = new org.onap.so.db.catalog.beans.Service();
        testService.setCategory("category");
        testService.setDescription("description");
        testService.setDistrobutionStatus("distrobutionStatus");
        testService.setEnvironmentContext("environmentContext");
        testService.setModelInvariantUUID("modelInvariantUUID");
        testService.setModelName("modelName");
        testService.setModelUUID("modelUUID");
        testService.setModelVersion("modelVersion");
        testService.setServiceType("serviceType");
        testService.setServiceRole("serviceRole");

        testService.getVnfCustomizations().add(getTestVnfCustomization());
        return testService;
    }

    private org.onap.so.db.catalog.beans.VnfResourceCustomization getTestVnfCustomization() {
        org.onap.so.db.catalog.beans.VnfResourceCustomization test =
                new org.onap.so.db.catalog.beans.VnfResourceCustomization();
        test.setId(1);
        test.setAvailabilityZoneMaxCount(11);
        test.setMaxInstances(3);
        test.setMinInstances(1);
        test.setModelCustomizationUUID("modelCustomizationUUID");
        test.setModelInstanceName("modelInstanceName");
        test.setMultiStageDesign("multiStageDesign");
        test.setNfFunction("nfFunction");
        test.setNfNamingCode("nfNamingCode");
        test.setNfRole("nfRole");
        test.setNfType("nfType");
        test.setService(new org.onap.so.db.catalog.beans.Service());
        test.setVnfResources(getTestVnfResource());
        test.setVfModuleCustomizations(getTestVfModuleCust());
        return test;
    }

    private List<VfModuleCustomization> getTestVfModuleCust() {
        List<VfModuleCustomization> test = new ArrayList<>();
        VfModuleCustomization testVfMod = new VfModuleCustomization();
        testVfMod.setAvailabilityZoneCount(10);
        testVfMod.setInitialCount(1);
        testVfMod.setLabel("label");
        testVfMod.setMaxInstances(3);
        testVfMod.setMinInstances(1);
        testVfMod.setModelCustomizationUUID("modelCustomizationUUID");
        org.onap.so.db.catalog.beans.VfModule vfModule = new org.onap.so.db.catalog.beans.VfModule();
        vfModule.setDescription("description");
        vfModule.setIsBase(false);
        vfModule.setModelInvariantUUID("modelInvariantUUID");
        vfModule.setModelName("modelName");
        vfModule.setModelUUID("modelUUID");
        vfModule.setModelVersion("modelVersion");
        HeatTemplate moduleHeatTemplate = new HeatTemplate();
        moduleHeatTemplate.setArtifactChecksum("artifactChecksum");
        moduleHeatTemplate.setArtifactUuid("artifactUuid");
        // moduleHeatTemplate.setChildTemplates(childTemplates);
        moduleHeatTemplate.setDescription("description");
        Set<HeatTemplateParam> parameters = new HashSet<>();
        HeatTemplateParam heatParam = new HeatTemplateParam();
        heatParam.setHeatTemplateArtifactUuid("heatTemplateArtifactUuid");
        heatParam.setParamAlias("paramAlias");
        heatParam.setParamName("paramName");
        heatParam.setParamType("paramType");
        heatParam.setRequired(false);
        parameters.add(heatParam);
        moduleHeatTemplate.setParameters(parameters);
        moduleHeatTemplate.setTemplateBody("templateBody");
        moduleHeatTemplate.setTemplateName("templateName");
        moduleHeatTemplate.setTimeoutMinutes(1000);
        moduleHeatTemplate.setVersion("version");
        vfModule.setModuleHeatTemplate(moduleHeatTemplate);
        testVfMod.setVfModule(vfModule);
        test.add(testVfMod);
        return test;
    }

    private org.onap.so.db.catalog.beans.VnfResource getTestVnfResource() {
        org.onap.so.db.catalog.beans.VnfResource test = new org.onap.so.db.catalog.beans.VnfResource();
        test.setCategory("category");
        test.setDescription("description");
        test.setModelInvariantUUID("modelInvariantUUID");
        test.setModelName("modelName");
        test.setModelUUID("modelUUID");
        test.setModelVersion("modelVersion");
        test.setAicVersionMax("cloudVersionMax");
        test.setAicVersionMin("cloudVersionMin");
        test.setOrchestrationMode("orchestrationMode");
        test.setSubCategory("subCategory");
        test.setToscaNodeType("toscaNodeType");
        return test;
    }

    private String getJson(String filename) throws IOException {
        return new String(Files.readAllBytes(Paths.get("src/test/resources/" + filename)));
    }
}
