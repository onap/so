package org.onap.so.simulator.scenarios.openstack;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import com.consol.citrus.endpoint.resolver.DynamicEndpointUriResolver;
import org.citrusframework.simulator.scenario.AbstractSimulatorScenario;
import org.citrusframework.simulator.scenario.Scenario;
import org.citrusframework.simulator.scenario.ScenarioDesigner;

@Scenario("Double-Failure-Stack-Endpoint")
@RequestMapping(value = "/sim/v1/tenantOne/stacks/double_failure_id/stackId")
public class QueryStackByIdDoubleFailure extends AbstractSimulatorScenario {


    @Override
    public void run(ScenarioDesigner scenario) {
        // Create Poll Service
        scenario.scenarioEndpoint().getEndpointConfiguration().setTimeout(300000L);
        scenario.http().receive().get().extractFromHeader(DynamicEndpointUriResolver.REQUEST_PATH_HEADER_NAME,
                "correlationId");
        scenario.echo("${correlationId}"); // step 2
        scenario.correlation().start().onHeader(DynamicEndpointUriResolver.REQUEST_PATH_HEADER_NAME,
                "${correlationId}");

        scenario.variable("stackName", "double_failure_id");
        scenario.variable("cloudOwner", "cloudOwner");
        scenario.variable("cloudRegion", "regionTwo");
        scenario.variable("tenantId", "872f331350c54e59991a8de2cbffb40c");
        scenario.variable("vServerId", "d29f3151-592d-4011-9356-ad047794e236");
        scenario.variable("stack_failure_message", "The Flavor ID (nd.c6r16d20) could not be found.");
        scenario.http().send().response(HttpStatus.OK) // step 4
                .payload(new ClassPathResource("openstack/gr_api/Stack_Failure.json"));

        // Create Poll Retry
        scenario.http().receive().get(); // step 5
        scenario.http().send().response(HttpStatus.OK)
                .payload(new ClassPathResource("openstack/gr_api/Stack_Failure.json"));

        // Rollback Poll
        scenario.http().receive().get(); // step 7
        scenario.http().send().response(HttpStatus.OK)
                .payload(new ClassPathResource("openstack/gr_api/Stack_Failure.json"));

        // Rollback Poll Retry
        scenario.http().receive().get(); // step 9
        scenario.http().send().response(HttpStatus.OK)
                .payload(new ClassPathResource("openstack/gr_api/Stack_Failure.json"));

        // Create Poll
        scenario.http().receive().get(); // step 11
        scenario.http().send().response(HttpStatus.OK)
                .payload(new ClassPathResource("openstack/gr_api/Stack_Failure.json"));

        // Create Poll Retry
        scenario.http().receive().get();
        scenario.http().send().response(HttpStatus.OK)
                .payload(new ClassPathResource("openstack/gr_api/Stack_Failure.json"));

        // Rollback Poll
        scenario.http().receive().get(); // step 15
        scenario.http().send().response(HttpStatus.OK)
                .payload(new ClassPathResource("openstack/gr_api/Stack_Failure.json"));

        // Rollback Poll Retry
        scenario.http().receive().get(); // step 17
        scenario.http().send().response(HttpStatus.OK)
                .payload(new ClassPathResource("openstack/gr_api/Stack_Failure.json"));

    }

}
