/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix
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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.EntityNotFoundException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.onap.aaiclient.client.aai.AAIVersion;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
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
import org.onap.so.db.request.beans.WatchdogComponentDistributionStatus;
import org.onap.so.db.request.data.repository.WatchdogComponentDistributionStatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * This is used to run some basic integration test(BIT) for ASDC controller. It will test the csar install and all the
 * testing csar files are located underneath src/main/resources/download folder,
 *
 * PNF csar: service-pnfservice.csar VNF csar: service-Svc140-VF-csar.csar
 *
 * All the csar files are cleaned, i.e, removing the comments and most of the description to avoid violation of
 * security.
 */
@Transactional
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

    @Autowired
    protected WatchdogComponentDistributionStatusRepository watchdogCDStatusRepository;

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
        String modelEndpoint = "/aai/" + AAIVersion.LATEST + "/service-design-and-creation/models/model/"
                + serviceInvariantUuid + "/model-vers/model-ver/" + serviceUuid + "?depth=0";

        wireMockServer.stubFor(post(urlEqualTo(modelEndpoint)).willReturn(ok()));
    }

    /**
     * Test with service-pnfservice.csar.
     */
    @Test
    public void treatNotification_ValidPnfResource_ExpectedOutput() {

        /**
         * service UUID/invariantUUID from global metadata in service-PnfServiceTestCds-template.yml.
         */
        String serviceUuid = "77cf276e-905c-43f6-8d54-dda474be2f2e";
        String serviceInvariantUuid = "913e6776-4bc3-49b9-b399-b5bb4690f0c7";

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
            assertEquals("tosca csar name", "service-pnfservice.csar", toscaCsar.getName());
            assertEquals("tosca csar version", "1.0", toscaCsar.getVersion());
            assertNull("tosca csar descrption", toscaCsar.getDescription());
            assertEquals("tosca csar checksum", "MANUAL_RECORD", toscaCsar.getArtifactChecksum());
            assertEquals("toscar csar URL", "/download/service-pnfservice.csar", toscaCsar.getUrl());

            /**
             * Check the service entity, it should be the same as global metadata information in
             * service-Testservice140-template.yml inside csar.
             */
            Service service = serviceRepository.findById(serviceUuid)
                    .orElseThrow(() -> new EntityNotFoundException("Service: " + serviceUuid + " not found"));
            assertEquals("model UUID", serviceUuid, service.getModelUUID());
            assertEquals("model name", "PNF Service Test CDS", service.getModelName());
            assertEquals("model invariantUUID", serviceInvariantUuid, service.getModelInvariantUUID());
            assertEquals("model version", "1.0", service.getModelVersion());
            assertEquals("description", "123123", service.getDescription().trim());
            assertEquals("tosca csar artifact UUID", artifactUuid, service.getCsar().getArtifactUUID());
            assertEquals("service type", "", service.getServiceType());
            assertEquals("service role", "", service.getServiceRole());
            assertEquals("environment context", "General_Revenue-Bearing", service.getEnvironmentContext());
            assertEquals("service category", "Network L1-3", service.getCategory());
            assertNull("workload context", service.getWorkloadContext());
            assertEquals("resource order", "PNF CDS Test", service.getResourceOrder());
            assertEquals("CDS blueprint name", "Blueprint140", service.getBlueprintName());
            assertEquals("CDS blueprint version", "v1.4.0", service.getBlueprintVersion());
            assertEquals("controller actor", "SO-REF-DATA", service.getControllerActor());

            /**
             * Check PNF resource, it should be the same as metadata in the topology template in
             * service-PnfServiceTestCds-template.yml OR global metadata in the resource-PnfServiceTestCds-template.yml
             */
            String pnfResourceKey = "aa5d0562-80e7-43e9-af74-3085e57ab09f";
            PnfResource pnfResource = pnfResourceRepository.findById(pnfResourceKey)
                    .orElseThrow(() -> new EntityNotFoundException("PNF resource:" + pnfResourceKey + " not found"));
            assertNull("orchestration mode", pnfResource.getOrchestrationMode());
            assertEquals("Description", "123123", pnfResource.getDescription().trim());
            assertEquals("model UUID", pnfResourceKey, pnfResource.getModelUUID());
            assertEquals("model invariant UUID", "17d9d183-cee5-4a46-b5c4-6d5203f7d2e8",
                    pnfResource.getModelInvariantUUID());
            assertEquals("model version", "1.0", pnfResource.getModelVersion());
            assertEquals("model name", "PNF CDS Test", pnfResource.getModelName());
            assertEquals("tosca node type", "org.openecomp.resource.pnf.PnfCdsTest", pnfResource.getToscaNodeType());
            assertEquals("resource category", "Application L4+", pnfResource.getCategory());
            assertEquals("resource sub category", "Firewall", pnfResource.getSubCategory());

            /**
             * Check PNF resource customization, it should be the same as metadata in the topology template in
             * service-PnfServiceTestCds-template.yml OR global metadata in the resource-PnfServiceTestCds-template.yml
             */
            String pnfCustomizationKey = "9f01263a-eaf7-4d98-a37b-3785f751903e";
            PnfResourceCustomization pnfCustomization = pnfCustomizationRepository.findById(pnfCustomizationKey)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "PNF resource customization: " + pnfCustomizationKey + " not found"));
            assertEquals("model customizationUUID", pnfCustomizationKey, pnfCustomization.getModelCustomizationUUID());
            assertEquals("model instance name", "PNF CDS Test 0", pnfCustomization.getModelInstanceName());
            assertEquals("NF type", "", pnfCustomization.getNfType());
            assertEquals("NF Role", "nf", pnfCustomization.getNfRole());
            assertEquals("NF function", "nf", pnfCustomization.getNfFunction());
            assertEquals("NF naming code", "", pnfCustomization.getNfNamingCode());
            assertEquals("PNF resource model UUID", pnfResourceKey, pnfCustomization.getPnfResources().getModelUUID());
            assertEquals("Multi stage design", "", pnfCustomization.getMultiStageDesign());
            assertNull("resource input", pnfCustomization.getResourceInput());
            assertEquals("cds blueprint name(sdnc_model_name property)", "Blueprint140",
                    pnfCustomization.getBlueprintName());
            assertEquals("cds blueprint version(sdnc_model_version property)", "v1.4.0",
                    pnfCustomization.getBlueprintVersion());
            assertTrue("skip post instantiation configuration", pnfCustomization.getSkipPostInstConf());
            assertEquals("controller actor", "SO-REF-DATA", pnfCustomization.getControllerActor());

            /**
             * Check the pnf resource customization with service mapping
             */
            List<PnfResourceCustomization> pnfCustList = service.getPnfCustomizations();
            assertEquals("PNF resource customization entity", 1, pnfCustList.size());
            assertEquals(pnfCustomizationKey, pnfCustList.get(0).getModelCustomizationUUID());

            /**
             * Check the watchdog for component distribution status
             */
            List<WatchdogComponentDistributionStatus> distributionList =
                    watchdogCDStatusRepository.findByDistributionId(this.distributionId);
            assertNotNull(distributionList);
            assertEquals(1, distributionList.size());
            WatchdogComponentDistributionStatus distributionStatus = distributionList.get(0);
            assertEquals("COMPONENT_DONE_OK", distributionStatus.getComponentDistributionStatus());
            assertEquals("SO", distributionStatus.getComponentName());


        } catch (Exception e) {
            logger.info(e.getMessage(), e);
            fail(e.getMessage());
        }
    }

    /**
     * Test to check RequestId is being set using the DistributionID.
     */
    @Test
    public void treatNotification_verifyRequestID() {

        String serviceUuid = "efaea486-561f-4159-9191-a8d3cb346728";
        String serviceInvariantUuid = "f2edfbf4-bb0a-4fe7-a57a-71362d4b0b23";
        distributionId = "bb15de12-166d-4e45-9e5f-4b3f25200d7b";

        initMockAaiServer(serviceUuid, serviceInvariantUuid);

        NotificationDataImpl notificationData = new NotificationDataImpl();
        notificationData.setServiceUUID(serviceUuid);
        notificationData.setDistributionID(distributionId);
        notificationData.setServiceInvariantUUID(serviceInvariantUuid);
        notificationData.setServiceVersion("1.0");

        try {
            asdcController.treatNotification(notificationData);
            logger.info("Verify RequestId : {}", MDC.get(ONAPLogConstants.MDCs.REQUEST_ID));
            assertEquals("bb15de12-166d-4e45-9e5f-4b3f25200d7b", MDC.get(ONAPLogConstants.MDCs.REQUEST_ID));

        } catch (Exception e) {
            logger.info(e.getMessage(), e);
            fail(e.getMessage());
        }
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
            assertEquals("tosca csar name", "service-vnfservice.csar", toscaCsar.getName());
            assertEquals("tosca csar version", "1.0", toscaCsar.getVersion());
            assertNull("tosca csar descrption", toscaCsar.getDescription());
            assertEquals("tosca csar checksum", "MANUAL_RECORD", toscaCsar.getArtifactChecksum());
            assertEquals("toscar csar URL", "/download/service-vnfservice.csar", toscaCsar.getUrl());

            /**
             * Check the service entity, it should be the same as global metadata information in
             * service-Testservice140-template.yml inside csar.
             */
            Service service = serviceRepository.findById(serviceUuid)
                    .orElseThrow(() -> new EntityNotFoundException("Service: " + serviceUuid + " not found"));
            assertEquals("model UUID", serviceUuid, service.getModelUUID());
            assertEquals("model name", "SVC140", service.getModelName());
            assertEquals("model invariantUUID", serviceInvariantUuid, service.getModelInvariantUUID());
            assertEquals("model version", "1.0", service.getModelVersion());
            assertEquals("description", "SVC140", service.getDescription().trim());
            assertEquals("tosca csar artifact UUID", artifactUuid, service.getCsar().getArtifactUUID());
            assertEquals("service type", "ST", service.getServiceType());
            assertEquals("service role", "Sr", service.getServiceRole());
            assertEquals("environment context", "General_Revenue-Bearing", service.getEnvironmentContext());
            assertEquals("service category", "Network Service", service.getCategory());
            assertNull("workload context", service.getWorkloadContext());
            assertEquals("resource order", "TestVF140", service.getResourceOrder());
            assertEquals("CDS blueprint name", "BP140", service.getBlueprintName());
            assertEquals("CDS blueprint version", "v1.4.0", service.getBlueprintVersion());
            assertEquals("controller actor", "SO-REF-DATA", service.getControllerActor());

            /**
             * Check VNF resource, it should be the same as metadata in the topology template in
             * service-Testservice140-template.yml OR global metadata in the resource-Testservice140-template.yml
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
             * Check VNF resource customization, it should be the same as metadata in the topology template in
             * service-Testservice140-template.yml OR global metadata in the resource-Testservice140-template.yml
             */
            String vnfCustomizationKey = "ca1c8455-8ce2-4a76-a037-3f4cf01cffa0";
            VnfResourceCustomization vnfCustomization =
                    Optional.ofNullable(vnfCustomizationRepository.findOneByModelCustomizationUUID(vnfCustomizationKey))
                            .orElseThrow(() -> new EntityNotFoundException(
                                    "VNF resource customization: " + vnfCustomizationKey + " not found"));
            assertEquals("model customizationUUID", vnfCustomizationKey, vnfCustomization.getModelCustomizationUUID());
            assertEquals("model instance name", "TestVF140 0", vnfCustomization.getModelInstanceName());
            assertNull("NF type", vnfCustomization.getNfType());
            assertNull("NF Role", vnfCustomization.getNfRole());
            assertNull("NF function", vnfCustomization.getNfFunction());
            assertNull("NF naming code", vnfCustomization.getNfNamingCode());
            assertEquals("VNF resource model UUID", vnfResourceKey, vnfCustomization.getVnfResources().getModelUUID());
            assertEquals("Multi stage design", "false", vnfCustomization.getMultiStageDesign());
            assertNotNull("resource input", vnfCustomization.getResourceInput());
            assertEquals("cds blueprint name(sdnc_model_name property)", "BP140", vnfCustomization.getBlueprintName());
            assertEquals("cds blueprint version(sdnc_model_version property)", "v1.4.0",
                    vnfCustomization.getBlueprintVersion());
            assertEquals("controller actor", "SO-REF-DATA", vnfCustomization.getControllerActor());

            /**
             * Check the vnf resource customization with service mapping
             */
            List<VnfResourceCustomization> vnfCustList = service.getVnfCustomizations();
            assertEquals("VNF resource customization entity", 1, vnfCustList.size());
            assertEquals(vnfCustomizationKey, vnfCustList.get(0).getModelCustomizationUUID());

            /**
             * Check the watchdog for component distribution status
             */
            List<WatchdogComponentDistributionStatus> distributionList =
                    watchdogCDStatusRepository.findByDistributionId(this.distributionId);
            assertNotNull(distributionList);
            assertEquals(1, distributionList.size());
            WatchdogComponentDistributionStatus distributionStatus = distributionList.get(0);
            assertEquals("COMPONENT_DONE_OK", distributionStatus.getComponentDistributionStatus());
            assertEquals("SO", distributionStatus.getComponentName());
        } catch (Exception e) {
            logger.info(e.getMessage(), e);
            fail(e.getMessage());
        }
    }

    /**
     * Test with service-Ericservice-csar.csar to test default_software_version field.
     */
    @Test
    public void treatNotification_ValidPnfResource_With_Default_Software_Version_ExpectedOutput() {

        /**
         * service UUID/invariantUUID from global metadata in service-PnfServiceTestCds-template.yml.
         */
        String serviceUuid = "8e7b2bd7-6901-4cc2-b3fb-3b6a1d5631e3";// "77cf276e-905c-43f6-8d54-dda474be2f2e";
        String serviceInvariantUuid = "9a5f99c8-0492-4691-b29a-7360d9c3aae3";// "913e6776-4bc3-49b9-b399-b5bb4690f0c7";

        initMockAaiServer(serviceUuid, serviceInvariantUuid);

        NotificationDataImpl notificationData = new NotificationDataImpl();
        notificationData.setServiceUUID(serviceUuid);
        notificationData.setDistributionID(distributionId);
        notificationData.setServiceInvariantUUID(serviceInvariantUuid);
        notificationData.setServiceVersion("1.0");

        ResourceInfoImpl resourceInfo = constructPnfResourceInfoWithSWV();
        List<ResourceInfoImpl> resourceInfoList = new ArrayList<>();
        resourceInfoList.add(resourceInfo);
        notificationData.setResources(resourceInfoList);

        ArtifactInfoImpl artifactInfo = constructPnfServiceArtifactWithSWV();
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
            assertEquals("tosca csar name", "service-Ericservice-csar.csar", toscaCsar.getName());
            assertEquals("tosca csar version", "1.0", toscaCsar.getVersion());
            assertNull("tosca csar descrption", toscaCsar.getDescription());
            assertEquals("tosca csar checksum", "MANUAL_RECORD", toscaCsar.getArtifactChecksum());
            assertEquals("toscar csar URL", "/download/service-Ericservice-csar.csar", toscaCsar.getUrl());

            /**
             * Check the service entity, it should be the same as global metadata information in
             * service-Testservice140-template.yml inside csar.
             */
            Service service = serviceRepository.findById(serviceUuid)
                    .orElseThrow(() -> new EntityNotFoundException("Service: " + serviceUuid + " not found"));

            /**
             * Check PNF resource, it should be the same as metadata in the topology template in
             * service-PnfServiceTestCds-template.yml OR global metadata in the resource-PnfServiceTestCds-template.yml
             */
            String pnfResourceKey = "7a90f80b-a6f6-4597-8c48-80bda26b4823";

            /**
             * Check PNF resource customization, it should be the same as metadata in the topology template in
             * service-PnfServiceTestCds-template.yml OR global metadata in the resource-PnfServiceTestCds-template.yml
             */
            String pnfCustomizationKey = "c850a53b-b63e-4043-ab10-53aabda78d37";// "9f01263a-eaf7-4d98-a37b-3785f751903e";
            PnfResourceCustomization pnfCustomization = pnfCustomizationRepository.findById(pnfCustomizationKey)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "PNF resource customization: " + pnfCustomizationKey + " not found"));
            assertEquals("model customizationUUID", pnfCustomizationKey, pnfCustomization.getModelCustomizationUUID());
            assertEquals("model instance name", "demo-PNF 1", pnfCustomization.getModelInstanceName());
            assertEquals("NF type", "", pnfCustomization.getNfType());
            assertEquals("NF Role", "", pnfCustomization.getNfRole());
            assertEquals("NF function", "", pnfCustomization.getNfFunction());
            assertEquals("NF naming code", "", pnfCustomization.getNfNamingCode());
            assertEquals("PNF resource model UUID", pnfResourceKey, pnfCustomization.getPnfResources().getModelUUID());
            assertEquals("Multi stage design", "", pnfCustomization.getMultiStageDesign());
            assertNull("resource input", pnfCustomization.getResourceInput());
            assertEquals("cds blueprint name(sdnc_model_name property)", "pm_control",
                    pnfCustomization.getBlueprintName());
            assertEquals("cds blueprint version(sdnc_model_version property)", "1.0.0",
                    pnfCustomization.getBlueprintVersion());
            assertEquals("default software version", "4.0.0", pnfCustomization.getDefaultSoftwareVersion());
            assertTrue("skip post instantiation configuration", pnfCustomization.getSkipPostInstConf());
            assertEquals("controller actor", "SO-REF-DATA", pnfCustomization.getControllerActor());

            /**
             * Check the pnf resource customization with service mapping
             */
            List<PnfResourceCustomization> pnfCustList = service.getPnfCustomizations();
            assertEquals("PNF resource customization entity", 1, pnfCustList.size());
            assertEquals(pnfCustomizationKey, pnfCustList.get(0).getModelCustomizationUUID());

            /**
             * Check the watchdog for component distribution status
             */
            List<WatchdogComponentDistributionStatus> distributionList =
                    watchdogCDStatusRepository.findByDistributionId(this.distributionId);
            assertNotNull(distributionList);
            assertEquals(1, distributionList.size());
            WatchdogComponentDistributionStatus distributionStatus = distributionList.get(0);
            assertEquals("COMPONENT_DONE_OK", distributionStatus.getComponentDistributionStatus());
            assertEquals("SO", distributionStatus.getComponentName());


        } catch (Exception e) {
            logger.info(e.getMessage(), e);
            fail(e.getMessage());
        }
    }

    /**
     * Test with service-Ericservice-csar.csar to test software_vesrions field.
     */
    @Test
    public void treatNotification_ValidPnfResource_With_Software_Version_ExpectedOutput() {

        /**
         * service UUID/invariantUUID from global metadata in service-PnfServiceTestCds-template.yml.
         */
        String serviceUuid = "8e7b2bd7-6901-4cc2-b3fb-3b6a1d5631e3";// "77cf276e-905c-43f6-8d54-dda474be2f2e";
        String serviceInvariantUuid = "9a5f99c8-0492-4691-b29a-7360d9c3aae3";// "913e6776-4bc3-49b9-b399-b5bb4690f0c7";

        initMockAaiServer(serviceUuid, serviceInvariantUuid);

        NotificationDataImpl notificationData = new NotificationDataImpl();
        notificationData.setServiceUUID(serviceUuid);
        notificationData.setDistributionID(distributionId);
        notificationData.setServiceInvariantUUID(serviceInvariantUuid);
        notificationData.setServiceVersion("1.0");

        ResourceInfoImpl resourceInfo = constructPnfResourceInfoWithSWV1();
        List<ResourceInfoImpl> resourceInfoList = new ArrayList<>();
        resourceInfoList.add(resourceInfo);
        notificationData.setResources(resourceInfoList);

        ArtifactInfoImpl artifactInfo = constructPnfServiceArtifactWithSWV();
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
            assertEquals("tosca csar name", "service-Ericservice-csar.csar", toscaCsar.getName());
            assertEquals("tosca csar version", "1.0", toscaCsar.getVersion());
            assertNull("tosca csar descrption", toscaCsar.getDescription());
            assertEquals("tosca csar checksum", "MANUAL_RECORD", toscaCsar.getArtifactChecksum());
            assertEquals("toscar csar URL", "/download/service-Ericservice-csar.csar", toscaCsar.getUrl());

            /**
             * Check the service entity, it should be the same as global metadata information in
             * service-Testservice140-template.yml inside csar.
             */
            Service service = serviceRepository.findById(serviceUuid)
                    .orElseThrow(() -> new EntityNotFoundException("Service: " + serviceUuid + " not found"));

            /**
             * Check PNF resource, it should be the same as metadata in the topology template in
             * service-PnfServiceTestCds-template.yml OR global metadata in the resource-PnfServiceTestCds-template.yml
             */
            String pnfResourceKey = "7a90f80b-a6f6-4597-8c48-80bda26b4823";

            /**
             * Check PNF resource customization, it should be the same as metadata in the topology template in
             * service-PnfServiceTestCds-template.yml OR global metadata in the resource-PnfServiceTestCds-template.yml
             */
            String pnfCustomizationKey = "05660370-41ed-4720-a42b-d6def010c326";// "9f01263a-eaf7-4d98-a37b-3785f751903e";
            PnfResourceCustomization pnfCustomization = pnfCustomizationRepository.findById(pnfCustomizationKey)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "PNF resource customization: " + pnfCustomizationKey + " not found"));
            assertEquals("model customizationUUID", pnfCustomizationKey, pnfCustomization.getModelCustomizationUUID());
            assertEquals("model instance name", "demo-PNF 0", pnfCustomization.getModelInstanceName());
            assertEquals("NF type", "", pnfCustomization.getNfType());
            assertEquals("NF Role", "", pnfCustomization.getNfRole());
            assertEquals("NF function", "", pnfCustomization.getNfFunction());
            assertEquals("NF naming code", "", pnfCustomization.getNfNamingCode());
            assertEquals("PNF resource model UUID", pnfResourceKey, pnfCustomization.getPnfResources().getModelUUID());
            assertEquals("Multi stage design", "", pnfCustomization.getMultiStageDesign());
            assertNull("resource input", pnfCustomization.getResourceInput());
            assertEquals("cds blueprint name(sdnc_model_name property)", "pm_control",
                    pnfCustomization.getBlueprintName());
            assertEquals("cds blueprint version(sdnc_model_version property)", "1.0.0",
                    pnfCustomization.getBlueprintVersion());
            assertEquals("default software version", "foo-123", pnfCustomization.getDefaultSoftwareVersion());
            assertTrue("skip post instantiation configuration", pnfCustomization.getSkipPostInstConf());
            assertEquals("controller actor", "SO-REF-DATA", pnfCustomization.getControllerActor());

            /**
             * Check the pnf resource customization with service mapping
             */
            List<PnfResourceCustomization> pnfCustList = service.getPnfCustomizations();
            assertEquals("PNF resource customization entity", 1, pnfCustList.size());
            assertEquals(pnfCustomizationKey, pnfCustList.get(0).getModelCustomizationUUID());

            /**
             * Check the watchdog for component distribution status
             */
            List<WatchdogComponentDistributionStatus> distributionList =
                    watchdogCDStatusRepository.findByDistributionId(this.distributionId);
            assertNotNull(distributionList);
            assertEquals(1, distributionList.size());
            WatchdogComponentDistributionStatus distributionStatus = distributionList.get(0);
            assertEquals("COMPONENT_DONE_OK", distributionStatus.getComponentDistributionStatus());
            assertEquals("SO", distributionStatus.getComponentName());


        } catch (Exception e) {
            logger.info(e.getMessage(), e);
            fail(e.getMessage());
        }
    }

    private ArtifactInfoImpl constructPnfServiceArtifact() {
        ArtifactInfoImpl artifactInfo = new ArtifactInfoImpl();
        artifactInfo.setArtifactType(ASDCConfiguration.TOSCA_CSAR);
        artifactInfo.setArtifactURL("/download/service-pnfservice.csar");
        artifactInfo.setArtifactName("service-pnfservice.csar");
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
        resourceInfo.setResourceInstanceName("PNF CDS Test");
        resourceInfo.setResourceInvariantUUID("17d9d183-cee5-4a46-b5c4-6d5203f7d2e8");
        resourceInfo.setResoucreType("PNF");
        resourceInfo.setCategory("Application L4+");
        resourceInfo.setSubcategory("Firewall");
        resourceInfo.setResourceUUID("aa5d0562-80e7-43e9-af74-3085e57ab09f");
        resourceInfo.setResourceCustomizationUUID("9f01263a-eaf7-4d98-a37b-3785f751903e");
        resourceInfo.setResourceVersion("1.0");
        return resourceInfo;
    }

    private ArtifactInfoImpl constructVnfServiceArtifact() {
        ArtifactInfoImpl artifactInfo = new ArtifactInfoImpl();
        artifactInfo.setArtifactType(ASDCConfiguration.TOSCA_CSAR);
        artifactInfo.setArtifactURL("/download/service-vnfservice.csar");
        artifactInfo.setArtifactName("service-vnfservice.csar");
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

    private ArtifactInfoImpl constructPnfServiceArtifactWithSWV() {
        ArtifactInfoImpl artifactInfo = new ArtifactInfoImpl();
        artifactInfo.setArtifactType(ASDCConfiguration.TOSCA_CSAR);
        artifactInfo.setArtifactURL("/download/service-Ericservice-csar.csar");// service-pnfservice.csar");
        artifactInfo.setArtifactName("service-Ericservice-csar.csar");// "service-pnfservice.csar");
        artifactInfo.setArtifactVersion("1.0");
        artifactInfo.setArtifactUUID(artifactUuid);
        return artifactInfo;
    }

    /**
     * Construct the PnfResourceInfo based on the resource-DemoPnf-template.yml from service-Ericservice-csar.csar.
     */
    private ResourceInfoImpl constructPnfResourceInfoWithSWV() {
        ResourceInfoImpl resourceInfo = new ResourceInfoImpl();
        resourceInfo.setResourceInstanceName("demo-PNF");
        resourceInfo.setResourceInvariantUUID("e688fe35-21c2-41be-9fd2-c5ae830a5031");
        resourceInfo.setResoucreType("PNF");
        resourceInfo.setCategory("Network L4+");
        resourceInfo.setSubcategory("Common Network Resources");
        resourceInfo.setResourceUUID("7a90f80b-a6f6-4597-8c48-80bda26b4823");
        resourceInfo.setResourceCustomizationUUID("c850a53b-b63e-4043-ab10-53aabda78d37");// "9f01263a-eaf7-4d98-a37b-3785f751903e");
        resourceInfo.setResourceVersion("1.0");
        return resourceInfo;
    }

    /**
     * Construct the PnfResourceInfo based on the resource-DemoPnf-template.yml from service-Ericservice-csar.csar.
     */
    private ResourceInfoImpl constructPnfResourceInfoWithSWV1() {
        ResourceInfoImpl resourceInfo = new ResourceInfoImpl();
        resourceInfo.setResourceInstanceName("demo-PNF");
        resourceInfo.setResourceInvariantUUID("e688fe35-21c2-41be-9fd2-c5ae830a5031");
        resourceInfo.setResoucreType("PNF");
        resourceInfo.setCategory("Network L4+");
        resourceInfo.setSubcategory("Common Network Resources");
        resourceInfo.setResourceUUID("7a90f80b-a6f6-4597-8c48-80bda26b4823");
        resourceInfo.setResourceCustomizationUUID("05660370-41ed-4720-a42b-d6def010c326");// "9f01263a-eaf7-4d98-a37b-3785f751903e");
        resourceInfo.setResourceVersion("1.0");
        return resourceInfo;
    }
}
