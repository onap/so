package org.onap.so.simulator.scenarios.openstack;

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.dsl.MessageSupport.MessageHeaderSupport.fromHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.onap.so.simulator.actions.aai.DeleteVServers;
import org.citrusframework.endpoint.resolver.DynamicEndpointUriResolver;
import org.citrusframework.simulator.scenario.AbstractSimulatorScenario;
import org.citrusframework.simulator.scenario.Scenario;
import org.citrusframework.simulator.scenario.ScenarioRunner;
import org.citrusframework.spi.Resources;

@Scenario("Openstack-QueryBaseVfModuleStackById")
@RequestMapping(value = "/sim/v1/872f331350c54e59991a8de2cbffb40c/stacks/base_module_id/stackId")
public class QueryBaseVfModuleStackById extends AbstractSimulatorScenario {

    @Override
    public void run(ScenarioRunner scenario) {
        // Get to see if stack exists
        scenario.$(scenario.http().receive().get().extract(
                fromHeaders().expression(DynamicEndpointUriResolver.REQUEST_PATH_HEADER_NAME, "correlationId")));
        scenario.$(echo("${correlationId}"));
        correlation().start().onHeader(DynamicEndpointUriResolver.REQUEST_PATH_HEADER_NAME, "${correlationId}");

        scenario.variable("stackName", "base_module_id");
        scenario.variable("cloudOwner", "cloudOwner");
        scenario.variable("cloudRegion", "regionTwo");
        scenario.variable("tenantId", "872f331350c54e59991a8de2cbffb40c");
        scenario.variable("vServerId", "d29f3151-592d-4011-9356-ad047794e236");
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

        // Poll Deletion of stack for status
        scenario.$(scenario.http().receive().get());
        scenario.$(scenario.http().send().response(HttpStatus.OK).message()
                .body(Resources.fromClasspath("openstack/gr_api/Stack_Deleted.json")));
    }
}
