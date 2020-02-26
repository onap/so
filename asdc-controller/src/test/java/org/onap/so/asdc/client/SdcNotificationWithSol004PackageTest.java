/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Ericsson. All rights reserved.
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
package org.onap.so.asdc.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.onap.sdc.utils.DistributionStatusEnum.COMPONENT_DONE_ERROR;
import static org.onap.sdc.utils.DistributionStatusEnum.COMPONENT_DONE_OK;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.asdc.BaseTest;
import org.onap.so.asdc.client.exceptions.ASDCControllerException;
import org.onap.so.asdc.client.test.emulators.DistributionClientEmulator;
import org.onap.so.asdc.client.test.emulators.NotificationDataImpl;
import org.onap.so.db.catalog.data.repository.ServiceRepository;
import org.onap.so.db.request.beans.WatchdogComponentDistributionStatus;
import org.onap.so.db.request.beans.WatchdogComponentDistributionStatusId;
import org.onap.so.db.request.data.repository.WatchdogComponentDistributionStatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import com.google.gson.GsonBuilder;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public class SdcNotificationWithSol004PackageTest extends BaseTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(SdcNotificationWithSol004PackageTest.class);
    private static final String ETSI_CATALOG_PACKAGE_ONBOARDING_URL = "/api/catalog/v1/vnfpackages";
    private static final String COMPONENT_NAME = "SO";
    private static final String CSAR_ID = "8f0b72e1-b6d6-42b6-a808-c60b17f04d7a";
    private static final String SDC_GET_RESOURCE_URL = "/sdc/v1/catalog/resources/" + CSAR_ID + "/toscaModel";
    private static final String ETSI_CATALOG_PACKAGE_ONBOARDING_REQUEST = "{\"csarId\": \"" + CSAR_ID + "\"}";
    private static final String DISTRIBUTION_ID = "35f20eb9-238a-4cc2-96dc-0a08f71bc209";
    private static final String VGW_RESOURCE_PATH = "src/test/resources/resource-examples/vgw";
    private static final String SERVICE_UUID = "e051ff77-fb79-451c-8457-1cbf94e4db8f";
    private static final String SERVICE_INVARIANT_UUID = "c2ce924f-0aa1-4777-9b42-c0fec006a883";
    private static final String JOB_ID = "57c13120-0a03-4d2e-837a-7c41d61e4a30";
    private static final String ETSI_CATALOG_PACKAGE_ONBOARDING_JOB_STATUS_URL = "/api/catalog/v1/jobs/" + JOB_ID;
    private static final String ETSI_CATALOG_PACKAGE_ONBOARDING_JOB_RESPONSE = "{\"jobId\": \"" + JOB_ID + "\"}";

    @Autowired
    private ASDCController asdcController;

    private DistributionClientEmulator distributionClient;

    @Autowired
    private WatchdogComponentDistributionStatusRepository watchdogComponentDistributionStatusRepository;

    @Autowired
    protected ServiceRepository serviceRepository;

    @Before
    public void setUp() {
        distributionClient = new DistributionClientEmulator();
        distributionClient.setResourcePath(getAbsolutePath(VGW_RESOURCE_PATH));
        asdcController.setDistributionClient(distributionClient);
        try {
            asdcController.initASDC();
        } catch (final ASDCControllerException controllerException) {
            LOGGER.error(controllerException.getMessage(), controllerException);
            fail(controllerException.getMessage());
        }
    }

    @After
    public void shutDown() {
        try {
            if (serviceRepository.existsById(SERVICE_UUID)) {
                LOGGER.debug("Deleting existing service using {} ", SERVICE_UUID);
                serviceRepository.deleteById(SERVICE_UUID);
            }

            final WatchdogComponentDistributionStatusId distributionId = new WatchdogComponentDistributionStatusId();
            distributionId.setDistributionId(DISTRIBUTION_ID);
            distributionId.setComponentName(COMPONENT_NAME);
            if (watchdogComponentDistributionStatusRepository.existsById(distributionId)) {
                LOGGER.debug("Deleting existing WatchdogComponentDistributionStatus using {} ", distributionId);
                watchdogComponentDistributionStatusRepository.deleteById(distributionId);
            }
            asdcController.closeASDC();
        } catch (final ASDCControllerException asdcControllerException) {
            LOGGER.error(asdcControllerException.getMessage(), asdcControllerException);
            fail(asdcControllerException.getMessage());
        }
    }

    @Test
    public void testTreatNotification_vgwServiceContainingSol004Package_successfullyOnboard() throws IOException {
        initMockAaiServer(SERVICE_UUID, SERVICE_INVARIANT_UUID);

        wireMockServer.stubFor(get(SDC_GET_RESOURCE_URL)
                .willReturn(aResponse().withBody(getFileContent(
                        Paths.get(getAbsolutePath(VGW_RESOURCE_PATH), "SDC_RESOURCE_CSAR", "vgw_sdc_resource.csar"))))
                .withHeader(ACCEPT, equalTo(APPLICATION_OCTET_STREAM_VALUE)));

        wireMockServer.stubFor(post(urlEqualTo(ETSI_CATALOG_PACKAGE_ONBOARDING_URL))
                .willReturn(okJson(ETSI_CATALOG_PACKAGE_ONBOARDING_JOB_RESPONSE)));

        wireMockServer.stubFor(get(urlEqualTo(ETSI_CATALOG_PACKAGE_ONBOARDING_JOB_STATUS_URL))
                .willReturn(okJson(new String(getFileContent(Paths.get(getAbsolutePath(VGW_RESOURCE_PATH),
                        "etsi-catalog-package-onboading-job-status-successful.json"))))));

        asdcController.treatNotification(getNotificationDataImplObject());

        final List<WatchdogComponentDistributionStatus> distributionList =
                watchdogComponentDistributionStatusRepository.findByDistributionId(DISTRIBUTION_ID);
        assertNotNull(distributionList);
        assertEquals(1, distributionList.size());
        final WatchdogComponentDistributionStatus distributionStatus = distributionList.get(0);
        assertEquals(COMPONENT_DONE_OK.toString(), distributionStatus.getComponentDistributionStatus());
        assertEquals(COMPONENT_NAME, distributionStatus.getComponentName());

        verify(postRequestedFor(urlEqualTo(ETSI_CATALOG_PACKAGE_ONBOARDING_URL))
                .withRequestBody(equalToJson(ETSI_CATALOG_PACKAGE_ONBOARDING_REQUEST)));

        verify(getRequestedFor(urlEqualTo(ETSI_CATALOG_PACKAGE_ONBOARDING_JOB_STATUS_URL)));
    }

    @Test
    public void testTreatNotification_vgwServiceUnableToGeSdcResource_successfullyOnboard() throws IOException {
        initMockAaiServer(SERVICE_UUID, SERVICE_INVARIANT_UUID);

        wireMockServer
                .stubFor(get(SDC_GET_RESOURCE_URL).willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value()))
                        .withHeader(ACCEPT, equalTo(APPLICATION_OCTET_STREAM_VALUE)));

        asdcController.treatNotification(getNotificationDataImplObject());

        final List<WatchdogComponentDistributionStatus> distributionList =
                watchdogComponentDistributionStatusRepository.findByDistributionId(DISTRIBUTION_ID);
        assertNotNull(distributionList);
        assertEquals(1, distributionList.size());
        final WatchdogComponentDistributionStatus distributionStatus = distributionList.get(0);
        assertEquals(COMPONENT_DONE_OK.toString(), distributionStatus.getComponentDistributionStatus());
        assertEquals(COMPONENT_NAME, distributionStatus.getComponentName());
    }

    @Test
    public void testTreatNotification_vgwServiceContainingNonEtsiSdcResource_successfullyOnboard() throws IOException {

        initMockAaiServer(SERVICE_UUID, SERVICE_INVARIANT_UUID);

        wireMockServer.stubFor(get(SDC_GET_RESOURCE_URL)
                .willReturn(aResponse().withBody(getFileContent(
                        Paths.get(getAbsolutePath(VGW_RESOURCE_PATH), "service-Vgwservicev1-csar.csar"))))
                .withHeader(ACCEPT, equalTo(APPLICATION_OCTET_STREAM_VALUE)));

        asdcController.treatNotification(getNotificationDataImplObject());

        final List<WatchdogComponentDistributionStatus> distributionList =
                watchdogComponentDistributionStatusRepository.findByDistributionId(DISTRIBUTION_ID);
        assertNotNull(distributionList);
        assertEquals(1, distributionList.size());
        final WatchdogComponentDistributionStatus distributionStatus = distributionList.get(0);
        assertEquals(COMPONENT_DONE_OK.toString(), distributionStatus.getComponentDistributionStatus());
        assertEquals(COMPONENT_NAME, distributionStatus.getComponentName());

    }

    @Test
    public void testTreatNotification_vgwServiceContainingSol004Package_onnboardRequestToEtsiCatalogReturnsBadGatway_distributionStatusError()
            throws IOException {
        initMockAaiServer(SERVICE_UUID, SERVICE_INVARIANT_UUID);

        wireMockServer.stubFor(get(SDC_GET_RESOURCE_URL)
                .willReturn(aResponse().withBody(getFileContent(
                        Paths.get(getAbsolutePath(VGW_RESOURCE_PATH), "SDC_RESOURCE_CSAR", "vgw_sdc_resource.csar"))))
                .withHeader(ACCEPT, equalTo(APPLICATION_OCTET_STREAM_VALUE)));

        wireMockServer.stubFor(post(urlEqualTo(ETSI_CATALOG_PACKAGE_ONBOARDING_URL))
                .willReturn(aResponse().withStatus(HttpStatus.BAD_GATEWAY.value())));

        asdcController.treatNotification(getNotificationDataImplObject());

        final List<WatchdogComponentDistributionStatus> distributionList =
                watchdogComponentDistributionStatusRepository.findByDistributionId(DISTRIBUTION_ID);
        assertNotNull(distributionList);
        assertEquals(1, distributionList.size());
        final WatchdogComponentDistributionStatus distributionStatus = distributionList.get(0);
        assertEquals(COMPONENT_DONE_ERROR.toString(), distributionStatus.getComponentDistributionStatus());
        assertEquals(COMPONENT_NAME, distributionStatus.getComponentName());

        verify(postRequestedFor(urlEqualTo(ETSI_CATALOG_PACKAGE_ONBOARDING_URL))
                .withRequestBody(equalToJson(ETSI_CATALOG_PACKAGE_ONBOARDING_REQUEST)));

    }

    @Test
    public void testTreatNotification_vgwServiceContainingSol004Package_getJobStatusReturnsBadGatway_distributionStatusError()
            throws IOException {
        initMockAaiServer(SERVICE_UUID, SERVICE_INVARIANT_UUID);

        wireMockServer.stubFor(get(SDC_GET_RESOURCE_URL)
                .willReturn(aResponse().withBody(getFileContent(
                        Paths.get(getAbsolutePath(VGW_RESOURCE_PATH), "SDC_RESOURCE_CSAR", "vgw_sdc_resource.csar"))))
                .withHeader(ACCEPT, equalTo(APPLICATION_OCTET_STREAM_VALUE)));

        wireMockServer.stubFor(post(urlEqualTo(ETSI_CATALOG_PACKAGE_ONBOARDING_URL))
                .willReturn(okJson(ETSI_CATALOG_PACKAGE_ONBOARDING_JOB_RESPONSE)));

        wireMockServer.stubFor(get(urlEqualTo(ETSI_CATALOG_PACKAGE_ONBOARDING_JOB_STATUS_URL))
                .willReturn(aResponse().withStatus(HttpStatus.BAD_GATEWAY.value())));

        asdcController.treatNotification(getNotificationDataImplObject());

        final List<WatchdogComponentDistributionStatus> distributionList =
                watchdogComponentDistributionStatusRepository.findByDistributionId(DISTRIBUTION_ID);
        assertNotNull(distributionList);
        assertEquals(1, distributionList.size());
        final WatchdogComponentDistributionStatus distributionStatus = distributionList.get(0);
        assertEquals(COMPONENT_DONE_ERROR.toString(), distributionStatus.getComponentDistributionStatus());
        assertEquals(COMPONENT_NAME, distributionStatus.getComponentName());

        verify(postRequestedFor(urlEqualTo(ETSI_CATALOG_PACKAGE_ONBOARDING_URL))
                .withRequestBody(equalToJson(ETSI_CATALOG_PACKAGE_ONBOARDING_REQUEST)));

        verify(getRequestedFor(urlEqualTo(ETSI_CATALOG_PACKAGE_ONBOARDING_JOB_STATUS_URL)));

    }

    @Test
    public void testTreatNotification_vgwServiceContainingSol004Package_getJobStatusBodyWithStatusError_distributionStatusError()
            throws IOException {

        initMockAaiServer(SERVICE_UUID, SERVICE_INVARIANT_UUID);

        wireMockServer.stubFor(get(SDC_GET_RESOURCE_URL)
                .willReturn(aResponse().withBody(getFileContent(
                        Paths.get(getAbsolutePath(VGW_RESOURCE_PATH), "SDC_RESOURCE_CSAR", "vgw_sdc_resource.csar"))))
                .withHeader(ACCEPT, equalTo(APPLICATION_OCTET_STREAM_VALUE)));

        wireMockServer.stubFor(post(urlEqualTo(ETSI_CATALOG_PACKAGE_ONBOARDING_URL))
                .willReturn(okJson(ETSI_CATALOG_PACKAGE_ONBOARDING_JOB_RESPONSE)));

        wireMockServer.stubFor(get(urlEqualTo(ETSI_CATALOG_PACKAGE_ONBOARDING_JOB_STATUS_URL))
                .willReturn(okJson(new String(getFileContent(Paths.get(getAbsolutePath(VGW_RESOURCE_PATH),
                        "etsi-catalog-package-onboading-job-status-error.json"))))));

        asdcController.treatNotification(getNotificationDataImplObject());

        final List<WatchdogComponentDistributionStatus> distributionList =
                watchdogComponentDistributionStatusRepository.findByDistributionId(DISTRIBUTION_ID);
        assertNotNull(distributionList);
        assertEquals(1, distributionList.size());
        final WatchdogComponentDistributionStatus distributionStatus = distributionList.get(0);
        assertEquals(COMPONENT_DONE_ERROR.toString(), distributionStatus.getComponentDistributionStatus());
        assertEquals(COMPONENT_NAME, distributionStatus.getComponentName());

        verify(postRequestedFor(urlEqualTo(ETSI_CATALOG_PACKAGE_ONBOARDING_URL))
                .withRequestBody(equalToJson(ETSI_CATALOG_PACKAGE_ONBOARDING_REQUEST)));

        verify(getRequestedFor(urlEqualTo(ETSI_CATALOG_PACKAGE_ONBOARDING_JOB_STATUS_URL)));


    }

    @Test
    public void testTreatNotification_vgwServiceContainingSol004PackageAndPackageAlreadyExistsInEtsiCatalog_successfullyOnboard()
            throws IOException {
        initMockAaiServer(SERVICE_UUID, SERVICE_INVARIANT_UUID);

        wireMockServer.stubFor(get(SDC_GET_RESOURCE_URL)
                .willReturn(aResponse().withBody(getFileContent(
                        Paths.get(getAbsolutePath(VGW_RESOURCE_PATH), "SDC_RESOURCE_CSAR", "vgw_sdc_resource.csar"))))
                .withHeader(ACCEPT, equalTo(APPLICATION_OCTET_STREAM_VALUE)));

        wireMockServer.stubFor(post(urlEqualTo(ETSI_CATALOG_PACKAGE_ONBOARDING_URL))
                .willReturn(okJson(ETSI_CATALOG_PACKAGE_ONBOARDING_JOB_RESPONSE)));

        wireMockServer.stubFor(get(urlEqualTo(ETSI_CATALOG_PACKAGE_ONBOARDING_JOB_STATUS_URL))
                .willReturn(okJson(new String(getFileContent(Paths.get(getAbsolutePath(VGW_RESOURCE_PATH),
                        "etsi-catalog-package-onboading-job-status-error-package-exists.json"))))));

        asdcController.treatNotification(getNotificationDataImplObject());

        final List<WatchdogComponentDistributionStatus> distributionList =
                watchdogComponentDistributionStatusRepository.findByDistributionId(DISTRIBUTION_ID);
        assertNotNull(distributionList);
        assertEquals(1, distributionList.size());
        final WatchdogComponentDistributionStatus distributionStatus = distributionList.get(0);
        assertEquals(COMPONENT_DONE_OK.toString(), distributionStatus.getComponentDistributionStatus());
        assertEquals(COMPONENT_NAME, distributionStatus.getComponentName());

        verify(postRequestedFor(urlEqualTo(ETSI_CATALOG_PACKAGE_ONBOARDING_URL))
                .withRequestBody(equalToJson(ETSI_CATALOG_PACKAGE_ONBOARDING_REQUEST)));

        verify(getRequestedFor(urlEqualTo(ETSI_CATALOG_PACKAGE_ONBOARDING_JOB_STATUS_URL)));
    }


    private byte[] getFileContent(final Path path) throws IOException {
        return Files.readAllBytes(path);
    }

    private NotificationDataImpl getNotificationDataImplObject() throws IOException {
        final Path filePath = Paths.get(getAbsolutePath(VGW_RESOURCE_PATH), "dmaap-notification-message.json");
        final byte[] bytes = Files.readAllBytes(filePath);

        return new GsonBuilder().setPrettyPrinting().create().fromJson(new String(bytes), NotificationDataImpl.class);
    }

    /**
     * Mock the AAI using wireshark.
     */
    private void initMockAaiServer(final String serviceUuid, final String serviceInvariantUuid) {
        final String modelEndpoint = "/aai/v19/service-design-and-creation/models/model/" + serviceInvariantUuid
                + "/model-vers/model-ver/" + serviceUuid + "?depth=0";

        wireMockServer.stubFor(post(urlEqualTo(modelEndpoint)).willReturn(ok()));
    }

    private String getAbsolutePath(final String path) {
        final File file = new File(path);
        return file.getAbsolutePath();
    }
}
