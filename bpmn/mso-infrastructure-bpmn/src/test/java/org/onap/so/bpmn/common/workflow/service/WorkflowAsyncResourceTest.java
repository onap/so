package org.onap.so.bpmn.common.workflow.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.ArgumentMatchers.anyMap;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Response;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.bpmn.common.workflow.context.WorkflowResponse;

@RunWith(MockitoJUnitRunner.class)
public class WorkflowAsyncResourceTest {

    private static final String requestUrl =
            "http:localhost:6746/onap/so/infra/orchestrationRequests/v7/serviceInstance";
    private static final String requestBody = "";

    @InjectMocks
    @Spy
    private WorkflowAsyncResource workflowAsyncResource;

    @Mock
    private WorkflowProcessor processor;

    private WorkflowResponse workflowResponse;
    private VariableMapImpl varMap;

    @Before
    public void before() {
        workflowResponse = new WorkflowResponse();
        varMap = new VariableMapImpl();
        Map<String, Object> variables = new HashMap<String, Object>();
        Map<String, Object> requestIdMap = new HashMap<String, Object>();
        requestIdMap.put("value", "123");
        requestIdMap.put("type", "String");

        Map<String, Object> requestUriMap = new HashMap<String, Object>();
        requestUriMap.put("value", requestUrl);
        requestUriMap.put("type", "String");

        Map<String, Object> requestBodyMap = new HashMap<String, Object>();
        requestBodyMap.put("value", requestBody);
        requestBodyMap.put("type", "String");

        variables.put("mso-request-id", requestIdMap);
        variables.put("requestUri", requestUriMap);
        variables.put("bpmnRequest", requestBodyMap);
        varMap.put("variables", variables);
    }

    @Test
    public void startProcessInstanceByKey200Test() throws Exception {
        workflowResponse.setMessageCode(200);
        doReturn(workflowResponse).when(workflowAsyncResource).waitForResponse(anyMap());
        Response response = workflowAsyncResource.startProcessInstanceByKey("123", varMap);
        assertEquals(202, response.getStatus());
    }

    @Test
    public void startProcessInstanceByKey500Test() throws Exception {
        workflowResponse.setMessageCode(500);
        doReturn(workflowResponse).when(workflowAsyncResource).waitForResponse(anyMap());
        Response response = workflowAsyncResource.startProcessInstanceByKey("123", varMap);
        assertEquals(500, response.getStatus());
    }
}
