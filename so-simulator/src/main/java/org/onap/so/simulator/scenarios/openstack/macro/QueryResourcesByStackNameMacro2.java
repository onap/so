package org.onap.so.simulator.scenarios.openstack.macro;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.consol.citrus.simulator.scenario.AbstractSimulatorScenario;
import com.consol.citrus.simulator.scenario.Scenario;
import com.consol.citrus.simulator.scenario.ScenarioDesigner;

@Scenario("Openstack-Query-Stack-Resources-Macro2")
@RequestMapping(value = "/sim/mockPublicUrl/stacks/macro_module_2/resources", method = RequestMethod.GET)
public class QueryResourcesByStackNameMacro2 extends AbstractSimulatorScenario {


    @Override
    public void run(ScenarioDesigner scenario) {
        scenario.http().receive().get();

        scenario.http().send().response(HttpStatus.OK)
                .payload(new ClassPathResource("openstack/gr_api/GetStackResourcesMacro.json"));

    }

}
