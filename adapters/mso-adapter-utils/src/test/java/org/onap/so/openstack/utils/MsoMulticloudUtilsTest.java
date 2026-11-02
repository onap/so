/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2022 Samsung Intellectual Property. All rights reserved.
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

package org.onap.so.openstack.utils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import static org.onap.so.openstack.utils.MsoMulticloudUtils.MULTICLOUD_QUERY_BODY_NULL;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import jakarta.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.onap.logging.filter.base.ONAPComponents;
import org.onap.so.BaseTest;
import org.onap.so.adapters.vdu.CloudInfo;
import org.onap.so.adapters.vdu.VduException;
import org.onap.so.adapters.vdu.VduInstance;
import org.onap.so.adapters.vdu.VduModelInfo;
import org.onap.so.adapters.vdu.VduStateType;
import org.onap.so.cloud.CloudConfig;
import org.onap.so.client.HttpClientFactory;
import org.onap.so.client.RestClient;
import org.onap.so.db.catalog.beans.CloudIdentity;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.openstack.beans.HeatStatus;
import org.onap.so.openstack.beans.StackInfo;
import org.onap.so.openstack.exceptions.MsoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.util.ReflectionTestUtils;

public class MsoMulticloudUtilsTest extends BaseTest {

    @Autowired
    private MsoMulticloudUtils multicloudUtils;

    @Autowired
    private ApplicationContext applicationContext;

    @InjectMocks
    private MsoMulticloudUtils multicloudUtilsMock;

    @Mock
    private CloudConfig cloudConfigMock;

    private static final String CREATE_STACK_RESPONSE = "{\"template_type\": \"heat\", \"workload_id\": "
            + "\"TEST-workload\", \"template_response\": {\"stack\": {\"id\": \"TEST-stack\", \"links\": []}}}";
    private static final String UPDATE_STACK_RESPONSE =
            "{\"template_type\": \"heat\", \"workload_id\": " + "\"TEST-workload\"}";

    private static final String MULTICLOUD_CREATE_PATH = "/api/multicloud/v1/CloudOwner/MTN14/infra_workload";
    private static final String MULTICLOUD_UPDATE_PATH =
            "/api/multicloud/v1/CloudOwner/MTN14/infra_workload/TEST-workload";
    private static final String MULTICLOUD_GET_PATH =
            "/api/multicloud/v1/CloudOwner/MTN14/infra_workload/TEST-workload";
    private static final String MULTICLOUD_DELETE_PATH =
            "/api/multicloud/v1/CloudOwner/MTN14/infra_workload/TEST-workload";

    private static final String AUTH_TEST_PATH = "/multicloud-auth-test";

    @Test
    public void absentMulticloudCredentialsDoNotSendAuthorization() {
        assertUnauthenticatedRequest(new MockEnvironment());
    }

    @Test
    public void emptyMulticloudCredentialsDoNotSendAuthorization() {
        assertUnauthenticatedRequest(new MockEnvironment().withProperty("multicloud.auth.username", "")
                .withProperty("multicloud.auth.password", ""));
    }

    @Test
    public void emptyUsernameAndAbsentPasswordDoNotSendAuthorization() {
        assertUnauthenticatedRequest(new MockEnvironment().withProperty("multicloud.auth.username", ""));
    }

    @Test
    public void absentUsernameAndEmptyPasswordDoNotSendAuthorization() {
        assertUnauthenticatedRequest(new MockEnvironment().withProperty("multicloud.auth.password", ""));
    }

    @Test
    public void multicloudCredentialsDoNotAuthenticateUnrelatedClients() throws Exception {
        assertAuthenticatedRequests(new MockEnvironment().withProperty("multicloud.auth.username", "user")
                .withProperty("multicloud.auth.password", "password"), "Basic dXNlcjpwYXNzd29yZA==");
        wireMockServer.stubFor(get(urlEqualTo("/unrelated-destination")).willReturn(aResponse().withStatus(204)));
        RestClient client = new HttpClientFactory().newJsonClient(
                new URL("http://localhost:" + wireMockPort + "/unrelated-destination"), ONAPComponents.AAI);
        try (Response response = client.get()) {
            assertEquals(204, response.getStatus());
        }
        wireMockServer.verify(getRequestedFor(urlEqualTo("/unrelated-destination"))
                .withHeader("Authorization", absent()).withHeader("Project", absent()));
    }

    @Test
    public void configuredMulticloudCredentialsAuthenticateEveryRequest() {
        assertAuthenticatedRequests(new MockEnvironment().withProperty("multicloud.auth.username", "user")
                .withProperty("multicloud.auth.password", "password"), "Basic dXNlcjpwYXNzd29yZA==");
    }

