package org.onap.so.simulator.scenarios.openstack.macro;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.consol.citrus.endpoint.resolver.DynamicEndpointUriResolver;
import com.consol.citrus.simulator.scenario.AbstractSimulatorScenario;
import com.consol.citrus.simulator.scenario.Scenario;
import com.consol.citrus.simulator.scenario.ScenarioDesigner;

/**
 * This scenario is used by the following test cases: Resume Service Instance Macro 3 Modules 1 To Complete
 *
 */
@Scenario("Openstack-QueryStackByID-Macro1")
@RequestMapping(value = "/sim/mockPublicUrl/stacks/macro_module_1/*", method = RequestMethod.GET)
public class QueryStackByIdMacro1 extends AbstractSimulatorScenario {


    @Override
    public void run(ScenarioDesigner scenario) {
        scenario.scenarioEndpoint().getEndpointConfiguration().setTimeout(300000L);

        // Poll
        scenario.http().receive().get().extractFromHeader(DynamicEndpointUriResolver.REQUEST_PATH_HEADER_NAME,
                "correlationId");
        scenario.echo("${correlationId}");
        scenario.correlation().start().onHeader(DynamicEndpointUriResolver.REQUEST_PATH_HEADER_NAME,
                "${correlationId}");

        scenario.variable("stackName", "macro_module_1");

        scenario.http().send().response(HttpStatus.OK)
                .payload(new ClassPathResource("openstack/gr_api/Stack_Created.json"));

        // Create (module_2)
        scenario.http().receive().get();
        scenario.http().send().response(HttpStatus.OK)
                .payload(new ClassPathResource("openstack/gr_api/Stack_Created.json"));

        // Create (module_3)
        scenario.http().receive().get();
        scenario.http().send().response(HttpStatus.OK)
                .payload(new ClassPathResource("openstack/gr_api/Stack_Created.json"));

        // Create (module_2 recreate)
        scenario.http().receive().get();
        scenario.http().send().response(HttpStatus.OK)
                .payload(new ClassPathResource("openstack/gr_api/Stack_Created.json"));

        // Delete
        scenario.http().receive().get();
        scenario.http().send().response(HttpStatus.OK)
                .payload(new ClassPathResource("openstack/gr_api/Stack_Deleted.json"));

        // Delete
        scenario.http().receive().get();
        scenario.http().send().response(HttpStatus.OK)
                .payload(new ClassPathResource("openstack/gr_api/Stack_Deleted.json"));

        // Poll
        scenario.http().receive().get();
        scenario.http().send().response(HttpStatus.OK)
                .payload(new ClassPathResource("openstack/gr_api/Stack_Deleted.json"));
    }

}
