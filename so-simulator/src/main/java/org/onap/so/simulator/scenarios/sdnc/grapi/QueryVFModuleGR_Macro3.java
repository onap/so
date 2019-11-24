package org.onap.so.simulator.scenarios.sdnc.grapi;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.consol.citrus.simulator.scenario.AbstractSimulatorScenario;
import com.consol.citrus.simulator.scenario.Scenario;
import com.consol.citrus.simulator.scenario.ScenarioDesigner;

@Scenario("SDNC-GRAPI-QueryVFModule-Macro3")
@RequestMapping(
        value = "/sim/restconf/config/GENERIC-RESOURCE-API:services/service/*/service-data/vnfs/vnf/*/vnf-data/vf-modules/vf-module/macro_module_3/vf-module-data/vf-module-topology/",
        method = RequestMethod.GET)
public class QueryVFModuleGR_Macro3 extends AbstractSimulatorScenario {

    @Override
    public void run(ScenarioDesigner scenario) {
        scenario.http().receive().get();

        scenario.variable("vfModuleName", "macro_module_3");

        scenario.http().send().response(HttpStatus.OK).header("ContentType", "application/json")
                .payload(new ClassPathResource("sdnc/gr-api/SDNC_Query_VfModule.json"));


    }


}
