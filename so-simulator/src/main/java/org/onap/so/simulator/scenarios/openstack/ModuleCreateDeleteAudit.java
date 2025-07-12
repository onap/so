package org.onap.so.simulator.scenarios.openstack;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import com.consol.citrus.endpoint.resolver.DynamicEndpointUriResolver;
import org.citrusframework.simulator.scenario.AbstractSimulatorScenario;
import org.citrusframework.simulator.scenario.Scenario;
import org.citrusframework.simulator.scenario.ScenarioDesigner;

@Scenario("Openstack-ModuleCreateDeleteAudit")
@RequestMapping(value = "/sim/mockPublicUrlThree/stacks/nc_dummy_id/stackId")
public class ModuleCreateDeleteAudit extends AbstractSimulatorScenario {


    @Override
    public void run(ScenarioDesigner scenario) {
        // Get to see if stack exists
        scenario.http().receive().get().extractFromHeader(DynamicEndpointUriResolver.REQUEST_PATH_HEADER_NAME,
                "correlationId");
        scenario.echo("${correlationId}");
        scenario.correlation().start().onHeader(DynamicEndpointUriResolver.REQUEST_PATH_HEADER_NAME,
                "${correlationId}");

        scenario.variable("stackName", "nc_dummy_id");
        scenario.variable("cloudOwner", "cloudOwner");
        scenario.variable("cloudRegion", "regionThree");
        scenario.variable("tenantId", "0422ffb57ba042c0800a29dc85ca70a3");
        scenario.variable("vServerId", "92272b67-d23f-42ca-87fa-7b06a9ec81f3");

        scenario.http().send().response(HttpStatus.OK)
                .payload(new ClassPathResource("openstack/gr_api/Stack_Created.json"));

        // Initial Get from Openstack Adapter prior to deletion of the stack
        scenario.http().receive().get();
        scenario.http().send().response(HttpStatus.OK)
                .payload(new ClassPathResource("openstack/gr_api/Stack_Created.json"));

        // Delete of the stack
        scenario.http().receive().delete();
        scenario.http().send().response(HttpStatus.NO_CONTENT);

        // Final Get from Openstack Adapter after the deletion of the stack
        scenario.http().receive().get();
        scenario.http().send().response(HttpStatus.OK)
                .payload(new ClassPathResource("openstack/gr_api/Stack_Deleted.json"));

    }

}
