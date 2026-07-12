package org.onap.so.simulator.scenarios.sdnc.grapi;

import org.citrusframework.simulator.scenario.AbstractSimulatorScenario;
import org.citrusframework.simulator.scenario.Scenario;
import org.citrusframework.simulator.scenario.ScenarioRunner;
import java.security.SecureRandom;
import org.citrusframework.spi.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Scenario("SDNC-GRAPI-QueryVFModule")
@RequestMapping(
        value = "/sim/restconf/config/GENERIC-RESOURCE-API:services/service/*/service-data/vnfs/vnf/*/vnf-data/vf-modules/vf-module/dummy_id/vf-module-data/vf-module-topology/",
        method = RequestMethod.GET)
public class QueryVFModuleGR extends AbstractSimulatorScenario {

    @Override
    public void run(ScenarioRunner scenario) {
        scenario.$(scenario.http().receive().get());
        int random = (new SecureRandom()).nextInt(50) + 1;

        scenario.variable("vfModuleName", "vfModuleName" + random);

        scenario.$(scenario.http().send().response(HttpStatus.OK).message().contentType("application/json")
                .body(Resources.fromClasspath("sdnc/gr-api/SDNC_Query_VfModule.json")));


    }


}
