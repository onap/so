package org.onap.so.simulator.scenarios.openstack.resources;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.citrusframework.simulator.scenario.AbstractSimulatorScenario;
import org.citrusframework.simulator.scenario.Scenario;
import org.citrusframework.simulator.scenario.ScenarioDesigner;

@Scenario("Query-Stack-Details-Role1-Sub0")
@RequestMapping(
        value = "/v1/tenantOne/stacks/tsbc0005vm002ssc001-ssc_1_subint_role1_port_0_subinterfaces-hlzdigtimzst-0-upfi5nhurk7y/f711be16-2654-4a09-b89d-0511fda20e81",
        method = RequestMethod.GET)
public class QueryStackDetailsRole1Sub0 extends AbstractSimulatorScenario {

    @Override
    public void run(ScenarioDesigner scenario) {
        scenario.http().receive().get();

        scenario.http().send().response(HttpStatus.OK)
                .payload(new ClassPathResource("openstack/gr_api/Role1SubInterface0.json"));

    }

}
