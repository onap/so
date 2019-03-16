/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.onap.so.asdc.client;

import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityNotFoundException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.onap.so.asdc.BaseTest;
import org.onap.so.asdc.client.exceptions.ASDCControllerException;
import org.onap.so.asdc.client.test.emulators.ArtifactInfoImpl;
import org.onap.so.asdc.client.test.emulators.DistributionClientEmulator;
import org.onap.so.asdc.client.test.emulators.NotificationDataImpl;
import org.onap.so.asdc.client.test.emulators.ResourceInfoImpl;
import org.onap.so.db.catalog.beans.PnfResource;
import org.onap.so.db.catalog.beans.PnfResourceCustomization;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.ToscaCsar;
import org.onap.so.db.catalog.beans.VnfResource;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.data.repository.PnfCustomizationRepository;
import org.onap.so.db.catalog.data.repository.PnfResourceRepository;
import org.onap.so.db.catalog.data.repository.ServiceRepository;
import org.onap.so.db.catalog.data.repository.ToscaCsarRepository;
import org.onap.so.db.catalog.data.repository.VnfCustomizationRepository;
import org.onap.so.db.catalog.data.repository.VnfResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

/**
 * This is used to run some basic integration test(BIT) for ASDC controller. It will test the csar install and all the
 * testing csar files are located underneath src/main/resources/download folder,
 *
 * PNF csar: service-Testservice140-csar.csar VNF csar: service-Svc140-VF-csar.csar
 */
public class ASDCControllerITTest extends BaseTest {

    private Logger logger = LoggerFactory.getLogger(ASDCControllerITTest.class);

    @Rule
    public TestName testName = new TestName();

    private String serviceUuid;
    private String serviceInvariantUuid;

    /**
     * Random UUID served as distribution UUID.
     */
    private String distributionId;
    private String artifactUuid;

    @Autowired
    private ASDCController asdcController;

    @Autowired
    private PnfResourceRepository pnfResourceRepository;

    @Autowired
    private PnfCustomizationRepository pnfCustomizationRepository;

    @Autowired
    private VnfResourceRepository vnfResourceRepository;

    @Autowired
    private VnfCustomizationRepository vnfCustomizationRepository;

    @Autowired
    private ToscaCsarRepository toscaCsarRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    private DistributionClientEmulator distributionClient;

    @Before
    public void setUp() {
        distributionId = UUID.randomUUID().toString();
        artifactUuid = UUID.randomUUID().toString();
        logger.info("Using distributionId: {}, artifactUUID: {} for testcase: {}", distributionId, artifactUuid,
            testName.getMethodName());

        distributionClient = new DistributionClientEmulator();
        distributionClient.setResourcePath("src/test/resources");
        asdcController.setDistributionClient(distributionClient);
        try {
            asdcController.initASDC();
        } catch (ASDCControllerException e) {
            logger.error(e.getMessage(), e);
            fail(e.getMessage());
        }
    }

    @After
    public void shutDown() {
        try {
            asdcController.closeASDC();
        } catch (ASDCControllerException e) {
            logger.error(e.getMessage(), e);
            fail(e.getMessage());
        }
    }

    /**
     * Mock the AAI using wireshark.
     */
    private void initMockAaiServer(final String serviceUuid, final String serviceInvariantUuid) {
        String modelEndpoint = "/aai/v15/service-design-and-creation/models/model/" + serviceInvariantUuid
            + "/model-vers/model-ver/" + serviceUuid + "?depth=0";

        stubFor(post(urlEqualTo(modelEndpoint)).willReturn(ok()));
    }

