package org.onap.so.bpmn.common.workflow.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.ArgumentMatchers.anyMap;
import java.util.HashMap;
import java.util.Map;
import jakarta.ws.rs.core.Response;
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
        variables.put("mso-request-id", requestIdMap);
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
