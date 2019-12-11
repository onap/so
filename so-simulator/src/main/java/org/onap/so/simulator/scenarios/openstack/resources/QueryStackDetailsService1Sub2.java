package org.onap.so.simulator.scenarios.openstack.resources;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.consol.citrus.simulator.scenario.AbstractSimulatorScenario;
import com.consol.citrus.simulator.scenario.Scenario;
import com.consol.citrus.simulator.scenario.ScenarioDesigner;

@Scenario("Query-Stack-Details-service1-Sub2")
@RequestMapping(
        value = "/sim/mockPublicUrl/stacks/tsbc0005vm002ssc001-ssc_1_subint_service1_port_0_subinterfaces-dtmxjmny7yjz-2-y3ndsavmsymv/bd0fc728-cbde-4301-a581-db56f494675c",
        method = RequestMethod.GET)
public class QueryStackDetailsService1Sub2 extends AbstractSimulatorScenario {

    @Override
    public void run(ScenarioDesigner scenario) {
        scenario.http().receive().get();

        scenario.http().send().response(HttpStatus.OK)
                .payload(new ClassPathResource("openstack/gr_api/service1SubInterface2.json"));

    }

}
