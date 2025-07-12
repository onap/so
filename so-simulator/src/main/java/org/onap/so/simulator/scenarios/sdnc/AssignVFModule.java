package org.onap.so.simulator.scenarios.sdnc;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.citrusframework.simulator.scenario.AbstractSimulatorScenario;
import org.citrusframework.simulator.scenario.Scenario;
import org.citrusframework.simulator.scenario.ScenarioDesigner;

@Scenario("SDNC-VNFAPI-AssignVFModule")
@RequestMapping(value = "/sim/restconf/operations/VNF-API:vnf-topology-operation", method = RequestMethod.POST)
public class AssignVFModule extends AbstractSimulatorScenario {

    @Override
    public void run(ScenarioDesigner scenario) {
        scenario.http().receive().post();

        scenario.http().send().response(HttpStatus.OK)
                .payload(new ClassPathResource("sdnc/vnf-api/AssignResponseSuccess.xml"));


    }


}