    @Test
    public void multicloudCredentialsPreserveUtf8AndPasswordSpecialCharacters() {
        assertAuthenticatedRequests(new MockEnvironment().withProperty("multicloud.auth.username", "us\u00e9r")
                .withProperty("multicloud.auth.password", "p\u00e4ss:$\"'"), "Basic dXPDqXI6cMOkc3M6JCIn");
    }

    @Test
    public void multicloudCredentialsResolveFromEnvironmentVariables() {
        MockEnvironment environment = new MockEnvironment();
        HashMap<String, Object> variables = new HashMap<>();
        variables.put("MULTICLOUD_AUTH_USERNAME", "user");
        variables.put("MULTICLOUD_AUTH_PASSWORD", "password");
        environment.getPropertySources().addFirst(new SystemEnvironmentPropertySource("auth-test", variables));
        assertAuthenticatedRequests(environment, "Basic dXNlcjpwYXNzd29yZA==");
    }

    @Test
    public void environmentCredentialsPreserveLiteralExpressions() {
        assertLiteralCredentials(true);
    }

    @Test
    public void propertyCredentialsPreserveLiteralExpressions() {
        assertLiteralCredentials(false);
    }

    private void assertLiteralCredentials(boolean environmentVariables) {
        String[] literals = {"literal${HOME}", "private${reviewMissingPlaceholder}suffix", "literal#{2*3}",
                "literal${HOME", "literal#{2*3"};
        for (String literal : literals) {
            wireMockServer.resetRequests();
            StandardEnvironment environment = new StandardEnvironment();
            environment.getPropertySources().addFirst(
                    new MapPropertySource("known-placeholder", Map.of("HOME", "resolved-home-must-not-be-used")));
            Map<String, Object> credentials = environmentVariables
                    ? Map.of("MULTICLOUD_AUTH_USERNAME", literal, "MULTICLOUD_AUTH_PASSWORD", literal)
                    : Map.of("multicloud.auth.username", literal, "multicloud.auth.password", literal);
            environment.getPropertySources()
                    .addFirst(environmentVariables
                            ? new SystemEnvironmentPropertySource("literal-credentials", credentials)
                            : new MapPropertySource("literal-credentials", credentials));
            String authorization = "Basic "
                    + Base64.getEncoder().encodeToString((literal + ":" + literal).getBytes(StandardCharsets.UTF_8));
            assertAuthenticatedRequests(environment, authorization);
        }
    }