    /**
     * Test with service-Testservice140-csar.csar.
     */
    @Test
    public void treatNotification_ValidPnfResource_ExpectedOutput() {

        /**
         * service UUID/invariantUUID from global metadata in service-Testservice140-template.yml.
         */
        String serviceUuid = "efaea486-561f-4159-9191-a8d3cb346728";
        String serviceInvariantUuid = "f2edfbf4-bb0a-4fe7-a57a-71362d4b0b23";

        initMockAaiServer(serviceUuid, serviceInvariantUuid);

        NotificationDataImpl notificationData = new NotificationDataImpl();
        notificationData.setServiceUUID(serviceUuid);
        notificationData.setDistributionID(distributionId);
        notificationData.setServiceInvariantUUID(serviceInvariantUuid);
        notificationData.setServiceVersion("1.0");

        ResourceInfoImpl resourceInfo = constructPnfResourceInfo();
        List<ResourceInfoImpl> resourceInfoList = new ArrayList<>();
        resourceInfoList.add(resourceInfo);
        notificationData.setResources(resourceInfoList);

        ArtifactInfoImpl artifactInfo = constructPnfServiceArtifact();
        List<ArtifactInfoImpl> artifactInfoList = new ArrayList<>();
        artifactInfoList.add(artifactInfo);
        notificationData.setServiceArtifacts(artifactInfoList);

        try {
            asdcController.treatNotification(notificationData);
            logger.info("Checking the database for PNF ingestion");

            /**
             * Check the tosca csar entity, it should be the same as provided from NotficationData.
             */
            ToscaCsar toscaCsar = toscaCsarRepository.findById(artifactUuid)
                .orElseThrow(() -> new EntityNotFoundException("Tosca csar: " + artifactUuid + " not found"));
            assertEquals("tosca csar UUID", artifactUuid, toscaCsar.getArtifactUUID());
            assertEquals("tosca csar name", "service-Testservice140-csar.csar", toscaCsar.getName());
            assertEquals("tosca csar version", "1.0", toscaCsar.getVersion());
            assertNull("tosca csar descrption", toscaCsar.getDescription());
            assertEquals("tosca csar checksum", "MANUAL_RECORD", toscaCsar.getArtifactChecksum());
            assertEquals("toscar csar URL", "/download/service-Testservice140-csar.csar", toscaCsar.getUrl());

            /**
             * Check the service entity, it should be the same as global metadata information in service-Testservice140-template.yml inside csar.
             */
            Service service = serviceRepository.findById(serviceUuid)
                .orElseThrow(() -> new EntityNotFoundException("Service: " + serviceUuid + " not found"));
            assertEquals("model UUID", "efaea486-561f-4159-9191-a8d3cb346728", service.getModelUUID());
            assertEquals("model name", "TestService140", service.getModelName());
            assertEquals("model invariantUUID", "f2edfbf4-bb0a-4fe7-a57a-71362d4b0b23",
                service.getModelInvariantUUID());
            assertEquals("model version", "1.0", service.getModelVersion());
            assertEquals("description", "Test Service for extended attributes of PNF resource",
                service.getDescription().trim());
            assertEquals("tosca csar artifact UUID", artifactUuid, service.getCsar().getArtifactUUID());
            assertEquals("service type", "Network", service.getServiceType());
            assertEquals("service role", "nfv", service.getServiceRole());
            assertEquals("environment context", "General_Revenue-Bearing", service.getEnvironmentContext());
            assertEquals("service category", "Network Service", service.getCategory());
            assertNull("workload context", service.getWorkloadContext());
            assertEquals("resource order", "Test140PNF", service.getResourceOrder());

            /**
             * Check PNF resource, it should be the same as metadata in the topology template in service-Testservice140-template.yml
             * OR
             * global metadata in the resource-Test140pnf-template.yml
             */
            String pnfResourceKey = "9c54e269-122b-4e8a-8b2a-6eac849b441a";
            PnfResource pnfResource = pnfResourceRepository.findById(pnfResourceKey)
                .orElseThrow(() -> new EntityNotFoundException("PNF resource:" + pnfResourceKey + " not found"));
            assertNull("orchestration mode", pnfResource.getOrchestrationMode());
            assertEquals("Description", "Oracle", pnfResource.getDescription().trim());
            assertEquals("model UUID", pnfResourceKey, pnfResource.getModelUUID());
            assertEquals("model invariant UUID", "d832a027-75f3-455d-9de4-f02fcdee7e7e",
                pnfResource.getModelInvariantUUID());
            assertEquals("model version", "1.0", pnfResource.getModelVersion());
            assertEquals("model name", "Test140PNF", pnfResource.getModelName());
            assertEquals("tosca node type", "org.openecomp.resource.pnf.Test140pnf", pnfResource.getToscaNodeType());
            assertEquals("resource category", "Application L4+", pnfResource.getCategory());
            assertEquals("resource sub category", "Call Control", pnfResource.getSubCategory());

            /**
             * Check PNF resource customization, it should be the same as metadata in the topology template in service-Testservice140-template.yml
             * OR
             * global metadata in the resource-Test140pnf-template.yml
             */
            String pnfCustomizationKey = "428a3d73-f962-4cc2-ba62-2483c45d6b12";
            PnfResourceCustomization pnfCustomization = pnfCustomizationRepository
                .findById(pnfCustomizationKey).orElseThrow(
                    () -> new EntityNotFoundException(
                        "PNF resource customization: " + pnfCustomizationKey + " not found"));
            assertEquals("model customizationUUID", pnfCustomizationKey, pnfCustomization.getModelCustomizationUUID());
            assertEquals("model instance name", "Test140PNF 0", pnfCustomization.getModelInstanceName());
            assertEquals("NF type", "", pnfCustomization.getNfType());
            assertEquals("NF Role", "nf", pnfCustomization.getNfRole());
            assertEquals("NF function", "nf", pnfCustomization.getNfFunction());
            assertEquals("NF naming code", "", pnfCustomization.getNfNamingCode());
            assertEquals("PNF resource model UUID", pnfResourceKey, pnfCustomization.getPnfResources().getModelUUID());
            assertEquals("Multi stage design", "", pnfCustomization.getMultiStageDesign());
            assertNull("resource input", pnfCustomization.getResourceInput());
            assertEquals("cds blueprint name(sdnc_model_name property)", pnfCustomization.getBlueprintName(),
                pnfCustomization.getBlueprintName());
            assertEquals("cds blueprint version(sdnc_model_version property)", pnfCustomization.getBlueprintVersion(),
                pnfCustomization.getBlueprintVersion());
            /**
             * Check the pnf resource customization with service mapping
             */
            List<PnfResourceCustomization> pnfCustFromJoinTable = pnfCustomizationRepository
                .findPnfResourceCustomizationFromJoinTable("efaea486-561f-4159-9191-a8d3cb346728");
            assertEquals("PNF resource customization entity from Join table", 1, pnfCustFromJoinTable.size());
            assertEquals(pnfCustomizationKey, pnfCustFromJoinTable.get(0).getModelCustomizationUUID());
        } catch (Exception e) {
            logger.info(e.getMessage(), e);
            fail(e.getMessage());
        }
    }

