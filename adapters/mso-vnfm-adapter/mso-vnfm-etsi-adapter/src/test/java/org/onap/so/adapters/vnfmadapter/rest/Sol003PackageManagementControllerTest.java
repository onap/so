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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.onap.so.adapters.vnfmadapter.Constants.PACKAGE_MANAGEMENT_BASE_URL;
import static org.onap.so.client.RestTemplateConfig.CONFIGURABLE_REST_TEMPLATE;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.adapters.vnfmadapter.VnfmAdapterApplication;
import org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.ProblemDetails;
import org.onap.so.configuration.rest.BasicHttpHeadersProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

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

    private MockRestServiceServer mockRestServer;
    private BasicHttpHeadersProvider basicHttpHeadersProvider;


    public Sol003PackageManagementControllerTest() {}

    @Before
    public void setUp() {
        MockRestServiceServer.MockRestServiceServerBuilder builder = MockRestServiceServer.bindTo(testRestTemplate);
        builder.ignoreExpectOrder(true);
        mockRestServer = builder.build();
        basicHttpHeadersProvider = new BasicHttpHeadersProvider();
    }

    @Test
    public void testGetPackageContent_ValidArray_Success() {
        byte[] responseArray = buildByteArrayWithRandomData(10);

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

    // The below 4 test methods are here to improve code coverage and provide a foundation for writing future tests
    @Test
    public void testGetVnfPackage_Not_Implemented() {
        final ResponseEntity<ProblemDetails> responseEntity = sendHttpRequest(VNF_PACKAGE_ID);
        assertEquals(HttpStatus.NOT_IMPLEMENTED, responseEntity.getStatusCode());
    }

    @Test
    public void testGetVnfPackages_Not_Implemented() {
        final ResponseEntity<ProblemDetails> responseEntity = sendHttpRequest("");
        assertEquals(HttpStatus.NOT_IMPLEMENTED, responseEntity.getStatusCode());
    }

    @Test
    public void testGetVnfd_Not_Implemented() {
        final ResponseEntity<ProblemDetails> responseEntity = sendHttpRequest(VNF_PACKAGE_ID + "/vnfd");
        assertEquals(HttpStatus.NOT_IMPLEMENTED, responseEntity.getStatusCode());
    }

    @Test
    public void testGetArtifact_Not_Implemented() {
        final ResponseEntity<ProblemDetails> responseEntity =
                sendHttpRequest(VNF_PACKAGE_ID + "/artifacts/" + ARTIFACT_PATH);
        assertEquals(HttpStatus.NOT_IMPLEMENTED, responseEntity.getStatusCode());
    }

    // Simply returns a byte array filled with random data, for use in the tests.
    private byte[] buildByteArrayWithRandomData(int sizeInKb) {
        final Random rnd = new Random();
        final byte[] b = new byte[sizeInKb * 1024]; // converting kb to byte
        rnd.nextBytes(b);
        return b;
    }

    private ResponseEntity<ProblemDetails> sendHttpRequest(String url) {
        final String testURL = localhostUrl + port + VNFPKGM_BASE_URL + "/" + url;
        final HttpEntity<?> request = new HttpEntity<>(basicHttpHeadersProvider.getHttpHeaders());
        return restTemplate.withBasicAuth("test", "test").exchange(testURL, HttpMethod.GET, request,
                ProblemDetails.class);
    }
}
