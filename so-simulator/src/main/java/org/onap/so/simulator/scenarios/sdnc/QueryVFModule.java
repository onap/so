
package org.onap.so.simulator.scenarios.sdnc;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.citrusframework.simulator.scenario.AbstractSimulatorScenario;
import org.citrusframework.simulator.scenario.Scenario;
import org.citrusframework.simulator.scenario.ScenarioDesigner;

@Scenario("SDNC-VNFAPI-QueryVFModule")
@RequestMapping(value = "/sim/restconf/config/VNF-API:vnfs/vnf-list/*", method = RequestMethod.GET)
public class QueryVFModule extends AbstractSimulatorScenario {

    @Override
    public void run(ScenarioDesigner scenario) {
        scenario.http().receive().get();

        scenario.http().send().response(HttpStatus.OK)
                .payload(new ClassPathResource("sdnc/vnf-api/QueryResponseSuccess.xml"));
    }

}
