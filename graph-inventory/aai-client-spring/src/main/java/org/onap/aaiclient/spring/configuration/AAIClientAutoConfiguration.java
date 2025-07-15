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
package org.onap.aaiclient.spring.configuration;

import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.UUID;
import org.onap.aaiclient.spring.AAIResourcesClient;
import org.onap.so.utils.CryptoUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableConfigurationProperties(AAIConfigurationProperties.class)
public class AAIClientAutoConfiguration {

    private static final String RESTTEMPLATE_QUALIFIER = "aaiClientRestTemplate";

    @Bean(name = RESTTEMPLATE_QUALIFIER)
    RestTemplate aaiClientRestTemplate(RestTemplateBuilder builder, AAIConfigurationProperties properties) {
        String rootUri = null; // null is also the default value inside
                               // the builder when rootUri is not specified
        try {
            rootUri = properties.getEndpoint().toString() + "/aai/" + properties.getDefaultVersion().toString();
        } catch (MalformedURLException e) {
            log.warn("aai.endpoint does not have a valid uri", e);
        }

        RestTemplateBuilder clientBuilder =
                builder.rootUri(rootUri).defaultHeader("X-FromAppId", properties.getSystemName())
                        .defaultHeader("X-TransactionId", UUID.randomUUID().toString());

        String auth = properties.getAuth();
        String key = properties.getKey();
        if (auth != null && key != null) {
            String base64Auth = decrypt(auth, key);
            return clientBuilder.defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + base64Auth).build();
        }
        var customMessageConverter = getJacksonMessageConverter();
        RestTemplate template = clientBuilder.build();
        // replace MappingJackson2HttpMessageConverter with custom one
        // without changing the other configured converters
        template.getMessageConverters()
                .removeIf(m -> m.getClass().getName().equals(MappingJackson2HttpMessageConverter.class.getName()));
        template.getMessageConverters().add(customMessageConverter);
        return template;
    }

    @Bean
    AAIResourcesClient aaiResourcesClient(@Qualifier(RESTTEMPLATE_QUALIFIER) RestTemplate restTemplate) {
        return new AAIResourcesClient(restTemplate);
    }

    private String decrypt(String auth, String key) {
        try {
            byte[] decryptedAuth = CryptoUtils.decrypt(auth, key).getBytes();
            return Base64.getEncoder().encodeToString(decryptedAuth);
        } catch (GeneralSecurityException e) {
            log.error(e.getMessage(), e);
            throw new IllegalArgumentException("AAI auth could not be decoded", e);
        }
    }

    private HttpMessageConverter<Object> getJacksonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        // do not include keys with null values when serializing to json
        // i.e "resource-version": null
        objectMapper.setSerializationInclusion(Include.NON_NULL);

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        return converter;
    }

}
