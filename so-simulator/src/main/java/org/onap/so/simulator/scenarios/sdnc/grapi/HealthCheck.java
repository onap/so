package org.onap.so.simulator.scenarios.sdnc.grapi;

import org.citrusframework.spi.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.citrusframework.simulator.scenario.AbstractSimulatorScenario;
import org.citrusframework.simulator.scenario.Scenario;
import org.citrusframework.simulator.scenario.ScenarioRunner;

@Scenario("Health-Check-SDNC")
@RequestMapping(value = "/sim/restconf/operations/SLI-API:healthcheck", method = RequestMethod.POST)
public class HealthCheck extends AbstractSimulatorScenario {

    @Override
    public void run(ScenarioRunner scenario) {
        scenario.$(scenario.http().receive().post());

        scenario.$(scenario.http().send().response(HttpStatus.OK).message().contentType("application/json")
                .body(Resources.fromClasspath("sdnc/HealthCheck.json")));
    }


}
