package org.onap.so.bpmn.common;

import static org.assertj.core.api.Assertions.assertThat;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.onap.so.bpmn.mock.StubResponseDatabase.MockPostRequestDB;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.junit.Test;
import org.onap.so.BaseIntegrationTest;
import org.onap.so.adapters.sdncrest.SdncRestCallback;
import org.onap.so.bpmn.common.adapter.sdnc.CallbackHeader;
import org.onap.so.bpmn.common.adapter.sdnc.SDNCAdapterCallbackRequest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SoapRestCoexistenceIT extends BaseIntegrationTest {
    @LocalServerPort
    private int httpPort;

    @Test
    public void sdncHttpCallbackCorrelatesTheLegacyVariableThroughTheRealEndpoint() throws Exception {
        String key = "SdncHttpCallback" + UUID.randomUUID().toString().replace("-", "");
        String requestId = UUID.randomUUID().toString();
        var model = org.camunda.bpm.model.bpmn.Bpmn.createExecutableProcess(key).startEvent()
                .intermediateCatchEvent("callback").message("sdncAdapterCallbackRequest").userTask("afterCallback")
                .endEvent().done();
        Deployment deployment = processEngine.getRepositoryService().createDeployment()
                .addModelInstance("sdnc-http-callback.bpmn", model).deploy();
        try {
            ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceByKey(key,
                    Map.of("SDNCA_requestId", requestId));
            SdncRestCallback callback = new SdncRestCallback();
            callback.setRequestId(requestId);
            callback.setResponseCode("200");
            callback.setResponseMessage("OK & ready");
            callback.setRequestDataXml("<output><value>Gr\u00fc\u00dfe &amp; SO</value></output>");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<byte[]> entity = new HttpEntity<>(
                    new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsBytes(callback), headers);
            String url = "http://localhost:" + httpPort + "/mso/sdnc/callbacks/v1";

            assertThat(new TestRestTemplate().postForEntity(url, entity, String.class).getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(new TestRestTemplate("test", "test").postForEntity(url, entity, String.class).getStatusCode())
                    .isEqualTo(HttpStatus.NO_CONTENT);

            CallbackHeader legacyHeader = new CallbackHeader();
            legacyHeader.setRequestId(requestId);
            legacyHeader.setResponseCode(callback.getResponseCode());
            legacyHeader.setResponseMessage(callback.getResponseMessage());
            SDNCAdapterCallbackRequest legacy = new SDNCAdapterCallbackRequest();
            legacy.setCallbackHeader(legacyHeader);
            legacy.setRequestData(callback.getRequestDataXml());
            assertThat(processEngine.getRuntimeService().getVariable(instance.getId(), "sdncAdapterCallbackRequest"))
                    .isEqualTo(legacy.toString());
            assertThat(processEngine.getTaskService().createTaskQuery().processInstanceId(instance.getId())
                    .taskDefinitionKey("afterCallback").count()).isEqualTo(1);
        } finally {
            processEngine.getRepositoryService().deleteDeployment(deployment.getId(), true);
        }
    }

    @Test
    public void deleteNetworkFailureUpdateUsesRestAndPreservesFallout() throws Exception {
        verifyDeleteNetworkFailureUpdate(204, 2);
    }

    @Test
    public void deleteNetworkRequestDbFailureStillReachesFallout() throws Exception {
        verifyDeleteNetworkFailureUpdate(500, 1);
    }

    private void verifyDeleteNetworkFailureUpdate(int responseCode, int executions) throws Exception {
        String requestId = UUID.randomUUID().toString();
        String path = "/v1/request-db/infra-requests/" + requestId + "/updates";
        wireMockServer.stubFor(post(urlPathEqualTo(path)).willReturn(aResponse().withStatus(responseCode)));
        String key = "DeleteNetworkCompatibility" + requestId.replace("-", "");
        Deployment deployment = deploy("process/DeleteNetworkInstance.bpmn", key, "CallActivity_1ksm1dz");
        try {
            for (int attempt = 0; attempt < executions; attempt++) {
                Map<String, Object> values = new HashMap<>();
                values.put("DELNI_requestId", requestId);
                values.put("mso-request-id", requestId);
                values.put("WorkflowException", new org.onap.so.bpmn.core.WorkflowException("DeleteNetworkInstance",
                        5300, "SDNC failed & rolled back"));
                ProcessInstance instance = processEngine.getRuntimeService().createProcessInstanceByKey(key)
                        .setVariables(values).startBeforeActivity("ScriptTask_1akfe0b").execute();
                Map<String, Object> result = processEngine.getRuntimeService().getVariables(instance.getId());
                assertThat(result.get("DELNI_dbReturnCode")).isEqualTo(responseCode);
                assertThat(result.get("DELNI_deleteDBResponse")).isEqualTo("");
                assertThat((String) result.get("DELNI_FalloutHandlerRequest")).contains("5300",
                        "SDNC failed &amp; rolled back");
                wireMockServer.verify(attempt + 1, postRequestedFor(urlPathEqualTo(path))
                        .withHeader("Content-Type",
                                com.github.tomakehurst.wiremock.client.WireMock.containing("application/json"))
                        .withRequestBody(com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath(
                                "$.requestStatus", com.github.tomakehurst.wiremock.client.WireMock.equalTo("FAILED"))));
            }
        } finally {
            processEngine.getRepositoryService().deleteDeployment(deployment.getId(), true);
        }
    }

    @Test
    public void oldManualInstanceResumesAfterRestDefinitionDeployment() throws Exception {
        MockPostRequestDB(wireMockServer);
        String restPath = "/v1/request-db/infra-requests/rest-compatibility/updates";
        wireMockServer.stubFor(post(urlPathEqualTo(restPath)).willReturn(aResponse().withStatus(204)));
        String key = "ManualHandlingCompatibility" + UUID.randomUUID().toString().replace("-", "");
        Deployment legacy = deploy("soap-retirement/legacy-ManualHandling.bpmn.fixture", key);
        Deployment current = null;
        try {
            ProcessDefinition oldDefinition = processEngine.getRepositoryService().createProcessDefinitionQuery()
                    .deploymentId(legacy.getId()).singleResult();
            ProcessInstance oldInstance = processEngine.getRuntimeService().startProcessInstanceById(
                    oldDefinition.getId(), UUID.randomUUID().toString(), variables("soap-compatibility"));
            Task oldTask = processEngine.getTaskService().createTaskQuery().processInstanceId(oldInstance.getId())
                    .singleResult();
            assertThat(oldTask).isNotNull();

            current = deploy("subprocess/BuildingBlock/ManualHandling.bpmn", key);
            ProcessDefinition newDefinition = processEngine.getRepositoryService().createProcessDefinitionQuery()
                    .deploymentId(current.getId()).singleResult();
            assertThat(newDefinition.getVersion()).isGreaterThan(oldDefinition.getVersion());
            processEngine.getTaskService().complete(oldTask.getId(), Map.of("responseValue", "skip"));
            assertThat(processEngine.getRuntimeService().createProcessInstanceQuery()
                    .processInstanceId(oldInstance.getId()).count()).isZero();

            ProcessInstance newInstance = processEngine.getRuntimeService().startProcessInstanceById(
                    newDefinition.getId(), UUID.randomUUID().toString(), variables("rest-compatibility"));
            Task newTask = processEngine.getTaskService().createTaskQuery().processInstanceId(newInstance.getId())
                    .singleResult();
            assertThat(newTask).isNotNull();
            processEngine.getTaskService().complete(newTask.getId(), Map.of("responseValue", "skip"));
            assertThat(processEngine.getRuntimeService().createProcessInstanceQuery()
                    .processInstanceId(newInstance.getId()).count()).isZero();
            wireMockServer.verify(2, postRequestedFor(urlPathEqualTo("/dbadapters/RequestsDbAdapter")));
            wireMockServer.verify(2, postRequestedFor(urlPathEqualTo(restPath)));
        } finally {
            if (current != null) {
                processEngine.getRepositoryService().deleteDeployment(current.getId(), true);
            }
            processEngine.getRepositoryService().deleteDeployment(legacy.getId(), true);
        }
    }

    private Deployment deploy(String resource, String processKey) throws Exception {
        return deploy(resource, processKey, null);
    }

    private Deployment deploy(String resource, String processKey, String pauseBefore) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        Document definition;
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertThat(input).isNotNull();
            definition = factory.newDocumentBuilder().parse(input);
        }
        Element process = (Element) definition
                .getElementsByTagNameNS("http://www.omg.org/spec/BPMN/20100524/MODEL", "process").item(0);
        process.setAttribute("id", processKey);
        if (pauseBefore != null) {
            org.w3c.dom.NodeList elements =
                    process.getElementsByTagNameNS("http://www.omg.org/spec/BPMN/20100524/MODEL", "callActivity");
            for (int index = 0; index < elements.getLength(); index++) {
                Element activity = (Element) elements.item(index);
                if (pauseBefore.equals(activity.getAttribute("id"))) {
                    Element wait =
                            definition.createElementNS("http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn2:userTask");
                    wait.setAttribute("id", pauseBefore);
                    activity.getParentNode().replaceChild(wait, activity);
                    break;
                }
            }
        }
        Element plane = (Element) definition
                .getElementsByTagNameNS("http://www.omg.org/spec/BPMN/20100524/DI", "BPMNPlane").item(0);
        plane.setAttribute("bpmnElement", processKey);
        TransformerFactory transformers = TransformerFactory.newInstance();
        transformers.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        transformers.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        transformers.newTransformer().transform(new DOMSource(definition), new StreamResult(output));
        return processEngine.getRepositoryService().createDeployment()
                .addInputStream("compatibility.bpmn", new ByteArrayInputStream(output.toByteArray())).deploy();
    }

    private Map<String, Object> variables(String requestId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("msoRequestId", requestId);
        variables.put("isDebugLogEnabled", "true");
        variables.put("serviceType", "service");
        variables.put("vnfType", "vnf");
        variables.put("currentActivity", "activity");
        variables.put("workStep", "1");
        variables.put("failedActivity", "AAI");
        variables.put("vnfName", "vnf");
        variables.put("errorCode", "123");
        variables.put("errorText", "update failed");
        variables.put("validResponses", "Rollback");
        return variables;
    }
}
