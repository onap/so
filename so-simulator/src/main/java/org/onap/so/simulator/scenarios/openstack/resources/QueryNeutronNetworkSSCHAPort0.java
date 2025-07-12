package org.onap.so.simulator.scenarios.openstack.resources;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.citrusframework.simulator.scenario.AbstractSimulatorScenario;
import org.citrusframework.simulator.scenario.Scenario;
import org.citrusframework.simulator.scenario.ScenarioDesigner;

@Scenario("QueryNeutronNetworkSSCHAPort0")
@RequestMapping(value = "/sim/v1/tenantOne/v2.0/ports/00bb8407-650e-48b5-b919-33b88d6f8fe3", method = RequestMethod.GET)
public class QueryNeutronNetworkSSCHAPort0 extends AbstractSimulatorScenario {


    @Override
    public void run(ScenarioDesigner scenario) {
        scenario.http().receive().get();

        scenario.variable("stackName", "dummy_id");

        scenario.http().send().response(HttpStatus.OK).header("ContentType", "application/json")
                .payload(new ClassPathResource("openstack/gr_api/GetNeutronNetworkSSCHAPort0.json"));

    }

}
