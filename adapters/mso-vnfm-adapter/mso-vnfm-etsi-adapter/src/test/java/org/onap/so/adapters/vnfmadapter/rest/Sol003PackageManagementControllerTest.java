/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

package org.onap.so.adapters.vnfmadapter.rest;

import static org.junit.Assert.*;
import static org.onap.so.adapters.vnfmadapter.Constants.PACKAGE_MANAGEMENT_BASE_URL;
import static org.onap.so.client.RestTemplateConfig.CONFIGURABLE_REST_TEMPLATE;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.adapters.vnfmadapter.VnfmAdapterApplication;
import org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.*;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.packagemanagement.model.InlineResponse2001;
import org.onap.so.configuration.rest.BasicHttpHeadersProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import com.google.gson.Gson;

/**
 * @author gareth.roper@est.tech
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = VnfmAdapterApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class Sol003PackageManagementControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(Sol003PackageManagementControllerTest.class);

    @LocalServerPort
    private int port;

    @Autowired
    @Qualifier(CONFIGURABLE_REST_TEMPLATE)
    private RestTemplate testRestTemplate;

    @Autowired
    private Sol003PackageManagementController controller;

    @Autowired
    private TestRestTemplate restTemplate;

    private static final String VNF_PACKAGE_ID = "myVnfPackageId";
    private static final String ARTIFACT_PATH = "myArtifactPath";
    private static final String MSB_BASE_URL = "http://msb_iag.onap:80/api/vnfpkgm/v1/vnf_packages";
    private static final String VNFPKGM_BASE_URL = PACKAGE_MANAGEMENT_BASE_URL + "/vnf_packages";
    private static final String localhostUrl = "http://localhost:";
    private static final String GET_VNF_PACKAGES_URL = "";
    private static final String GET_VNF_PACKAGE_BY_ID_URL = "/" + VNF_PACKAGE_ID;
    private static final String VNFD_ID = "vnfdId";
    private static final String VNF_PROVIDER = "vnfProvider";
    private static final String VNF_PRODUCT_NAME = "vnfProductName";
    private static final String VNF_SOFTWARE_VERSION = "vnfSoftwareVersion";
    private static final String VNFD_VERSION = "vnfdVersion";
    private static final String ALGORITHM = "algorithm";
    private static final String HASH = "hash";
    private static final String URI_HREF = "uriHref";

    private MockRestServiceServer mockRestServer;
    private BasicHttpHeadersProvider basicHttpHeadersProvider;
    private final Gson gson = new Gson();

    public Sol003PackageManagementControllerTest() {}

    @Before
    public void setUp() {
        final MockRestServiceServer.MockRestServiceServerBuilder builder =
                MockRestServiceServer.bindTo(testRestTemplate);
        builder.ignoreExpectOrder(true);
        mockRestServer = builder.build();
        basicHttpHeadersProvider = new BasicHttpHeadersProvider();
    }

    @Test
    public void testGetPackageContent_ValidArray_Success() {
        final byte[] responseArray = buildByteArrayWithRandomData(10);

        mockRestServer.expect(requestTo(MSB_BASE_URL + "/" + VNF_PACKAGE_ID + "/package_content"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(responseArray, MediaType.APPLICATION_OCTET_STREAM));

        final String testURL = "http://localhost:" + port + PACKAGE_MANAGEMENT_BASE_URL + "/vnf_packages/"
                + VNF_PACKAGE_ID + "/package_content";
        final HttpEntity<?> request = new HttpEntity<>(basicHttpHeadersProvider.getHttpHeaders());
        final ResponseEntity<byte[]> responseEntity =
                restTemplate.withBasicAuth("test", "test").exchange(testURL, HttpMethod.GET, request, byte[].class);

        assertEquals(byte[].class, responseEntity.getBody().getClass());
        assertArrayEquals(responseEntity.getBody(), responseArray);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void testOnGetPackageContent_Conflict_Fail() {
        mockRestServer.expect(requestTo(MSB_BASE_URL + "/" + VNF_PACKAGE_ID + "/package_content"))
                .andExpect(method(HttpMethod.GET)).andRespond(withStatus(HttpStatus.CONFLICT));

        final ResponseEntity<ProblemDetails> responseEntity = sendHttpRequest(VNF_PACKAGE_ID + "/package_content");

        assertTrue(responseEntity.getBody() instanceof ProblemDetails);
        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
    }

    @Test
    public void testOnGetPackageContent_NotFound_Fail() {
        mockRestServer.expect(requestTo(MSB_BASE_URL + "/" + VNF_PACKAGE_ID + "/package_content"))
                .andExpect(method(HttpMethod.GET)).andRespond(withStatus(HttpStatus.NOT_FOUND));

        final ResponseEntity<ProblemDetails> responseEntity = sendHttpRequest(VNF_PACKAGE_ID + "/package_content");

        assertTrue(responseEntity.getBody() instanceof ProblemDetails);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    public void testOnGetPackageContent_UnauthorizedClient_Fail() {
        final String testURL = "http://localhost:" + port + PACKAGE_MANAGEMENT_BASE_URL + "/vnf_packages/"
                + VNF_PACKAGE_ID + "/package_content";
        final HttpEntity<?> request = new HttpEntity<>(basicHttpHeadersProvider.getHttpHeaders());
        final ResponseEntity<ProblemDetails> responseEntity =
                restTemplate.exchange(testURL, HttpMethod.GET, request, ProblemDetails.class);

        assertTrue(responseEntity.getBody() instanceof ProblemDetails);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Test
    public void testOnGetPackageContent_InternalServerError_Fail() {
        mockRestServer.expect(requestTo(MSB_BASE_URL + "/" + VNF_PACKAGE_ID + "/package_content"))
                .andExpect(method(HttpMethod.GET)).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        final ResponseEntity<ProblemDetails> responseEntity = sendHttpRequest(VNF_PACKAGE_ID + "/package_content");

        assertTrue(responseEntity.getBody() instanceof ProblemDetails);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }

    @Test
    public void testOnGetPackageContent_BadRequest_Fail() {
        mockRestServer.expect(requestTo(MSB_BASE_URL + "/" + VNF_PACKAGE_ID + "/package_content"))
                .andExpect(method(HttpMethod.GET)).andRespond(withStatus(HttpStatus.BAD_REQUEST));

        final ResponseEntity<ProblemDetails> responseEntity = sendHttpRequest(VNF_PACKAGE_ID + "/package_content");

        assertTrue(responseEntity.getBody() instanceof ProblemDetails);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }

    @Test
    public void testOnGetPackageContent_UnauthorizedServer_InternalError_Fail() {
        mockRestServer.expect(requestTo(MSB_BASE_URL + "/" + VNF_PACKAGE_ID + "/package_content"))
                .andExpect(method(HttpMethod.GET)).andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        final ResponseEntity<ProblemDetails> responseEntity = sendHttpRequest(VNF_PACKAGE_ID + "/package_content");

        assertTrue(responseEntity.getBody() instanceof ProblemDetails);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }

    @Test
    public void testGetPackageContent_SuccessResponseFromServerWithNullPackage_Fail() {
        mockRestServer.expect(requestTo(MSB_BASE_URL + "/" + VNF_PACKAGE_ID + "/package_content"))
                .andExpect(method(HttpMethod.GET)).andRespond(withSuccess());

        final ResponseEntity<ProblemDetails> responseEntity = sendHttpRequest(VNF_PACKAGE_ID + "/package_content");

        assertEquals(ProblemDetails.class, responseEntity.getBody().getClass());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }

    @Test
    public void testGetPackageArtifact_ValidArray_Success() {
        final byte[] responseArray = buildByteArrayWithRandomData(10);

        mockRestServer.expect(requestTo(MSB_BASE_URL + "/" + VNF_PACKAGE_ID + "/artifacts/" + ARTIFACT_PATH))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(responseArray, MediaType.APPLICATION_OCTET_STREAM));

        final String testURL = "http://localhost:" + port + PACKAGE_MANAGEMENT_BASE_URL + "/vnf_packages/"
                + VNF_PACKAGE_ID + "/artifacts/" + ARTIFACT_PATH;
        final HttpEntity<?> request = new HttpEntity<>(basicHttpHeadersProvider.getHttpHeaders());
        final ResponseEntity<byte[]> responseEntity =
                restTemplate.withBasicAuth("test", "test").exchange(testURL, HttpMethod.GET, request, byte[].class);

        assertEquals(byte[].class, responseEntity.getBody().getClass());
        assertArrayEquals(responseEntity.getBody(), responseArray);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void testOnGetPackageArtifact_Conflict_Fail() {
        mockRestServer.expect(requestTo(MSB_BASE_URL + "/" + VNF_PACKAGE_ID + "/artifacts/" + ARTIFACT_PATH))
                .andExpect(method(HttpMethod.GET)).andRespond(withStatus(HttpStatus.CONFLICT));

        final ResponseEntity<ProblemDetails> responseEntity =
                sendHttpRequest(VNF_PACKAGE_ID + "/artifacts/" + ARTIFACT_PATH);

        assertNotNull(responseEntity.getBody());
        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
    }

    @Test
    public void testOnGetPackageArtifact_NotFound_Fail() {
        mockRestServer.expect(requestTo(MSB_BASE_URL + "/" + VNF_PACKAGE_ID + "/artifacts/" + ARTIFACT_PATH))
                .andExpect(method(HttpMethod.GET)).andRespond(withStatus(HttpStatus.NOT_FOUND));

        final ResponseEntity<ProblemDetails> responseEntity =
                sendHttpRequest(VNF_PACKAGE_ID + "/artifacts/" + ARTIFACT_PATH);

        assertNotNull(responseEntity.getBody());
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    public void testOnGetPackageArtifact_UnauthorizedClient_Fail() {
        final String testURL = "http://localhost:" + port + PACKAGE_MANAGEMENT_BASE_URL + "/vnf_packages/"
                + VNF_PACKAGE_ID + "/artifacts/" + ARTIFACT_PATH;
        final HttpEntity<?> request = new HttpEntity<>(basicHttpHeadersProvider.getHttpHeaders());
        final ResponseEntity<ProblemDetails> responseEntity =
                restTemplate.exchange(testURL, HttpMethod.GET, request, ProblemDetails.class);

        assertNotNull(responseEntity.getBody());
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Test
    public void testOnGetPackageArtifact_InternalServerError_Fail() {
        mockRestServer.expect(requestTo(MSB_BASE_URL + "/" + VNF_PACKAGE_ID + "/artifacts/" + ARTIFACT_PATH))
                .andExpect(method(HttpMethod.GET)).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        final ResponseEntity<ProblemDetails> responseEntity =
                sendHttpRequest(VNF_PACKAGE_ID + "/artifacts/" + ARTIFACT_PATH);

        assertNotNull(responseEntity.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }

    @Test
    public void testOnGetPackageArtifact_BadRequest_Fail() {
        mockRestServer.expect(requestTo(MSB_BASE_URL + "/" + VNF_PACKAGE_ID + "/artifacts/" + ARTIFACT_PATH))
                .andExpect(method(HttpMethod.GET)).andRespond(withStatus(HttpStatus.BAD_REQUEST));

        final ResponseEntity<ProblemDetails> responseEntity =
                sendHttpRequest(VNF_PACKAGE_ID + "/artifacts/" + ARTIFACT_PATH);

        assertNotNull(responseEntity.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }

    @Test
    public void testOnGetPackageArtifact_UnauthorizedServer_InternalError_Fail() {
        mockRestServer.expect(requestTo(MSB_BASE_URL + "/" + VNF_PACKAGE_ID + "/artifacts/" + ARTIFACT_PATH))
                .andExpect(method(HttpMethod.GET)).andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        final ResponseEntity<ProblemDetails> responseEntity =
                sendHttpRequest(VNF_PACKAGE_ID + "/artifacts/" + ARTIFACT_PATH);

        assertNotNull(responseEntity.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }

    @Test
    public void testGetPackageArtifact_SuccessResponseFromServerWithNullPackage_Fail() {
        mockRestServer.expect(requestTo(MSB_BASE_URL + "/" + VNF_PACKAGE_ID + "/artifacts/" + ARTIFACT_PATH))
                .andExpect(method(HttpMethod.GET)).andRespond(withSuccess());

        final ResponseEntity<ProblemDetails> responseEntity =
                sendHttpRequest(VNF_PACKAGE_ID + "/artifacts/" + ARTIFACT_PATH);

        assertNotNull(responseEntity.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }

    @Test
    public void testVnfPackagesReceivedAsInlineResponse2001ListIfGetVnfPackagesSuccessful() {
        final VnfPkgInfo[] responses = createVnfPkgArray();

        mockRestServer.expect(requestTo(MSB_BASE_URL)).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(gson.toJson(responses), MediaType.APPLICATION_JSON));

        final String testURL = localhostUrl + port + VNFPKGM_BASE_URL;
        final HttpEntity<?> request = new HttpEntity<>(basicHttpHeadersProvider.getHttpHeaders());

        final ResponseEntity<InlineResponse2001[]> responseEntity = restTemplate.withBasicAuth("test", "test")
                .exchange(testURL, HttpMethod.GET, request, InlineResponse2001[].class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        final InlineResponse2001[] inlineResponse2001array = responseEntity.getBody();
        final InlineResponse2001 inlineResponse2001 = inlineResponse2001array[0];
        assertEquals(VNF_PACKAGE_ID, inlineResponse2001.getId());
        assertEquals(VNFD_ID, inlineResponse2001.getVnfdId());
        assertEquals(VNFD_ID, inlineResponse2001.getSoftwareImages().get(0).getId());
        assertEquals(VNF_PRODUCT_NAME, inlineResponse2001.getSoftwareImages().get(0).getName());
        assertEquals(ALGORITHM, inlineResponse2001.getChecksum().getAlgorithm());
        assertEquals(HASH, inlineResponse2001.getChecksum().getHash());
        assertEquals(ARTIFACT_PATH, inlineResponse2001.getAdditionalArtifacts().get(0).getArtifactPath());
        assertEquals(ALGORITHM, inlineResponse2001.getAdditionalArtifacts().get(0).getChecksum().getAlgorithm());
        assertEquals(HASH, inlineResponse2001.getAdditionalArtifacts().get(0).getChecksum().getHash());
        assertEquals(URI_HREF, inlineResponse2001.getLinks().getSelf().getHref());
    }

    @Test
    public void test400BadRequestInfoReceivedAsProblemDetailsIfGetVnfPackagesIs400BadRequest() {
        mockRestServer.expect(requestTo(MSB_BASE_URL)).andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        final ResponseEntity<ProblemDetails> responseEntity = sendHttpRequest(GET_VNF_PACKAGES_URL);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        assertNotNull(responseEntity.getBody());
        final ProblemDetails problemDetails = responseEntity.getBody();
        assertEquals("Error: Bad Request Received", problemDetails.getDetail());
    }

    @Test
    public void test404NotFoundInfoReceivedAsProblemDetailsIfGetVnfPackagesIs404NotFound() {
        mockRestServer.expect(requestTo(MSB_BASE_URL)).andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        final ResponseEntity<ProblemDetails> responseEntity = sendHttpRequest(GET_VNF_PACKAGES_URL);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());

        assertNotNull(responseEntity.getBody());
        final ProblemDetails problemDetails = responseEntity.getBody();
        assertEquals("No Vnf Packages found", problemDetails.getDetail());
    }

    @Test
    public void test500InternalServerErrorProblemDetailsReceivedIfGetVnfPackagesReturns500InternalServerError() {
        mockRestServer.expect(requestTo(MSB_BASE_URL)).andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        final ResponseEntity<ProblemDetails> responseEntity = sendHttpRequest(GET_VNF_PACKAGES_URL);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());

        assertNotNull(responseEntity.getBody());
        final ProblemDetails problemDetails = responseEntity.getBody();
        assertEquals("Internal Server Error Occurred.", problemDetails.getDetail());
    }

    @Test
    public void test500InternalServerErrorProblemDetailsReceivedIfGetVnfPackagesReturnsANullPackage() {
        mockRestServer.expect(requestTo(MSB_BASE_URL)).andExpect(method(HttpMethod.GET)).andRespond(withSuccess());

        final ResponseEntity<ProblemDetails> responseEntity = sendHttpRequest(GET_VNF_PACKAGES_URL);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());

        assertNotNull(responseEntity.getBody());
        final ProblemDetails problemDetails = responseEntity.getBody();
        assertEquals("An error occurred, a null response was received by the\n"
                + " Sol003PackageManagementController from the EtsiCatalogManager using the GET \"vnf_packages\" \n"
                + "endpoint.", problemDetails.getDetail());
    }

    @Test
    public void testVnfPackageReceivedAsInlineResponse2001IfGetVnfPackageByIdSuccessful() {
        final VnfPkgInfo response = createVnfPkgInfo(VNF_PACKAGE_ID);

        mockRestServer.expect(requestTo(MSB_BASE_URL + "/" + VNF_PACKAGE_ID)).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(gson.toJson(response), MediaType.APPLICATION_JSON));

        final String testURL = localhostUrl + port + VNFPKGM_BASE_URL + "/" + VNF_PACKAGE_ID;
        final HttpEntity<?> request = new HttpEntity<>(basicHttpHeadersProvider.getHttpHeaders());
        final ResponseEntity<InlineResponse2001> responseEntity = restTemplate.withBasicAuth("test", "test")
                .exchange(testURL, HttpMethod.GET, request, InlineResponse2001.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        final InlineResponse2001 inlineResponse2001 = responseEntity.getBody();
        assertEquals(VNF_PACKAGE_ID, inlineResponse2001.getId());
        assertEquals(VNFD_ID, inlineResponse2001.getVnfdId());
        assertEquals(VNFD_ID, inlineResponse2001.getSoftwareImages().get(0).getId());
        assertEquals(VNF_PRODUCT_NAME, inlineResponse2001.getSoftwareImages().get(0).getName());
        assertEquals(ALGORITHM, inlineResponse2001.getChecksum().getAlgorithm());
        assertEquals(HASH, inlineResponse2001.getChecksum().getHash());
        assertEquals(ARTIFACT_PATH, inlineResponse2001.getAdditionalArtifacts().get(0).getArtifactPath());
        assertEquals(ALGORITHM, inlineResponse2001.getAdditionalArtifacts().get(0).getChecksum().getAlgorithm());
        assertEquals(HASH, inlineResponse2001.getAdditionalArtifacts().get(0).getChecksum().getHash());
        assertEquals(URI_HREF, inlineResponse2001.getLinks().getSelf().getHref());
    }

    @Test
    public void test400BadRequestInfoReceivedAsProblemDetailsIfGetVnfPackageByIdIs400BadRequest() {
        mockRestServer.expect(requestTo(MSB_BASE_URL + "/" + VNF_PACKAGE_ID)).andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        final ResponseEntity<ProblemDetails> responseEntity = sendHttpRequest(GET_VNF_PACKAGE_BY_ID_URL);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        final ProblemDetails problemDetails = responseEntity.getBody();
        assertEquals("Error: Bad Request Received", problemDetails.getDetail());
    }

    @Test
    public void test404NotFoundInfoReceivedAsProblemDetailsIfGetVnfPackageByIdIs404NotFound() {
        mockRestServer.expect(requestTo(MSB_BASE_URL + "/" + VNF_PACKAGE_ID)).andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        final ResponseEntity<ProblemDetails> responseEntity = sendHttpRequest(GET_VNF_PACKAGE_BY_ID_URL);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        final ProblemDetails problemDetails = responseEntity.getBody();
        assertEquals("No Vnf Package found with vnfPkgId: " + VNF_PACKAGE_ID, problemDetails.getDetail());
    }

    @Test
    public void test500InternalServerErrorProblemDetailsReceivedIfGetVnfPackageByIdReturns500InternalServerError() {
        mockRestServer.expect(requestTo(MSB_BASE_URL + "/" + VNF_PACKAGE_ID)).andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        final ResponseEntity<ProblemDetails> responseEntity = sendHttpRequest(GET_VNF_PACKAGE_BY_ID_URL);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        final ProblemDetails problemDetails = responseEntity.getBody();
        assertEquals("Internal Server Error Occurred.", problemDetails.getDetail());
    }

    @Test
    public void test500InternalServerErrorProblemDetailsReceivedIfGetVnfPackageByIdReturnsANullPackage() {
        mockRestServer.expect(requestTo(MSB_BASE_URL + "/" + VNF_PACKAGE_ID)).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess());

        final ResponseEntity<ProblemDetails> responseEntity = sendHttpRequest(GET_VNF_PACKAGE_BY_ID_URL);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());

        assertNotNull(responseEntity.getBody());
        final ProblemDetails problemDetails = responseEntity.getBody();
        assertEquals("An error occurred, a null response was received by the\n"
                + " Sol003PackageManagementController from the EtsiCatalogManager using the GET \"vnf_packages\" by vnfPkgId: \""
                + VNF_PACKAGE_ID + "\" \n" + "endpoint.", problemDetails.getDetail());
    }

    // The below test method is here to improve code coverage and provide a foundation for writing future tests
    @Test
    public void testGetVnfd_Not_Implemented() {
        final ResponseEntity<ProblemDetails> responseEntity = sendHttpRequest(VNF_PACKAGE_ID + "/vnfd");
        assertEquals(HttpStatus.NOT_IMPLEMENTED, responseEntity.getStatusCode());
    }

    // Simply returns a byte array filled with random data, for use in the tests.
    private byte[] buildByteArrayWithRandomData(final int sizeInKb) {
        final Random rnd = new Random();
        final byte[] b = new byte[sizeInKb * 1024]; // converting kb to byte
        rnd.nextBytes(b);
        return b;
    }

    private ResponseEntity<ProblemDetails> sendHttpRequest(final String url) {
        final String testURL = localhostUrl + port + VNFPKGM_BASE_URL + "/" + url;
        final HttpEntity<?> request = new HttpEntity<>(basicHttpHeadersProvider.getHttpHeaders());
        return restTemplate.withBasicAuth("test", "test").exchange(testURL, HttpMethod.GET, request,
                ProblemDetails.class);
    }

    private VnfPkgInfo[] createVnfPkgArray() {
        final VnfPkgInfo[] vnfPkgInfoArray = new VnfPkgInfo[1];
        final VnfPkgInfo vnfPkgInfo = createVnfPkgInfo(VNF_PACKAGE_ID);
        vnfPkgInfoArray[0] = vnfPkgInfo;
        return vnfPkgInfoArray;
    }

    private VnfPkgInfo createVnfPkgInfo(final String vnfPackageId) {
        final VnfPkgInfo vnfPkgInfo = new VnfPkgInfo();
        vnfPkgInfo.setId(vnfPackageId);
        vnfPkgInfo.setVnfdId(VNFD_ID);
        vnfPkgInfo.setVnfProvider(VNF_PROVIDER);
        vnfPkgInfo.setVnfProductName(VNF_PRODUCT_NAME);
        vnfPkgInfo.setVnfSoftwareVersion(VNF_SOFTWARE_VERSION);
        vnfPkgInfo.setVnfdVersion(VNFD_VERSION);
        vnfPkgInfo.setChecksum(createVnfPkgChecksum());
        vnfPkgInfo.setSoftwareImages(createSoftwareImages());
        vnfPkgInfo.setAdditionalArtifacts(createAdditionalArtifacts());
        vnfPkgInfo.setLinks(createVNFPKGMLinkSerializerLinks());
        return vnfPkgInfo;
    }

    private Checksum createVnfPkgChecksum() {
        final Checksum checksum = new Checksum();
        checksum.setAlgorithm(ALGORITHM);
        checksum.setHash(HASH);
        return checksum;
    }

    private List<VnfPackageSoftwareImageInfo> createSoftwareImages() {
        final List<VnfPackageSoftwareImageInfo> softwareImages = new ArrayList<>();
        final VnfPackageSoftwareImageInfo vnfPackageSoftwareImageInfo = new VnfPackageSoftwareImageInfo();
        vnfPackageSoftwareImageInfo.setId(VNFD_ID);
        vnfPackageSoftwareImageInfo.setName(VNF_PRODUCT_NAME);
        vnfPackageSoftwareImageInfo.setProvider("");
        vnfPackageSoftwareImageInfo.setVersion("");
        vnfPackageSoftwareImageInfo.setChecksum(createVnfPkgChecksum());
        vnfPackageSoftwareImageInfo
                .setContainerFormat(VnfPackageSoftwareImageInfo.ContainerFormatEnum.fromValue("AKI"));
        softwareImages.add(vnfPackageSoftwareImageInfo);
        return softwareImages;
    }

    private List<VnfPackageArtifactInfo> createAdditionalArtifacts() {
        final List<VnfPackageArtifactInfo> vnfPackageArtifactInfos = new ArrayList<>();
        final VnfPackageArtifactInfo vnfPackageArtifactInfo =
                new VnfPackageArtifactInfo().artifactPath(ARTIFACT_PATH).checksum(createVnfPkgChecksum());
        vnfPackageArtifactInfos.add(vnfPackageArtifactInfo);
        return vnfPackageArtifactInfos;
    }

    private VNFPKGMLinkSerializer createVNFPKGMLinkSerializerLinks() {
        final UriLink uriLink = new UriLink().href(URI_HREF);
        final VNFPKGMLinkSerializer vnfpkgmLinkSerializer = new VNFPKGMLinkSerializer().self(uriLink);
        return vnfpkgmLinkSerializer;
    }
}
