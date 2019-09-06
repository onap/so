package org.onap.so.bpmn.cloudify;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

@RunWith(MockitoJUnitRunner.class)
public class InstallDelegateTest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8888);

    @Mock
    DelegateExecution execution;


    @Test
    public void TestInstall() throws Exception {
        CloudifyInstallBlueprintDelegate d = new CloudifyInstallBlueprintDelegate();
        when(execution.getVariable("InputCfy_blueprint")).thenReturn("InputCfy_blueprint",
                "tosca_definitions_version: cloudify_dsl_1_3\n" + "imports:\n"
                        + "   - http://www.getcloudify.org/spec/cloudify/4.5/types.yaml\n" + "node_templates:\n"
                        + "  node:\n" + "    type: cloudify.nodes.Root\n");
        when(execution.hasVariable("InputCfy_blueprint")).thenReturn(true);
        when(execution.getVariable("InputCfy_blueprint_yaml")).thenReturn("blueprint.yaml");
        when(execution.hasVariable("InputCfy_blueprint_yaml")).thenReturn(true);
        when(execution.getVariable("InputCfy_blueprint_name")).thenReturn("test");
        when(execution.hasVariable("InputCfy_blueprint_name")).thenReturn(true);

        Map<String, String> creds = new HashMap<>();
        creds.put("username", "admin");
        creds.put("password", "admin");
        creds.put("url", "http://localhost:8888");
        creds.put("tenant", "default_tenant");
        when(execution.getVariable("InputCfy_credentials")).thenReturn(creds);
        when(execution.hasVariable("InputCfy_credentials")).thenReturn(true);

        // wiremock
        stubFor(get(urlMatching("/.*")).willReturn(aResponse().withStatus(200)));
        stubFor(put(urlMatching("/.*deployments.*")).willReturn(aResponse().withStatus(200)));
        stubFor(put(urlMatching("/.*blueprints.*")).willReturn(aResponse().withBody("{\"id\":\"test\"}")
                .withHeader("content-type", "application/json").withStatus(200)));
        stubFor(get(urlMatching("/.*executions.*")).willReturn(aResponse().withBody(
                "{\"items\":[{\"id\": \"1\",\"blueprint_id\": \"test\",\"deployment_id\": \"test\",\"created_at\": \"2009-01-01\", \"status\": \"terminated\"}]}")
                .withHeader("content-type", "application/json").withStatus(200)));
        stubFor(get(urlMatching("/.*executions/[a-zA-Z0-9]*")).willReturn(aResponse().withBody(
                "{\"id\": \"1\",\"blueprint_id\": \"test\",\"deployment_id\": \"test\",\"created_at\": \"2009-01-01\", \"status\": \"terminated\"}")
                .withHeader("content-type", "application/json").withStatus(200)));
        stubFor(post(urlMatching("/.*executions.*")).willReturn(aResponse().withBody(
                "{\"id\": \"1\",\"blueprint_id\": \"test\",\"deployment_id\": \"test\",\"created_at\": \"2009-01-01\", \"status\": \"terminated\"}")
                .withHeader("content-type", "application/json").withStatus(200)));
        Thread.sleep(5000L);
        d.execute(execution);
    }

    @Test(expected = java.lang.Exception.class)
    public void TestMissingCreds() throws Exception {
        CloudifyInstallBlueprintDelegate d = new CloudifyInstallBlueprintDelegate();
        when(execution.getVariable("InputCfy_blueprint")).thenReturn("InputCfy_blueprint",
                "tosca_definitions_version: cloudify_dsl_1_3\n" + "imports:\n"
                        + "   - http://www.getcloudify.org/spec/cloudify/4.5/types.yaml\n" + "node_templates:\n"
                        + "  node:\n" + "    type: cloudify.nodes.Root\n");
        when(execution.hasVariable("InputCfy_blueprint")).thenReturn(true);
        when(execution.getVariable("InputCfy_blueprint_yaml")).thenReturn("blueprint.yaml");
        when(execution.hasVariable("InputCfy_blueprint_yaml")).thenReturn(true);
        when(execution.getVariable("InputCfy_blueprint_name")).thenReturn("test");
        when(execution.hasVariable("InputCfy_blueprint_name")).thenReturn(true);

        d.execute(execution);

    }
}
