package org.onap.so.simulator.scenarios.sdnc.grapi;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.citrusframework.simulator.scenario.AbstractSimulatorScenario;
import org.citrusframework.simulator.scenario.Scenario;
import org.citrusframework.simulator.scenario.ScenarioDesigner;

@Scenario("SDNC-GRAPI-QueryVnf")
@RequestMapping(
        value = "/sim/restconf/config/GENERIC-RESOURCE-API:services/service/*/service-data/vnfs/vnf/*/vnf-data/vnf-topology/",
        method = RequestMethod.GET)
public class QueryVNF extends AbstractSimulatorScenario {

    @Override
    public void run(ScenarioDesigner scenario) {
        scenario.http().receive().get();

        scenario.http().send().response(HttpStatus.OK).header("ContentType", "application/json")
                .payload(new ClassPathResource("sdnc/gr-api/SDNC_Query_Vnf.json"));
    }


}
