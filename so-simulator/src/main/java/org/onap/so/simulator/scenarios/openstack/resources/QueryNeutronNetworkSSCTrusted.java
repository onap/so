package org.onap.so.simulator.scenarios.openstack.resources;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.citrusframework.simulator.scenario.AbstractSimulatorScenario;
import org.citrusframework.simulator.scenario.Scenario;
import org.citrusframework.simulator.scenario.ScenarioDesigner;

@Scenario("QueryNeutronNetworkSSCTrusted")
@RequestMapping(value = "/sim/v1/tenantOne/v2.0/ports/d2f51f82-0ec2-4581-bd1a-d2a82073e52b", method = RequestMethod.GET)
public class QueryNeutronNetworkSSCTrusted extends AbstractSimulatorScenario {


    @Override
    public void run(ScenarioDesigner scenario) {
        scenario.http().receive().get();

        scenario.variable("stackName", "dummy_id");

        scenario.http().send().response(HttpStatus.OK).header("ContentType", "application/json")
                .payload(new ClassPathResource("openstack/gr_api/GetNeutronNetworkSSCTrustedPort.json"));

    }

}
