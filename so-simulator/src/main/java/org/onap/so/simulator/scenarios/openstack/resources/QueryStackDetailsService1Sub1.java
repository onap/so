package org.onap.so.simulator.scenarios.openstack.resources;

import org.citrusframework.simulator.scenario.AbstractSimulatorScenario;
import org.citrusframework.simulator.scenario.Scenario;
import org.citrusframework.simulator.scenario.ScenarioRunner;
import org.citrusframework.spi.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Scenario("Query-Stack-Details-service1-Sub1")
@RequestMapping(
        value = "/sim/v1/tenantOne/stacks/tsbc0005vm002ssc001-ssc_1_subint_service1_port_0_subinterfaces-dtmxjmny7yjz-1-fmn5laetg5cs/0d9cd813-2ae1-46c0-9ebb-48081f6cffbb",
        method = RequestMethod.GET)
public class QueryStackDetailsService1Sub1 extends AbstractSimulatorScenario {

    @Override
    public void run(ScenarioRunner scenario) {
        scenario.$(scenario.http().receive().get());

        scenario.$(scenario.http().send().response(HttpStatus.OK).message()
                .body(Resources.fromClasspath("openstack/gr_api/service1SubInterface1.json")));

    }

}
