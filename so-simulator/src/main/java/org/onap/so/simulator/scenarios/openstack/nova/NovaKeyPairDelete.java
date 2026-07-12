package org.onap.so.simulator.scenarios.openstack.nova;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.citrusframework.simulator.scenario.AbstractSimulatorScenario;
import org.citrusframework.simulator.scenario.Scenario;
import org.citrusframework.simulator.scenario.ScenarioRunner;

@Scenario("Nova-Keypair-Delete")
@RequestMapping(value = "/sim/v1/tenantOne/os-keypairs/*", method = RequestMethod.DELETE)
public class NovaKeyPairDelete extends AbstractSimulatorScenario {

    @Override
    public void run(ScenarioRunner scenario) {
        scenario.$(scenario.http().receive().delete());
        scenario.$(scenario.http().send().response(HttpStatus.NO_CONTENT));
    }
}
