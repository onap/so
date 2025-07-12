package org.onap.so.simulator.scenarios.openstack.macro;

import com.consol.citrus.endpoint.resolver.DynamicEndpointUriResolver;
import org.citrusframework.simulator.scenario.AbstractSimulatorScenario;
import org.citrusframework.simulator.scenario.Scenario;
import org.citrusframework.simulator.scenario.ScenarioDesigner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * This scenario is used by the following test cases: Resume Service Instance Macro 3 Modules 1 To Complete.
 *
 */
@Scenario("Openstack-QueryStackByID-Macro1")
@RequestMapping(value = "/sim/v1/tenantOne/stacks/macro_module_1/*", method = RequestMethod.GET)
public class QueryStackByIdMacro1 extends AbstractSimulatorScenario {

    private static final String FILE_STACK_CREATED_PATH = "openstack/gr_api/Stack_Created.json";
    private static final String FILE_STACK_DELETED_PATH = "openstack/gr_api/Stack_Deleted.json";

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

        scenario.http().send().response(HttpStatus.OK).payload(new ClassPathResource(FILE_STACK_CREATED_PATH));

        // Create (module_2)
        scenario.http().receive().get();
        scenario.http().send().response(HttpStatus.OK).payload(new ClassPathResource(FILE_STACK_CREATED_PATH));

        // Create (module_3)
        scenario.http().receive().get();
        scenario.http().send().response(HttpStatus.OK).payload(new ClassPathResource(FILE_STACK_CREATED_PATH));

        // Create (module_2 recreate)
        scenario.http().receive().get();
        scenario.http().send().response(HttpStatus.OK).payload(new ClassPathResource(FILE_STACK_CREATED_PATH));

        // Delete
        scenario.http().receive().get();
        scenario.http().send().response(HttpStatus.OK).payload(new ClassPathResource(FILE_STACK_DELETED_PATH));

        // Delete
        scenario.http().receive().get();
        scenario.http().send().response(HttpStatus.OK).payload(new ClassPathResource(FILE_STACK_DELETED_PATH));

        // Poll
        scenario.http().receive().get();
        scenario.http().send().response(HttpStatus.OK).payload(new ClassPathResource(FILE_STACK_DELETED_PATH));
    }

}
