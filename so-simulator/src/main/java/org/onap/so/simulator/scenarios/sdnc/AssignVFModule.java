package org.onap.so.simulator.scenarios.sdnc;

import org.citrusframework.spi.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.citrusframework.simulator.scenario.AbstractSimulatorScenario;
import org.citrusframework.simulator.scenario.Scenario;
import org.citrusframework.simulator.scenario.ScenarioRunner;

@Scenario("SDNC-VNFAPI-AssignVFModule")
@RequestMapping(value = "/sim/restconf/operations/VNF-API:vnf-topology-operation", method = RequestMethod.POST)
public class AssignVFModule extends AbstractSimulatorScenario {

    @Override
    public void run(ScenarioRunner scenario) {
        scenario.$(scenario.http().receive().post());

        scenario.$(scenario.http().send().response(HttpStatus.OK).message()
                .body(Resources.fromClasspath("sdnc/vnf-api/AssignResponseSuccess.xml")));


    }


}
