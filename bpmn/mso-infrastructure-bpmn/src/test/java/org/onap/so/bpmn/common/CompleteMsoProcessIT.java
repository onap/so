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

package org.onap.so.bpmn.common;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;
import org.onap.so.BaseIntegrationTest;
import org.onap.so.bpmn.core.WorkflowException;

/**
 * Unit test for CompleteMsoProcess.bpmn.
 */

public class CompleteMsoProcessIT extends BaseIntegrationTest {

    private static final String REQUEST_PATH = "/infraActiveRequests/STUW105_5002";

    private String executeFlow(String inputRequestFile) {
        wireMockServer.stubFor(get(urlEqualTo(REQUEST_PATH)).willReturn(aResponse()
                .withHeader("Content-Type", "application/json").withBody("{\"serviceInstanceId\":\"service-1\"}")));
        wireMockServer.stubFor(put(urlEqualTo(REQUEST_PATH)).willReturn(aResponse().withStatus(204)));
        wireMockServer.stubFor(get(urlEqualTo("/infraActiveRequests/")).willReturn(aResponse().withStatus(400)));
        Map<String, Object> variables = new HashMap<>();
        variables.put("CompleteMsoProcessRequest", inputRequestFile);
        variables.put("mso-request-id", UUID.randomUUID().toString());
        String processId = invokeSubProcess("CompleteMsoProcess", variables);
        Assert.assertEquals(1,
                historyService.createHistoricProcessInstanceQuery().processInstanceId(processId).finished().count());
        logEnd();
        return processId;
    }

    @Test
    public void msoCompletionRequestWithNotificationUrl_200() throws Exception {
        logStart();

        // Execute Flow
        String processId = executeFlow(gMsoCompletionRequestWithNotificationurl());
        assertCompletionUpdate(processId);
        logEnd();
    }

    @Test
    public void msoCompletionRequestWithNoNotificationurl() throws Exception {
        logStart();

        // Execute Flow
        String processId = executeFlow(gMsoCompletionRequestWithNoNotificationurl());
        assertCompletionUpdate(processId);
        logEnd();
    }

    @Test
    public void msoCompletionRequestWithNotificationurlNoRequestId() throws Exception {
        logStart();

        // Execute Flow
        String processId = executeFlow(gMsoCompletionRequestWithNotificationurlNoRequestId());
        assertDatabaseError(processId);
        logEnd();
    }

    @Test
    public void msoCompletionRequestWithNoNotificationurlNoRequestId() throws Exception {
        logStart();

        // Execute Flow
        String processId = executeFlow(gMsoCompletionRequestWithNoNotificationurlNoRequestId());
        assertDatabaseError(processId);
        logEnd();
    }

    private void assertCompletionUpdate(String processId) throws Exception {
        Assert.assertEquals(1, historyService.createHistoricActivityInstanceQuery().processInstanceId(processId)
                .activityId("EndEvent_1").count());
        Assert.assertEquals(0, historyService.createHistoricActivityInstanceQuery().processInstanceId(processId)
                .activityId("startEventExceptionCompleteMSO").count());
        wireMockServer.verify(1, getRequestedFor(urlEqualTo(REQUEST_PATH)));
        wireMockServer.verify(1, putRequestedFor(urlEqualTo(REQUEST_PATH)));
        JsonNode update = new ObjectMapper()
                .readTree(wireMockServer.findAll(putRequestedFor(urlEqualTo(REQUEST_PATH))).get(0).getBodyAsString());
        Assert.assertEquals("STUW105_5002", update.path("requestId").asText());
        Assert.assertEquals("COMPLETE", update.path("requestStatus").asText());
        Assert.assertEquals("Resource Completed Successfully", update.path("statusMessage").asText());
        Assert.assertEquals("service-1", update.path("serviceInstanceId").asText());
        Assert.assertEquals("BPMN", update.path("lastModifiedBy").asText());
        Assert.assertTrue(update.path("progress").isIntegralNumber());
        Assert.assertEquals(100, update.path("progress").asInt());
    }

    private void assertDatabaseError(String processId) {
        Object stored = historyService.createHistoricVariableInstanceQuery().processInstanceId(processId)
                .variableName("WorkflowException").singleResult().getValue();
        Assert.assertTrue(stored instanceof WorkflowException);
        WorkflowException error = (WorkflowException) stored;
        Assert.assertEquals("CompleteMsoProcess", error.getProcessKey());
        Assert.assertEquals(2000, error.getErrorCode());
        Assert.assertEquals("Internal Error - Occured in update infra request db", error.getErrorMessage());
        Assert.assertEquals(1, historyService.createHistoricActivityInstanceQuery().processInstanceId(processId)
                .activityId("endEventExceptionCompleteMSO").count());
        Assert.assertEquals(0, historyService.createHistoricActivityInstanceQuery().processInstanceId(processId)
                .activityId("EndEvent_1").count());
        wireMockServer.verify(1, getRequestedFor(urlEqualTo("/infraActiveRequests/")));
        Assert.assertTrue(wireMockServer.findAll(putRequestedFor(urlEqualTo(REQUEST_PATH))).isEmpty());
    }