    @Test
    public void invalidLiteralCredentialsFailWithoutEchoingSecrets() {
        String secret = "private${reviewMissingPlaceholder}suffix";
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new SystemEnvironmentPropertySource("literal-credentials",
                Map.of("MULTICLOUD_AUTH_USERNAME", "private:user", "MULTICLOUD_AUTH_PASSWORD", secret)));
        try {
            initializeMulticloudUtils(environment);
            fail("Invalid MultiCloud credentials must fail bean initialization");
        } catch (BeanCreationException exception) {
            assertTrue(exception.getMostSpecificCause() instanceof IllegalArgumentException);
            assertTrue(exception.getMostSpecificCause().getMessage().contains("multicloud.auth.username"));
            for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
                assertFalse(cause.toString().contains(secret));
                assertFalse(cause.toString().contains("private:user"));
            }
        }
        wireMockServer.verify(0, anyRequestedFor(urlEqualTo(AUTH_TEST_PATH)));
    }

    @Test
    public void invalidMulticloudCredentialsFailInitializationWithoutSendingRequests() {
        String[][] invalidCredentials = {{"private-user", null}, {null, "private-password"}, {"private-user", ""},
                {"", "private-password"}, {"private:user", "private-password"}, {" ", "private-password"},
                {"private-user", "\t "}, {" ", " "}, {"", " "}, {" ", ""}, {null, " "}, {" ", null},
                {"\u2003", "private-password"}, {"private-user", "\u2003"}};
        for (String[] credentials : invalidCredentials) {
            MockEnvironment environment = new MockEnvironment();
            if (credentials[0] != null) {
                environment.setProperty("multicloud.auth.username", credentials[0]);
            }
            if (credentials[1] != null) {
                environment.setProperty("multicloud.auth.password", credentials[1]);
            }
            try {
                initializeMulticloudUtils(environment);
                fail("Invalid MultiCloud credentials must fail bean initialization");
            } catch (BeanCreationException exception) {
                Throwable cause = exception.getMostSpecificCause();
                assertTrue(cause instanceof IllegalArgumentException);
                assertTrue(cause.getMessage().contains("multicloud.auth.username"));
                assertTrue(cause.getMessage().contains("multicloud.auth.password"));
                assertFalse(exception.toString().contains("private-user"));
                assertFalse(exception.toString().contains("private:user"));
                assertFalse(exception.toString().contains("private-password"));
            }
        }
        wireMockServer.verify(0, anyRequestedFor(urlEqualTo(AUTH_TEST_PATH)));
    }

    private MsoMulticloudUtils initializeMulticloudUtils(Environment environment) {
        MsoMulticloudUtils utils = new MsoMulticloudUtils();
        ReflectionTestUtils.setField(utils, "environment", environment);
        return (MsoMulticloudUtils) applicationContext.getAutowireCapableBeanFactory().initializeBean(utils,
                "multicloudAuthTest");
    }

    private RestClient authTestClient(Environment environment) {
        return ReflectionTestUtils.invokeMethod(initializeMulticloudUtils(environment), "getMulticloudClient",
                "http://localhost:" + wireMockPort + AUTH_TEST_PATH, "TEST-tenant");
    }

    private void assertUnauthenticatedRequest(Environment environment) {
        wireMockServer.stubFor(get(urlEqualTo(AUTH_TEST_PATH)).willReturn(aResponse().withStatus(204)));
        try (Response response = authTestClient(environment).get()) {
            assertEquals(204, response.getStatus());
        }
        wireMockServer.verify(getRequestedFor(urlEqualTo(AUTH_TEST_PATH)).withHeader("Authorization", absent())
                .withHeader("Project", equalTo("TEST-tenant")));
    }

    private void assertAuthenticatedRequests(Environment environment, String authorization) {
        wireMockServer.stubFor(get(urlEqualTo(AUTH_TEST_PATH)).willReturn(aResponse().withStatus(204)));
        wireMockServer.stubFor(post(urlEqualTo(AUTH_TEST_PATH)).willReturn(aResponse().withStatus(204)));
        wireMockServer.stubFor(delete(urlEqualTo(AUTH_TEST_PATH)).willReturn(aResponse().withStatus(204)));
        RestClient client = authTestClient(environment);
        try (Response response = client.get()) {
            assertEquals(204, response.getStatus());
        }
        try (Response response = client.post("{}")) {
            assertEquals(204, response.getStatus());
        }
        try (Response response = client.delete()) {
            assertEquals(204, response.getStatus());
        }
        wireMockServer.verify(getRequestedFor(urlEqualTo(AUTH_TEST_PATH))
                .withHeader("Authorization", equalTo(authorization)).withHeader("Project", equalTo("TEST-tenant")));
        wireMockServer.verify(postRequestedFor(urlEqualTo(AUTH_TEST_PATH))
                .withHeader("Authorization", equalTo(authorization)).withHeader("Project", equalTo("TEST-tenant")));
        wireMockServer.verify(deleteRequestedFor(urlEqualTo(AUTH_TEST_PATH))
                .withHeader("Authorization", equalTo(authorization)).withHeader("Project", equalTo("TEST-tenant")));
    }

    @Test
    public void createStackSuccess() throws MsoException {
        wireMockServer
                .stubFor(post(urlEqualTo(MULTICLOUD_CREATE_PATH)).inScenario("CREATE")
                        .willReturn(aResponse().withHeader("Content-Type", "application/json")
                                .withBody(CREATE_STACK_RESPONSE).withStatus(HttpStatus.SC_CREATED))
                        .willSetStateTo("CREATING"));
        wireMockServer.stubFor(get(urlPathEqualTo(MULTICLOUD_GET_PATH)).inScenario("CREATE")
                .whenScenarioStateIs("CREATING").willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("MulticloudGetCreateResponse.json").withStatus(HttpStatus.SC_OK)));
        wireMockServer.stubFor(post(urlPathEqualTo(MULTICLOUD_UPDATE_PATH))
                .inScenario("CREATE").willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody(UPDATE_STACK_RESPONSE).withStatus(HttpStatus.SC_ACCEPTED))
                .willSetStateTo("UPDATING"));
        wireMockServer.stubFor(get(urlEqualTo(MULTICLOUD_GET_PATH)).inScenario("CREATE").whenScenarioStateIs("UPDATING")
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("MulticloudGetUpdateResponse.json").withStatus(HttpStatus.SC_OK)));
        StackInfo result = multicloudUtils.createStack("MTN14", "CloudOwner", "TEST-tenant", "TEST-stack",
                new VduModelInfo(), "TEST-heat", new HashMap<>(), true, 200, "TEST-env", new HashMap<>(),
                new HashMap<>(), false, false);
        wireMockServer.resetScenarios();
        assertNotNull(result);
        assertEquals("TEST-stack", result.getName());
    }

    @Test
    public void deleteStack() throws MsoException {
        wireMockServer.stubFor(delete(urlEqualTo(MULTICLOUD_DELETE_PATH)).willReturn(
                aResponse().withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_NO_CONTENT)));
        wireMockServer.stubFor(get(urlEqualTo(MULTICLOUD_GET_PATH))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("MulticloudGetDeleteResponse.json").withStatus(HttpStatus.SC_OK)));
        StackInfo result =
                multicloudUtils.deleteStack("MTN14", "CloudOwner", "TEST-tenant", "TEST-stack/TEST-workload");
        assertNotNull(result);
        assertSame(HeatStatus.NOTFOUND, result.getStatus());
    }

    @Test
    public void queryStack() throws MsoException {
        StackInfo result = multicloudUtils.queryStack("MTN13", "CloudOwner", "TEST-tenant", "instanceId");
        assertSame(HeatStatus.NOTFOUND, result.getStatus());
    }

    @Test
    public void queryStackWithNullMulticloudQueryBody() throws MsoException {
        wireMockServer.stubFor(get(urlPathEqualTo("/api/multicloud/v1/CloudOwner/MTN13/infra_workload/instanceId"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(CREATE_STACK_RESPONSE)
                        .withStatus(HttpStatus.SC_OK)));

        StackInfo result = multicloudUtils.queryStack("MTN13", "CloudOwner", "TEST-tenant", "instanceName/instanceId");
        assertSame(HeatStatus.FAILED, result.getStatus());
        assertEquals(MULTICLOUD_QUERY_BODY_NULL, result.getStatusMessage());
    }

    @Test(expected = VduException.class)
    public void updateVdu() throws MsoException {
        multicloudUtils.updateVdu(new CloudInfo(), "instanceId", new HashMap<>(), new VduModelInfo(), false);
    }

    @Test
    public void deleteVdu() throws VduException {
        CloudInfo cloudInfo = new CloudInfo("cloudSiteId", "cloudOwner", "tenantId", "tenantName");
        VduInstance vduInstance = multicloudUtils.deleteVdu(cloudInfo, "instanceId", 3);
        assertNotNull(vduInstance);
        assertSame(VduStateType.DELETED, vduInstance.getStatus().getState());
    }

    @Ignore
    @Test
    public void createStackMulticloudClientIsNull() {
        try {
            multicloudUtilsMock.cloudConfig = cloudConfigMock;
            CloudSite cloudSite = new CloudSite();
            cloudSite.setIdentityService(new CloudIdentity());
            when(cloudConfigMock.getCloudSite("MTN13")).thenReturn(Optional.of(cloudSite));
            multicloudUtilsMock.createStack("MNT14", "CloudOwner", "TEST-tenant", "TEST-stack", new VduModelInfo(),
                    "TEST-heat", new HashMap<>(), false, 200, "TEST-env", new HashMap<>(), new HashMap<>(), false,
                    false);
        } catch (MsoException e) {
            assertEquals("0 : Multicloud client could not be initialized", e.toString());
            return;
        }
        fail("MsoOpenstackException expected!");
    }

    @Test
    public void createStackBadRequest() {
        try {
            wireMockServer.stubFor(post(urlPathEqualTo(MULTICLOUD_CREATE_PATH)).willReturn(
                    aResponse().withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_BAD_REQUEST)));
            multicloudUtils.createStack("MTN14", "CloudOwner", "TEST-tenant", "TEST-stack", new VduModelInfo(),
                    "TEST-heat", new HashMap<>(), false, 200, "TEST-env", new HashMap<>(), new HashMap<>(), false,
                    false);
        } catch (MsoException e) {
            assertEquals("0 : Bad Request", e.toString());
            return;
        }
        fail("MsoOpenstackException expected!");
    }

    @Test
    public void createStackEmptyResponseEntity() throws MsoException {
        wireMockServer.stubFor(post(urlPathEqualTo(MULTICLOUD_CREATE_PATH)).willReturn(aResponse()
                .withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_CREATED).withBody("{}")));
        StackInfo result = multicloudUtils.createStack("MTN14", "CloudOwner", "TEST-tenant", "TEST-stack",
                new VduModelInfo(), "TEST-heat", new HashMap<>(), false, 200, "TEST-env", new HashMap<>(),
                new HashMap<>(), false, false);
        assertNotNull(result);
        assertEquals("TEST-stack", result.getName());
    }
}
