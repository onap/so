package org.onap.so.simulator.scenarios.openstack.resources;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.citrusframework.simulator.scenario.AbstractSimulatorScenario;
import org.citrusframework.simulator.scenario.Scenario;
import org.citrusframework.simulator.scenario.ScenarioDesigner;

@Scenario("Query-Resource-Details-service1-Sub2")
@RequestMapping(
        value = "/sim/v1/tenantOne/stacks/tsbc0005vm002ssc001-ssc_1_subint_service1_port_0_subinterfaces-dtmxjmny7yjz-2-y3ndsavmsymv/bd0fc728-cbde-4301-a581-db56f494675c/resources",
        method = RequestMethod.GET)
public class QueryResourceDetailsService1Sub2 extends AbstractSimulatorScenario {

    @Override
    public void run(ScenarioDesigner scenario) {
        scenario.http().receive().get();

        scenario.http().send().response(HttpStatus.OK)
                .payload(new ClassPathResource("openstack/gr_api/service1SubInterface2Resources.json"));

    }

}
