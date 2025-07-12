package org.onap.so.simulator.scenarios.openstack.resources;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.citrusframework.simulator.scenario.AbstractSimulatorScenario;
import org.citrusframework.simulator.scenario.Scenario;
import org.citrusframework.simulator.scenario.ScenarioDesigner;

@Scenario("Openstack-Query-Stack-Resources")
@RequestMapping(value = {"/sim/v1/tenantOne/stacks/dummy_id/stackId/resources",
        "/sim/v1/tenantOne/stacks/base_module_id/stackId/resources",
        "/sim/v1/tenantOne/stacks/replace_module/stackId/resources",
        "/sim/v1/tenantOne/stacks/replace_module_volume_id/stackId/resources",
        "/sim/v1/tenantOne/stacks/macro_module_1/stackId/resources",
        "/sim/v1/tenantOne/stacks/macro_module_2/stackId/resources",
        "/sim/v1/tenantOne/stacks/macro_module_3/stackId/resources",
        "/sim/v1/tenantOne/stacks/created_success_id/stackId/resources",
        "/sim/v1/tenantOne/stacks/failure__success_id/stackId/resources",
        "/sim/v1/tenantOne/stacks/created_in_progress_id/stackId/resources"}, method = RequestMethod.GET)
public class QueryResourcesByStackName extends AbstractSimulatorScenario {

    @Override
    public void run(ScenarioDesigner scenario) {
        scenario.http().receive().get();

        scenario.http().send().response(HttpStatus.OK).header("ContentType", "application/json")
                .payload(new ClassPathResource("openstack/gr_api/zrdm52emccr01_base_resources.json"));

    }

}
