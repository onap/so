package org.onap.so.simulator.scenarios.sdnc.grapi;

import org.citrusframework.spi.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.citrusframework.simulator.scenario.AbstractSimulatorScenario;
import org.citrusframework.simulator.scenario.Scenario;
import org.citrusframework.simulator.scenario.ScenarioRunner;

@Scenario("SDNC-GRAPI-QueryVFModule-Macro3")
@RequestMapping(
        value = "/sim/restconf/config/GENERIC-RESOURCE-API:services/service/*/service-data/vnfs/vnf/*/vnf-data/vf-modules/vf-module/macro_module_3/vf-module-data/vf-module-topology/",
        method = RequestMethod.GET)
public class QueryVFModuleGR_Macro3 extends AbstractSimulatorScenario {

    @Override
    public void run(ScenarioRunner scenario) {
        scenario.$(scenario.http().receive().get());

        scenario.variable("vfModuleName", "macro_module_3");

        scenario.$(scenario.http().send().response(HttpStatus.OK).message().contentType("application/json")
                .body(Resources.fromClasspath("sdnc/gr-api/SDNC_Query_VfModule.json")));


    }


}
