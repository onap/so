package org.onap.so.simulator.scenarios.openstack.resources;

import org.citrusframework.simulator.scenario.AbstractSimulatorScenario;
import org.citrusframework.simulator.scenario.Scenario;
import org.citrusframework.simulator.scenario.ScenarioRunner;
import org.citrusframework.spi.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Scenario("QueryNeutronNetworkSSCHAPort0")
@RequestMapping(value = "/sim/v1/tenantOne/v2.0/ports/00bb8407-650e-48b5-b919-33b88d6f8fe3", method = RequestMethod.GET)
public class QueryNeutronNetworkSSCHAPort0 extends AbstractSimulatorScenario {


    @Override
    public void run(ScenarioRunner scenario) {
        scenario.$(scenario.http().receive().get());

        scenario.variable("stackName", "dummy_id");

        scenario.$(scenario.http().send().response(HttpStatus.OK).message().contentType("application/json")
                .body(Resources.fromClasspath("openstack/gr_api/GetNeutronNetworkSSCHAPort0.json")));

    }

}
