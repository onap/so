/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.apihandlerinfra;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.junit.Test;
import org.onap.so.apihandlerinfra.tasksbeans.TaskList;
import org.onap.so.apihandlerinfra.tasksbeans.TasksGetResponse;
import org.onap.so.apihandlerinfra.tasksbeans.ValidResponses;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TasksHandlerTest extends BaseTest {

    private final String basePath = "onap/so/infra/tasks/v1";

    @Test
    public void getTasksTestByOriginalRequestId()
            throws ParseException, JSONException, JsonParseException, JsonMappingException, IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/sobpmnengine/task"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/GetTaskResponse.json").withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlPathEqualTo("/sobpmnengine/task/b5fa707a-f55a-11e7-a796-005056856d52/variables"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/GetTaskVariablesResponse.json").withStatus(HttpStatus.SC_OK)));

        String requestId = "4f6fe9ac-800c-4540-a93e-10d179fa1b0a";

        // expected response
        TasksGetResponse expectedResponse = new TasksGetResponse();

        List<TaskList> taskList = new ArrayList<TaskList>();
        TaskList taskList1 = new TaskList();
        List<String> validEntries = new ArrayList<String>();
        validEntries.add(ValidResponses.rollback.toString());
        validEntries.add(ValidResponses.skip.toString());
        validEntries.add(ValidResponses.manual.toString());
        validEntries.add(ValidResponses.abort.toString());
        taskList1.setBuildingBlockName("UpdateConfigurationState");
        taskList1.setBuildingBlockStep("Configurationactivate SDNO Post-Check");
        taskList1.setErrorCode("1002");
        taskList1.setErrorSource("SDNO");
        taskList1.setErrorMessage(
                "SDN-O exception: failed with message FAIL - AnsibleOperations exception: Failed : HTTP error code : 400 - Error Msg : no node list provided and no inventory file found");
        taskList1.setNfRole("VPROBE");
        taskList1.setType("fallout");
        taskList1.setOriginalRequestId(requestId);
        taskList1.setOriginalRequestorId("VID");
        taskList1.setSubscriptionServiceType("PORT-MIRROR");
        taskList1.setTaskId("b5fa707a-f55a-11e7-a796-005056856d52");
        taskList1.setDescription("test task");
        taskList1.setTimeout("PT3000S");
        taskList1.setValidResponses(validEntries);
        taskList.add(taskList1);

        expectedResponse.setTaskList(taskList);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON);
        headers.set("Content-Type", MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(basePath))
                .queryParam("taskId", "b5fa707a-f55a-11e7-a796-005056856d52");

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        ObjectMapper mapper = new ObjectMapper();

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


        // then
        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        TasksGetResponse realResponse = mapper.readValue(response.getBody(), TasksGetResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse));
        assertEquals("application/json", response.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0));
        assertEquals("0", response.getHeaders().get("X-MinorVersion").get(0));
        assertEquals("0", response.getHeaders().get("X-PatchVersion").get(0));
        assertEquals("1.0.0", response.getHeaders().get("X-LatestVersion").get(0));
    }

}
