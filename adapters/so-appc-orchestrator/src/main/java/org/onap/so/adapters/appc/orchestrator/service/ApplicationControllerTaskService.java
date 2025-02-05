package org.onap.so.adapters.appc.orchestrator.service;

import jakarta.annotation.PostConstruct;
import org.camunda.bpm.client.ExternalTaskClient;
import org.onap.so.utils.ExternalTaskServiceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class ApplicationControllerTaskService {

    @Autowired
    public Environment env;

    @Autowired
    private ApplicationControllerTask appcOrchestrator;

    @Autowired
    private ExternalTaskServiceUtils externalTaskServiceUtils;

    @PostConstruct
    public void appcOrchestrator() throws Exception {
        for (int i = 0; i < externalTaskServiceUtils.getMaxClients(); i++) {
            ExternalTaskClient client = externalTaskServiceUtils.createExternalTaskClient();
            client.subscribe("AppcService").lockDuration(604800000).handler(appcOrchestrator::executeExternalTask)
                    .open();
        }
    }

}
