package org.onap.so.simulator.scenarios.openstack.resources;

import org.citrusframework.simulator.scenario.AbstractSimulatorScenario;
import org.citrusframework.simulator.scenario.Scenario;
import org.citrusframework.simulator.scenario.ScenarioRunner;
import org.citrusframework.spi.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Scenario("Openstack-Query-Stack-Resources-2")
@RequestMapping(value = "/sim/v1/tenantOne/stacks/dummy_id/resources", method = RequestMethod.GET)
public class QueryResourcesByStackName2 extends AbstractSimulatorScenario {


    @Override
    public void run(ScenarioRunner scenario) {
        scenario.$(scenario.http().receive().get());

        scenario.variable("stackName", "dummy_id");

        scenario.$(scenario.http().send().response(HttpStatus.OK).message()
                .body(Resources.fromClasspath("openstack/gr_api/GetStackResources.json")));

    }

}
