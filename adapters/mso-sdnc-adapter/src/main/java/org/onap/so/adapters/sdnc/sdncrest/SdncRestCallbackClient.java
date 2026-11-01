package org.onap.so.adapters.sdnc.sdncrest;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Objects;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.onap.so.adapters.sdncrest.SdncRestCallback;
import org.onap.so.client.ClientObservationRegistry;
import org.onap.so.logging.jaxrs.filter.SOSpringClientFilter;
import org.onap.so.logging.jaxrs.filter.SOSpringClientPayloadFilter;
import org.onap.so.utils.BasicAuth;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class SdncRestCallbackClient {
    private final Environment environment;
    private final ObjectMapper mapper;

    public SdncRestCallbackClient(Environment environment, ObjectMapper mapper) {
        this.environment = environment;
        this.mapper = mapper.copy().disable(DeserializationFeature.UNWRAP_ROOT_VALUE)
                .disable(SerializationFeature.WRAP_ROOT_VALUE);
    }

    public void send(String callbackUrl, SdncRestCallback callback) {
        Objects.requireNonNull(callback, "callback");
        Assert.hasText(callback.getRequestId(), "requestId is required");
        Assert.hasText(callback.getResponseCode(), "responseCode is required");
        URI uri = URI.create(callbackUrl);
        Assert.isTrue(("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())) && uri.getHost() != null
                && uri.getUserInfo() == null && uri.getFragment() == null, "callbackUrl must be an HTTP URL");
        String username = environment.getProperty(BasicAuth.SO_USERNAME_PROPERTY);
        String password = environment.getProperty(BasicAuth.SO_PASSWORD_PROPERTY);
        if (!StringUtils.hasText(username) || password == null) {
            throw new IllegalStateException("SO callback credentials are not configured");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        try (CloseableHttpClient httpClient =
                HttpClients.custom().disableAutomaticRetries().disableRedirectHandling().build()) {
            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
            factory.setConnectTimeout(60000);
            factory.setConnectionRequestTimeout(60000);
            factory.setReadTimeout(60000);
            RestTemplate template = new RestTemplate(new BufferingClientHttpRequestFactory(factory));
            template.setObservationRegistry(ClientObservationRegistry.get());
            template.getInterceptors().add(new SOSpringClientFilter());
            template.getInterceptors().add(new SOSpringClientPayloadFilter());
            template.getMessageConverters().stream().filter(MappingJackson2HttpMessageConverter.class::isInstance)
                    .map(MappingJackson2HttpMessageConverter.class::cast)
                    .forEach(converter -> converter.setObjectMapper(mapper));
            ResponseEntity<Void> response =
                    template.postForEntity(uri, new HttpEntity<>(callback, headers), Void.class);
            if (response.getStatusCode() != HttpStatus.NO_CONTENT) {
                throw new RestClientException("Unexpected SDNC callback status: " + response.getStatusCode().value());
            }
        } catch (IOException failure) {
            throw new ResourceAccessException("SDNC callback transport failed", failure);
        }
    }
}
