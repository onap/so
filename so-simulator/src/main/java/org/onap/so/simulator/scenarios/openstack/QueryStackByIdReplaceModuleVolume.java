package org.onap.so.simulator.scenarios.openstack;

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.dsl.MessageSupport.MessageHeaderSupport.fromHeaders;
import org.onap.so.simulator.actions.aai.DeleteVServers;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.citrusframework.endpoint.resolver.DynamicEndpointUriResolver;
import org.citrusframework.simulator.scenario.AbstractSimulatorScenario;
import org.citrusframework.simulator.scenario.Scenario;
import org.citrusframework.simulator.scenario.ScenarioRunner;
import org.citrusframework.spi.Resources;

@Scenario("Openstack-Replace-VF-Module-Volume-Get")
@RequestMapping(value = "/sim/v1/tenantOne/stacks/replace_module_volume_id/*")
public class QueryStackByIdReplaceModuleVolume extends AbstractSimulatorScenario {


    @Override
    public void run(ScenarioRunner scenario) {
        // Get to see if stack exists
        scenario.$(scenario.http().receive().get().extract(
                fromHeaders().expression(DynamicEndpointUriResolver.REQUEST_PATH_HEADER_NAME, "correlationId")));
        scenario.$(echo("${correlationId}"));
        correlation().start().onHeader(DynamicEndpointUriResolver.REQUEST_PATH_HEADER_NAME, "${correlationId}");

        scenario.variable("stackName", "replace_module_volume_id");
        scenario.variable("cloudOwner", "cloudOwner");
        scenario.variable("cloudRegion", "regionOne");
        scenario.variable("tenantId", "0422ffb57ba042c0800a29dc85ca70f8");
        scenario.variable("vServerId", "92272b67-d23f-42ca-87fa-7b06a9ec81f3");

        scenario.$(scenario.http().send().response(HttpStatus.OK).message()
                .body(Resources.fromClasspath("openstack/gr_api/Stack_Created.json")));

        // Initial Get from Openstack Adapter prior to deletion of the stack
        scenario.$(scenario.http().receive().get());
        scenario.$(scenario.http().send().response(HttpStatus.OK).message()
                .body(Resources.fromClasspath("openstack/gr_api/Stack_Created.json")));

        // Delete of the stack
        scenario.$(scenario.http().receive().delete());
        scenario.$(new DeleteVServers());
        scenario.$(scenario.http().send().response(HttpStatus.NO_CONTENT));

        // Final Get from Openstack Adapter after the deletion of the stack
        scenario.$(scenario.http().receive().get());
        scenario.$(scenario.http().send().response(HttpStatus.OK).message()
                .body(Resources.fromClasspath("openstack/gr_api/Stack_Deleted.json")));
    }

}
