package org.onap.so.simulator.scenarios.openstack.resources;

import org.citrusframework.simulator.scenario.AbstractSimulatorScenario;
import org.citrusframework.simulator.scenario.Scenario;
import org.citrusframework.simulator.scenario.ScenarioRunner;
import org.citrusframework.spi.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Scenario("Query-Stack-Details-service1-Sub0")
@RequestMapping(
        value = "/sim/v1/tenantOne/stacks/tsbc0005vm002ssc001-ssc_1_subint_service1_port_0_subinterfaces-dtmxjmny7yjz-0-yghihziaf36m/b7019dd0-2ee9-4447-bdef-ac25676b205a",
        method = RequestMethod.GET)
public class QueryStackDetailsService1Sub0 extends AbstractSimulatorScenario {

    @Override
    public void run(ScenarioRunner scenario) {
        scenario.$(scenario.http().receive().get());

        scenario.$(scenario.http().send().response(HttpStatus.OK).message()
                .body(Resources.fromClasspath("openstack/gr_api/service1SubInterface0.json")));

    }

}
