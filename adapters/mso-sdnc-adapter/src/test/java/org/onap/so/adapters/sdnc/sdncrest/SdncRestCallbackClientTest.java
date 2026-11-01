package org.onap.so.adapters.sdnc.sdncrest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.onap.so.adapters.sdncrest.SdncRestCallback;
import org.onap.so.utils.BasicAuth;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.web.client.RestClientException;

public class SdncRestCallbackClientTest {
    @Rule
    public WireMockRule server = new WireMockRule(wireMockConfig().dynamicPort());

    private final ObjectMapper mapper = new ObjectMapper().enable(DeserializationFeature.UNWRAP_ROOT_VALUE)
            .enable(SerializationFeature.WRAP_ROOT_VALUE);
    private final MockEnvironment environment =
            new MockEnvironment().withProperty(BasicAuth.SO_USERNAME_PROPERTY, "callback-user")
                    .withProperty(BasicAuth.SO_PASSWORD_PROPERTY, "synthetic-password");
    private SdncRestCallback callback;
    private SdncRestCallbackClient client;

    @Before
    public void prepareCallback() {
        callback = new SdncRestCallback();
        callback.setRequestId("operation-1");
        callback.setResponseCode("200");
        callback.setResponseMessage("OK");
        callback.setRequestDataXml("<output>Gr\u00fc\u00dfe &amp; SO</output>");
        client = new SdncRestCallbackClient(environment, mapper);
    }

    @Test
    public void deliversUnwrappedJsonWithAuthenticationWithoutMutatingTheSharedMapper() {
        server.stubFor(post(urlEqualTo("/callback")).willReturn(aResponse().withStatus(204)));

        client.send(url("/callback"), callback);

        server.verify(1, postRequestedFor(urlEqualTo("/callback"))
                .withHeader("Authorization", equalTo(BasicAuth.header("callback-user", "synthetic-password")))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("{\"requestId\":\"operation-1\",\"responseCode\":\"200\","
                        + "\"responseMessage\":\"OK\",\"requestDataXml\":\"<output>Gr\u00fc\u00dfe &amp; SO</output>\"}")));
        assertTrue(mapper.isEnabled(DeserializationFeature.UNWRAP_ROOT_VALUE));
        assertTrue(mapper.isEnabled(SerializationFeature.WRAP_ROOT_VALUE));
    }

    @Test
    public void rejectsEveryNonAcknowledgementWithoutRetry() {
        for (int status : new int[] {200, 202, 400, 401, 403, 500, 503}) {
            server.resetAll();
            server.stubFor(post(urlEqualTo("/callback")).willReturn(aResponse().withStatus(status)));

            assertThrows(RestClientException.class, () -> client.send(url("/callback"), callback));

            server.verify(1, postRequestedFor(urlEqualTo("/callback")));
        }
    }

    @Test
    public void doesNotFollowRedirectsOrForwardCredentials() {
        server.stubFor(post(urlEqualTo("/callback"))
                .willReturn(aResponse().withStatus(307).withHeader("Location", url("/redirect"))));
        server.stubFor(post(urlEqualTo("/redirect")).willReturn(aResponse().withStatus(204)));

        assertThrows(RestClientException.class, () -> client.send(url("/callback"), callback));

        server.verify(1, postRequestedFor(urlEqualTo("/callback")));
        server.verify(0, postRequestedFor(urlEqualTo("/redirect")));
    }

    @Test
    public void missingCredentialsPreventDelivery() {
        environment.setProperty(BasicAuth.SO_USERNAME_PROPERTY, "");

        assertThrows(IllegalStateException.class, () -> client.send(url("/callback"), callback));

        assertTrue(server.getAllServeEvents().isEmpty());
    }

    private String url(String path) {
        return "http://localhost:" + server.port() + path;
    }
}
