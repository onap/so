/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Wipro Limited. All rights reserved.
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

package org.onap.so.adapters.oof.rest;

import static org.mockito.Mockito.when;
import java.io.File;
import java.io.IOException;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.onap.so.adapters.oof.model.OofRequest;
import org.onap.so.adapters.oof.utils.OofUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class OofClientTest {

    @Autowired
    TestRestTemplate restTemplate;

    @MockBean
    OofUtils oofutils;

    @MockBean
    RestTemplate mockrestTemplate;

    @Before
    void prepareMocks() throws Exception {
        ResponseEntity<Object> responseEntity = new ResponseEntity<>(HttpStatus.OK);
        when(oofutils.getOofHttpHeaders()).thenReturn(new HttpHeaders());
        when(oofutils.getOofurl(Mockito.anyString())).thenReturn("oofurl");
        when(mockrestTemplate.postForEntity(Mockito.anyString(), Mockito.any(), Mockito.any()))
                .thenReturn(responseEntity);
    }

    @Test
    void callOofTest() throws Exception {
        OofRequest request = prepareOofRequest();
        System.out.println(request);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<OofRequest> entity = new HttpEntity<OofRequest>(request, headers);
        ResponseEntity<String> response = restTemplate.postForEntity("/so/adapters/oof/v1", entity, String.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    private OofRequest prepareOofRequest() throws IOException {
        File file = new ClassPathResource("testInputs/NsiSelectionOofRequest.json").getFile();
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(file, OofRequest.class);
    }


}
