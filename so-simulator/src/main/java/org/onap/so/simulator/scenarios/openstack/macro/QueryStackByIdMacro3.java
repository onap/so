package org.onap.so.simulator.scenarios.openstack.macro;

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.dsl.MessageSupport.MessageHeaderSupport.fromHeaders;
import org.citrusframework.endpoint.resolver.DynamicEndpointUriResolver;
import org.citrusframework.simulator.scenario.AbstractSimulatorScenario;
import org.citrusframework.simulator.scenario.Scenario;
import org.citrusframework.simulator.scenario.ScenarioRunner;
import org.citrusframework.spi.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * This scenario is used by the following test cases: Resume Service Instance Macro 3 Modules 1 To Complete.
 *
 */
@Scenario("Openstack-QueryStackByID-Macro3")
@RequestMapping(value = "/sim/v1/tenantOne/stacks/macro_module_3/*", method = RequestMethod.GET)
public class QueryStackByIdMacro3 extends AbstractSimulatorScenario {

    private static final String FILE_STACK_CREATED_PATH = "openstack/gr_api/Stack_Created.json";
    private static final String FILE_STACK_DELETED_PATH = "openstack/gr_api/Stack_Deleted.json";

    @Override
    public void run(ScenarioRunner scenario) {
        scenario.getScenarioEndpoint().getEndpointConfiguration().setTimeout(300000L);

        scenario.$(scenario.http().receive().get().extract(
                fromHeaders().expression(DynamicEndpointUriResolver.REQUEST_PATH_HEADER_NAME, "correlationId")));
        scenario.$(echo("${correlationId}"));
        correlation().start().onHeader(DynamicEndpointUriResolver.REQUEST_PATH_HEADER_NAME, "${correlationId}");

        scenario.variable("stackName", "macro_module_3");

        // Poll
        scenario.$(scenario.http().send().response(HttpStatus.OK).message()
                .body(Resources.fromClasspath(FILE_STACK_CREATED_PATH)));

        // Delete
        scenario.$(scenario.http().receive().get());
        scenario.$(scenario.http().send().response(HttpStatus.OK).message()
                .body(Resources.fromClasspath(FILE_STACK_CREATED_PATH)));

        scenario.$(scenario.http().receive().get());
        scenario.$(scenario.http().send().response(HttpStatus.OK).message()
                .body(Resources.fromClasspath(FILE_STACK_DELETED_PATH)));

        // Poll
        scenario.$(scenario.http().receive().get());
        scenario.$(scenario.http().send().response(HttpStatus.OK).message()
                .body(Resources.fromClasspath(FILE_STACK_DELETED_PATH)));

    }

}