    private ArtifactInfoImpl constructPnfServiceArtifact() {
        ArtifactInfoImpl artifactInfo = new ArtifactInfoImpl();
        artifactInfo.setArtifactType(ASDCConfiguration.TOSCA_CSAR);
        artifactInfo.setArtifactURL("/download/service-Testservice140-csar.csar");
        artifactInfo.setArtifactName("service-Testservice140-csar.csar");
        artifactInfo.setArtifactVersion("1.0");
        artifactInfo.setArtifactUUID(artifactUuid);
        return artifactInfo;
    }

    /**
     * Construct the PnfResourceInfo based on the resource-Test140Pnf-template.yml from
     * service-Testservice140-csar.csar.
     */
    private ResourceInfoImpl constructPnfResourceInfo() {
        ResourceInfoImpl resourceInfo = new ResourceInfoImpl();
        resourceInfo.setResourceInstanceName("Test140PNF");
        resourceInfo.setResourceInvariantUUID("d832a027-75f3-455d-9de4-f02fcdee7e7e");
        resourceInfo.setResoucreType("PNF");
        resourceInfo.setCategory("Application L4+");
        resourceInfo.setSubcategory("Call Control");
        resourceInfo.setResourceUUID("9c54e269-122b-4e8a-8b2a-6eac849b441a");
        resourceInfo.setResourceCustomizationUUID("428a3d73-f962-4cc2-ba62-2483c45d6b12");
        resourceInfo.setResourceVersion("1.0");
        return resourceInfo;
    }

