/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright © 2025 Deutsche Telekom AG Intellectual Property. All rights reserved.
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
package org.onap.aaiclient.spring.compat;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Collections;
import java.util.Properties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.aaiclient.client.aai.AAIProperties;
import org.onap.aaiclient.spring.configuration.AAIPropertiesImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import lombok.SneakyThrows;

@ExtendWith(MockitoExtension.class)
public class RestTemplateDefaultHeaderInterceptorTest {

    private static final String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
    private static final String propertiesFilePath = rootPath + "tmp.properties";
    private static AAIProperties aaiProperties;
    private static RestTemplateDefaultHeaderInterceptor interceptor;

    @Mock
    HttpRequest request;

    @Mock
    ClientHttpRequestExecution execution;

    @BeforeAll
    public static void setup() {
        aaiProperties = new AAIPropertiesImpl();
        interceptor = new RestTemplateDefaultHeaderInterceptor(aaiProperties);
    }

    @Test
    @SneakyThrows
    public void thatAuthHeadersCanBeOmitted() {
        var aaiProperties = new AAIPropertiesImpl();
        var interceptor = new RestTemplateDefaultHeaderInterceptor(aaiProperties);
        HttpHeaders headers = new HttpHeaders();
        when(request.getHeaders()).thenReturn(headers);

        interceptor.intercept(request, new byte[0], execution);
        assertEquals(Collections.singletonList("SO"), headers.get("X-FromAppId"));
    }

    @Test
    @SneakyThrows
    public void thatAuthHeadersCanBeSet() {

        Properties properties = new Properties();
        String aaiAuth =
                "2A11B07DB6214A839394AA1EC5844695F5114FC407FF5422625FB00175A3DCB8A1FF745F22867EFA72D5369D599BBD88DA8BED4233CF5586";
        String msoKey = "07a7159d3bf51a0e53be7a8f89699be7";
        properties.setProperty("aai.auth", aaiAuth);
        properties.setProperty("mso.msoKey", msoKey);
        properties.store(new FileWriter(propertiesFilePath), "");
        var aaiProperties = new AAIPropertiesImpl(propertiesFilePath);
        // aaiProperties.
        var interceptor = new RestTemplateDefaultHeaderInterceptor(aaiProperties);
        HttpHeaders headers = new HttpHeaders();
        when(request.getHeaders()).thenReturn(headers);

        interceptor.intercept(request, new byte[0], execution);
        assertEquals(Collections.singletonList("SO"), headers.get("X-FromAppId"));
        String base64Encoded = Base64.getEncoder().encodeToString("aai@aai.onap.org:demo123456!".getBytes());
        assertEquals(Collections.singletonList("Basic " + base64Encoded),
                headers.get(HttpHeaders.AUTHORIZATION.toString()));
    }

    @AfterAll
    @SneakyThrows
    public static void cleanup() {
        Path fileToDeletePath = Paths.get(propertiesFilePath);
        Files.delete(fileToDeletePath);
    }

}
