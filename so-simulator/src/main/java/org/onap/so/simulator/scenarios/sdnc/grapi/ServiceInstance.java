package org.onap.so.simulator.scenarios.sdnc.grapi;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.citrusframework.simulator.scenario.AbstractSimulatorScenario;
import org.citrusframework.simulator.scenario.Scenario;
import org.citrusframework.simulator.scenario.ScenarioDesigner;

@Scenario("SDNC-GRAPI-ServiceInstance")
@RequestMapping(value = "/sim/restconf/operations/GENERIC-RESOURCE-API:service-topology-operation/",
        method = RequestMethod.POST)
public class ServiceInstance extends AbstractSimulatorScenario {

    @Override
    public void run(ScenarioDesigner scenario) {
        scenario.http().receive().post().extractFromPayload("$.input.service-information.service-id", "serviceId")
                .extractFromPayload("$.input.service-request-input.service-instance-name", "serviceName")
                .extractFromPayload("$.input.sdnc-request-header.svc-action", "action");

        scenario.createVariable("finalIndicator", "Y");
        scenario.createVariable("responseCode", "200");
        scenario.createVariable("responseMessage", "success");
        scenario.action(new ProcessSDNCAssignService());

        scenario.http().send().response(HttpStatus.OK).header("ContentType", "application/json")
                .payload(new ClassPathResource("sdnc/gr-api/SDNCSuccess.json"));


    }


}