    /**
     * Testing with the service-Svc140-VF-csar.csar.
     */
    @Test
    public void treatNotification_ValidVnfResource_ExpectedOutput() {

        /**
         * service UUID/invariantUUID from global metadata in resource-Testvf140-template.yml.
         */
        String serviceUuid = "28944a37-de3f-46ec-9c60-b57036fbd26d";
        String serviceInvariantUuid = "9e900d3e-1e2e-4124-a5c2-4345734dc9de";

        initMockAaiServer(serviceUuid, serviceInvariantUuid);

        NotificationDataImpl notificationData = new NotificationDataImpl();
        notificationData.setServiceUUID(serviceUuid);
        notificationData.setDistributionID(distributionId);
        notificationData.setServiceInvariantUUID(serviceInvariantUuid);
        notificationData.setServiceVersion("1.0");

        ResourceInfoImpl resourceInfo = constructVnfResourceInfo();
        List<ResourceInfoImpl> resourceInfoList = new ArrayList<>();
        resourceInfoList.add(resourceInfo);
        notificationData.setResources(resourceInfoList);

        ArtifactInfoImpl artifactInfo = constructVnfServiceArtifact();
        List<ArtifactInfoImpl> artifactInfoList = new ArrayList<>();
        artifactInfoList.add(artifactInfo);
        notificationData.setServiceArtifacts(artifactInfoList);

        try {
            asdcController.treatNotification(notificationData);
            logger.info("Checking the database for VNF ingestion");

            /**
             * Check the tosca csar entity, it should be the same as provided from NotficationData.
             */
            ToscaCsar toscaCsar = toscaCsarRepository.findById(artifactUuid)
                .orElseThrow(() -> new EntityNotFoundException("Tosca csar: " + artifactUuid + " not found"));
            assertEquals("tosca csar UUID", artifactUuid, toscaCsar.getArtifactUUID());
            assertEquals("tosca csar name", "service-Svc140-VF-csar.csar", toscaCsar.getName());
            assertEquals("tosca csar version", "1.0", toscaCsar.getVersion());
            assertNull("tosca csar descrption", toscaCsar.getDescription());
            assertEquals("tosca csar checksum", "MANUAL_RECORD", toscaCsar.getArtifactChecksum());
            assertEquals("toscar csar URL", "/download/service-Svc140-VF-csar.csar", toscaCsar.getUrl());

            /**
             * Check the service entity, it should be the same as global metadata information in service-Testservice140-template.yml inside csar.
             */
            Service service = serviceRepository.findById(serviceUuid)
                .orElseThrow(() -> new EntityNotFoundException("Service: " + serviceUuid + " not found"));
            assertEquals("model UUID", serviceUuid, service.getModelUUID());
            assertEquals("model name", "SVC140", service.getModelName());
            assertEquals("model invariantUUID", serviceInvariantUuid,
                service.getModelInvariantUUID());
            assertEquals("model version", "1.0", service.getModelVersion());
            assertEquals("description", "SVC140",
                service.getDescription().trim());
            assertEquals("tosca csar artifact UUID", artifactUuid, service.getCsar().getArtifactUUID());
            assertEquals("service type", "ST", service.getServiceType());
            assertEquals("service role", "Sr", service.getServiceRole());
            assertEquals("environment context", "General_Revenue-Bearing", service.getEnvironmentContext());
            assertEquals("service category", "Network Service", service.getCategory());
            assertNull("workload context", service.getWorkloadContext());
            assertEquals("resource order", "TestVF140", service.getResourceOrder());

            /**
             * Check VNF resource, it should be the same as metadata in the topology template in service-Testservice140-template.yml
             * OR
             * global metadata in the resource-Testservice140-template.yml
             */
            String vnfResourceKey = "d20d3ea9-2f54-4071-8b5c-fd746dde245e";
            VnfResource vnfResource = vnfResourceRepository.findById(vnfResourceKey)
                .orElseThrow(() -> new EntityNotFoundException("VNF resource:" + vnfResourceKey + " not found"));
            assertEquals("orchestration mode", "HEAT", vnfResource.getOrchestrationMode());
            assertEquals("Description", "TestPNF140", vnfResource.getDescription().trim());
            assertEquals("model UUID", vnfResourceKey, vnfResource.getModelUUID());
            assertEquals("model invariant UUID", "7a4bffa2-fac5-4b8b-b348-0bdf313a1aeb",
                vnfResource.getModelInvariantUUID());
            assertEquals("model version", "1.0", vnfResource.getModelVersion());
            assertEquals("model name", "TestVF140", vnfResource.getModelName());
            assertEquals("tosca node type", "org.openecomp.resource.vf.Testvf140", vnfResource.getToscaNodeType());
            assertEquals("resource category", "Application L4+", vnfResource.getCategory());
            assertEquals("resource sub category", "Database", vnfResource.getSubCategory());

            /**
             * Check VNF resource customization, it should be the same as metadata in the topology template in service-Testservice140-template.yml
             * OR
             * global metadata in the resource-Testservice140-template.yml
             */
            String vnfCustomizationKey = "ca1c8455-8ce2-4a76-a037-3f4cf01cffa0";
            VnfResourceCustomization vnfCustomization = vnfCustomizationRepository
                .findById(vnfCustomizationKey).orElseThrow(
                    () -> new EntityNotFoundException(
                        "VNF resource customization: " + vnfCustomizationKey + " not found"));
            assertEquals("model customizationUUID", vnfCustomizationKey, vnfCustomization.getModelCustomizationUUID());
            assertEquals("model instance name", "TestVF140 0", vnfCustomization.getModelInstanceName());
            assertNull("NF type", vnfCustomization.getNfType());
            assertNull("NF Role", vnfCustomization.getNfRole());
            assertNull("NF function", vnfCustomization.getNfFunction());
            assertNull("NF naming code", vnfCustomization.getNfNamingCode());
            assertEquals("VNF resource model UUID", vnfResourceKey, vnfCustomization.getVnfResources().getModelUUID());
            assertEquals("Multi stage design", "false", vnfCustomization.getMultiStageDesign());
            assertNull("resource input", vnfCustomization.getResourceInput());
            assertEquals("cds blueprint name(sdnc_model_name property)", vnfCustomization.getBlueprintName(),
                vnfCustomization.getBlueprintName());
            assertEquals("cds blueprint version(sdnc_model_version property)", vnfCustomization.getBlueprintVersion(),
                vnfCustomization.getBlueprintVersion());
            /**
             * Check the vnf resource customization with service mapping
             */
            List<VnfResourceCustomization> vnfCustFromJoinTable = vnfCustomizationRepository
                .findVnfResourceCustomizationFromJoinTable(serviceUuid);
            assertEquals("VNF resource customization entity from Join table", 1, vnfCustFromJoinTable.size());
            assertEquals(vnfCustomizationKey, vnfCustFromJoinTable.get(0).getModelCustomizationUUID());
        } catch (Exception e) {
            logger.info(e.getMessage(), e);
            fail(e.getMessage());
        }
    }

