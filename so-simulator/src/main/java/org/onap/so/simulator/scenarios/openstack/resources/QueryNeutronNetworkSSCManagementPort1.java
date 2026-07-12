package org.onap.so.simulator.scenarios.openstack.resources;

import org.citrusframework.simulator.scenario.AbstractSimulatorScenario;
import org.citrusframework.simulator.scenario.Scenario;
import org.citrusframework.simulator.scenario.ScenarioRunner;
import org.citrusframework.spi.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Scenario("QueryNeutronNetworkSSCManagementPort1")
@RequestMapping(value = "/sim/v1/tenantOne/v2.0/ports/07f5b14c-147a-4d14-8c94-a9e94dbc097b", method = RequestMethod.GET)
public class QueryNeutronNetworkSSCManagementPort1 extends AbstractSimulatorScenario {


    @Override
    public void run(ScenarioRunner scenario) {
        scenario.$(scenario.http().receive().get());

        scenario.variable("stackName", "dummy_id");

        scenario.$(scenario.http().send().response(HttpStatus.OK).message().contentType("application/json")
                .body(Resources.fromClasspath("openstack/gr_api/GetNeutronNetworkSSCManagementPort1.json")));

    }

}
