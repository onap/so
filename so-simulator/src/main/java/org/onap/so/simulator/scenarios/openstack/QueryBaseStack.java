package org.onap.so.simulator.scenarios.openstack;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.consol.citrus.endpoint.resolver.DynamicEndpointUriResolver;
import org.citrusframework.simulator.scenario.AbstractSimulatorScenario;
import org.citrusframework.simulator.scenario.Scenario;
import org.citrusframework.simulator.scenario.ScenarioDesigner;

@Scenario("Openstack-QueryBaseStackByName")
@RequestMapping(value = "/sim/v1/tenantOne/stacks/base_module_id/*", method = RequestMethod.GET)
public class QueryBaseStack extends AbstractSimulatorScenario {

    @Override
    public void run(ScenarioDesigner scenario) {
        // Get to see if stack exists
        scenario.http().receive().get().extractFromHeader(DynamicEndpointUriResolver.REQUEST_PATH_HEADER_NAME,
                "correlationId");
        scenario.echo("${correlationId}");
        scenario.correlation().start().onHeader(DynamicEndpointUriResolver.REQUEST_PATH_HEADER_NAME,
                "${correlationId}");

        scenario.variable("stackName", "base_module_id");
        scenario.variable("cloudOwner", "cloudOwner");
        scenario.variable("cloudRegion", "regionTwo");
        scenario.variable("tenantId", "872f331350c54e59991a8de2cbffb40c");
        scenario.variable("vServerId", "d29f3151-592d-4011-9356-ad047794e236");
        scenario.http().send().response(HttpStatus.OK)
                .payload(new ClassPathResource("openstack/gr_api/Stack_Created.json"));

    }

}
