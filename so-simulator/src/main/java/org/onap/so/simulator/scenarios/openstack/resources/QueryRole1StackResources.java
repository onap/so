package org.onap.so.simulator.scenarios.openstack.resources;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.consol.citrus.simulator.scenario.AbstractSimulatorScenario;
import com.consol.citrus.simulator.scenario.Scenario;
import com.consol.citrus.simulator.scenario.ScenarioDesigner;

@Scenario("QueryRole1StackResources-tsbc0005vm002ssc001")
@RequestMapping(
        value = "/sim/mockPublicUrl/stacks/tsbc0005vm002ssc001-ssc_1_subint_role1_port_0_subinterfaces-hlzdigtimzst/447a9b41-714e-434b-b1d0-6cce8d9f0f0c/resources",
        method = RequestMethod.GET)
public class QueryRole1StackResources extends AbstractSimulatorScenario {


    @Override
    public void run(ScenarioDesigner scenario) {
        scenario.http().receive().get();

        scenario.http().send().response(HttpStatus.OK)
                .payload(new ClassPathResource("openstack/gr_api/StackResourcesRole1ResourceGroup.json"));

    }

}
