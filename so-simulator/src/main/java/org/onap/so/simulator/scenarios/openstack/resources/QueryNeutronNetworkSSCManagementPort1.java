package org.onap.so.simulator.scenarios.openstack.resources;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.citrusframework.simulator.scenario.AbstractSimulatorScenario;
import org.citrusframework.simulator.scenario.Scenario;
import org.citrusframework.simulator.scenario.ScenarioDesigner;

@Scenario("QueryNeutronNetworkSSCManagementPort1")
@RequestMapping(value = "/sim/v1/tenantOne/v2.0/ports/07f5b14c-147a-4d14-8c94-a9e94dbc097b", method = RequestMethod.GET)
public class QueryNeutronNetworkSSCManagementPort1 extends AbstractSimulatorScenario {


    @Override
    public void run(ScenarioDesigner scenario) {
        scenario.http().receive().get();

        scenario.variable("stackName", "dummy_id");

        scenario.http().send().response(HttpStatus.OK).header("ContentType", "application/json")
                .payload(new ClassPathResource("openstack/gr_api/GetNeutronNetworkSSCManagementPort1.json"));

    }

}