    public String gMsoCompletionRequestWithNotificationurl() {
        String xml = ""
                + "<sdncadapterworkflow:MsoCompletionRequest xmlns:ns=\"http://openecomp.org/mso/request/types/v1\" xmlns:sdncadapterworkflow=\"http://ecomp.openecomp.org.com/mso/workflow/schema/v1\">"
                + "<ns:request-info><ns:request-id>STUW105_5002</ns:request-id>"
                + "			<ns:request-action>RequestAction</ns:request-action>"
                + "			<ns:request-sub-action>COMPLETE</ns:request-sub-action>" + "			<ns:source>SOURCE</ns:source>"
                + "			<ns:notification-url>https://t3nap1a1.snt.bst.bls.com:9004/sdncontroller-sdncontroller-inbound-ws-war/sdncontroller-sdncontroller-inbound-ws.wsdl</ns:notification-url>"
                + "			<ns:order-number>10205000</ns:order-number>" + "			<ns:order-version>1</ns:order-version>"
                + "</ns:request-info>"
                + "		<sdncadapterworkflow:mso-bpel-name>BPELNAME</sdncadapterworkflow:mso-bpel-name>"
                + "</sdncadapterworkflow:MsoCompletionRequest>";

        return xml;
    }



    public String gMsoCompletionRequestWithNoNotificationurl() {
        // Generated the below XML from ActiveVOS moduler ... Using the generate sample XML feature in ActiveVOS
        String xml = ""
                + "<sdncadapterworkflow:MsoCompletionRequest xmlns:ns=\"http://openecomp.org/mso/request/types/v1\" xmlns:sdncadapterworkflow=\"http://openecomp.org/mso/workflow/schema/v1\">"
                + "<ns:request-info><ns:request-id>STUW105_5002</ns:request-id>"
                + "			<ns:request-action>RequestAction</ns:request-action>"
                + "			<ns:request-sub-action>COMPLETE</ns:request-sub-action>" + "			<ns:source>SOURCE</ns:source>"
                + "			<ns:notification-url></ns:notification-url>"
                + "			<ns:order-number>10205000</ns:order-number>" + "			<ns:order-version>1</ns:order-version>"
                + "</ns:request-info>"
                + "		<sdncadapterworkflow:mso-bpel-name>BPELNAME</sdncadapterworkflow:mso-bpel-name>"
                + "</sdncadapterworkflow:MsoCompletionRequest>";

        return xml;
    }

    public String gMsoCompletionRequestWithNoNotificationurlNoRequestId() {
        // Generated the below XML from ActiveVOS moduler ... Using the generate sample XML feature in ActiveVOS
        String xml = ""
                + "<sdncadapterworkflow:MsoCompletionRequest xmlns:ns=\"http://openecomp.org/mso/request/types/v1\" xmlns:sdncadapterworkflow=\"http://openecomp.org/mso/workflow/schema/v1\">"
                + "<ns:request-info><ns:request-id></ns:request-id>"
                + "			<ns:request-action>RequestAction</ns:request-action>"
                + "			<ns:request-sub-action>COMPLETE</ns:request-sub-action>" + "			<ns:source>SOURCE</ns:source>"
                + "			<ns:notification-url></ns:notification-url>"
                + "			<ns:order-number>10205000</ns:order-number>" + "			<ns:order-version>1</ns:order-version>"
                + "</ns:request-info>"
                + "		<sdncadapterworkflow:mso-bpel-name>BPELNAME</sdncadapterworkflow:mso-bpel-name>"
                + "</sdncadapterworkflow:MsoCompletionRequest>";

        return xml;
    }

    public String gMsoCompletionRequestWithNotificationurlNoRequestId() {
        // Generated the below XML from ActiveVOS moduler ... Using the generate sample XML feature in ActiveVOS
        String xml = ""
                + "<sdncadapterworkflow:MsoCompletionRequest xmlns:ns=\"http://openecomp.org/mso/request/types/v1\" xmlns:sdncadapterworkflow=\"http://openecomp.org/mso/workflow/schema/v1\">"
                + "<ns:request-info><ns:request-id></ns:request-id>"
                + "			<ns:request-action>RequestAction</ns:request-action>"
                + "			<ns:request-sub-action>COMPLETE</ns:request-sub-action>" + "			<ns:source>SOURCE</ns:source>"
                + "			<ns:notification-url>https://t3nap1a1.snt.bst.bls.com:9004/sdncontroller-sdncontroller-inbound-ws-war/sdncontroller-sdncontroller-inbound-ws.wsdl</ns:notification-url>"
                + "			<ns:order-number>10205000</ns:order-number>" + "			<ns:order-version>1</ns:order-version>"
                + "</ns:request-info>"
                + "		<sdncadapterworkflow:mso-bpel-name>BPELNAME</sdncadapterworkflow:mso-bpel-name>"
                + "</sdncadapterworkflow:MsoCompletionRequest>";

        return xml;
    }
}

