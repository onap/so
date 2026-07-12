package org.onap.so.simulator.scenarios.openstack.resources;

import org.citrusframework.simulator.scenario.AbstractSimulatorScenario;
import org.citrusframework.simulator.scenario.Scenario;
import org.citrusframework.simulator.scenario.ScenarioRunner;
import org.citrusframework.spi.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Scenario("QueryNeutronNetworkSSCTrusted")
@RequestMapping(value = "/sim/v1/tenantOne/v2.0/ports/d2f51f82-0ec2-4581-bd1a-d2a82073e52b", method = RequestMethod.GET)
public class QueryNeutronNetworkSSCTrusted extends AbstractSimulatorScenario {


    @Override
    public void run(ScenarioRunner scenario) {
        scenario.$(scenario.http().receive().get());

        scenario.variable("stackName", "dummy_id");

        scenario.$(scenario.http().send().response(HttpStatus.OK).message().contentType("application/json")
                .body(Resources.fromClasspath("openstack/gr_api/GetNeutronNetworkSSCTrustedPort.json")));

    }

}
