package org.onap.so.simulator.scenarios.openstack;

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.dsl.MessageSupport.MessageHeaderSupport.fromHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.citrusframework.endpoint.resolver.DynamicEndpointUriResolver;
import org.citrusframework.simulator.scenario.AbstractSimulatorScenario;
import org.citrusframework.simulator.scenario.Scenario;
import org.citrusframework.simulator.scenario.ScenarioRunner;
import org.citrusframework.spi.Resources;

@Scenario("Double-Failure-Stack-Endpoint")
@RequestMapping(value = "/sim/v1/tenantOne/stacks/double_failure_id/stackId")
public class QueryStackByIdDoubleFailure extends AbstractSimulatorScenario {


    @Override
    public void run(ScenarioRunner scenario) {
        // Create Poll Service
        scenario.getScenarioEndpoint().getEndpointConfiguration().setTimeout(300000L);
        scenario.$(scenario.http().receive().get().extract(
                fromHeaders().expression(DynamicEndpointUriResolver.REQUEST_PATH_HEADER_NAME, "correlationId")));
        scenario.$(echo("${correlationId}")); // step 2
        correlation().start().onHeader(DynamicEndpointUriResolver.REQUEST_PATH_HEADER_NAME, "${correlationId}");

        scenario.variable("stackName", "double_failure_id");
        scenario.variable("cloudOwner", "cloudOwner");
        scenario.variable("cloudRegion", "regionTwo");
        scenario.variable("tenantId", "872f331350c54e59991a8de2cbffb40c");
        scenario.variable("vServerId", "d29f3151-592d-4011-9356-ad047794e236");
        scenario.variable("stack_failure_message", "The Flavor ID (nd.c6r16d20) could not be found.");
        scenario.$(scenario.http().send().response(HttpStatus.OK).message() // step 4
                .body(Resources.fromClasspath("openstack/gr_api/Stack_Failure.json")));

        // Create Poll Retry
        scenario.$(scenario.http().receive().get()); // step 5
        scenario.$(scenario.http().send().response(HttpStatus.OK).message()
                .body(Resources.fromClasspath("openstack/gr_api/Stack_Failure.json")));

        // Rollback Poll
        scenario.$(scenario.http().receive().get()); // step 7
        scenario.$(scenario.http().send().response(HttpStatus.OK).message()
                .body(Resources.fromClasspath("openstack/gr_api/Stack_Failure.json")));

        // Rollback Poll Retry
        scenario.$(scenario.http().receive().get()); // step 9
        scenario.$(scenario.http().send().response(HttpStatus.OK).message()
                .body(Resources.fromClasspath("openstack/gr_api/Stack_Failure.json")));

        // Create Poll
        scenario.$(scenario.http().receive().get()); // step 11
        scenario.$(scenario.http().send().response(HttpStatus.OK).message()
                .body(Resources.fromClasspath("openstack/gr_api/Stack_Failure.json")));

        // Create Poll Retry
        scenario.$(scenario.http().receive().get());
        scenario.$(scenario.http().send().response(HttpStatus.OK).message()
                .body(Resources.fromClasspath("openstack/gr_api/Stack_Failure.json")));

        // Rollback Poll
        scenario.$(scenario.http().receive().get()); // step 15
        scenario.$(scenario.http().send().response(HttpStatus.OK).message()
                .body(Resources.fromClasspath("openstack/gr_api/Stack_Failure.json")));

        // Rollback Poll Retry
        scenario.$(scenario.http().receive().get()); // step 17
        scenario.$(scenario.http().send().response(HttpStatus.OK).message()
                .body(Resources.fromClasspath("openstack/gr_api/Stack_Failure.json")));

    }

}
