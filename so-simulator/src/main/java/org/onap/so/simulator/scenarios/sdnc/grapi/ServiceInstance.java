package org.onap.so.simulator.scenarios.sdnc.grapi;

import static org.citrusframework.dsl.MessageSupport.MessageBodySupport.fromBody;
import org.citrusframework.spi.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.citrusframework.simulator.scenario.AbstractSimulatorScenario;
import org.citrusframework.simulator.scenario.Scenario;
import org.citrusframework.simulator.scenario.ScenarioRunner;

@Scenario("SDNC-GRAPI-ServiceInstance")
@RequestMapping(value = "/sim/restconf/operations/GENERIC-RESOURCE-API:service-topology-operation/",
        method = RequestMethod.POST)
public class ServiceInstance extends AbstractSimulatorScenario {

    @Override
    public void run(ScenarioRunner scenario) {
        scenario.$(scenario.http().receive().post()
                .extract(fromBody().expression("$.input.service-information.service-id", "serviceId"))
                .extract(fromBody().expression("$.input.service-request-input.service-instance-name", "serviceName"))
                .extract(fromBody().expression("$.input.sdnc-request-header.svc-action", "action")));

        scenario.variable("finalIndicator", "Y");
        scenario.variable("responseCode", "200");
        scenario.variable("responseMessage", "success");
        scenario.$(new ProcessSDNCAssignService());

        scenario.$(scenario.http().send().response(HttpStatus.OK).message().contentType("application/json")
                .body(Resources.fromClasspath("sdnc/gr-api/SDNCSuccess.json")));


    }


}
