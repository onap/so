package org.onap.so.simulator.scenarios.openstack.resources;

import org.citrusframework.simulator.scenario.AbstractSimulatorScenario;
import org.citrusframework.simulator.scenario.Scenario;
import org.citrusframework.simulator.scenario.ScenarioRunner;
import org.citrusframework.spi.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Scenario("QueryNeutronNetworkSSCRole1Port0")
@RequestMapping(value = "/sim/v1/tenantOne/v2.0/ports/0594a2f2-7ea4-42eb-abc2-48ea49677fca", method = RequestMethod.GET)
public class QueryNeutronNetworkSSCRole1Port0 extends AbstractSimulatorScenario {


    @Override
    public void run(ScenarioRunner scenario) {
        scenario.$(scenario.http().receive().get());

        scenario.variable("stackName", "dummy_id");

        scenario.$(scenario.http().send().response(HttpStatus.OK).message().contentType("application/json")
                .body(Resources.fromClasspath("openstack/gr_api/GetNeutronNetworkSSCRole1Port0.json")));

    }

}