    private ArtifactInfoImpl constructVnfServiceArtifact() {
        ArtifactInfoImpl artifactInfo = new ArtifactInfoImpl();
        artifactInfo.setArtifactType(ASDCConfiguration.TOSCA_CSAR);
        artifactInfo.setArtifactURL("/download/service-Svc140-VF-csar.csar");
        artifactInfo.setArtifactName("service-Svc140-VF-csar.csar");
        artifactInfo.setArtifactVersion("1.0");
        artifactInfo.setArtifactUUID(artifactUuid);
        return artifactInfo;
    }

    /**
     * Construct the PnfResourceInfo based on the resource-Testvf140-template.yml from service-Svc140-VF-csar.csar.
     */
    private ResourceInfoImpl constructVnfResourceInfo() {
        ResourceInfoImpl resourceInfo = new ResourceInfoImpl();
        resourceInfo.setResourceInstanceName("TestVF140");
        resourceInfo.setResourceInvariantUUID("7a4bffa2-fac5-4b8b-b348-0bdf313a1aeb");
        resourceInfo.setResoucreType("VF");
        resourceInfo.setCategory("Application L4+");
        resourceInfo.setSubcategory("Database");
        resourceInfo.setResourceUUID("d20d3ea9-2f54-4071-8b5c-fd746dde245e");
        resourceInfo.setResourceCustomizationUUID("ca1c8455-8ce2-4a76-a037-3f4cf01cffa0");
        resourceInfo.setResourceVersion("1.0");
        resourceInfo.setArtifacts(Collections.EMPTY_LIST);
        return resourceInfo;
    }
}
