package org.onap.so.adapters.appc.orchestrator.service;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.onap.so.adapters.appc.orchestrator.client.ApplicationControllerCallback;
import org.onap.so.adapters.appc.orchestrator.client.ApplicationControllerSupport;
import org.onap.so.appc.orchestrator.service.beans.ApplicationControllerTaskRequest;
import org.onap.so.externaltasks.logging.AuditMDCSetup;
import org.onap.so.utils.ExternalTaskUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.onap.appc.client.lcm.model.Status;

@Component
public class ApplicationControllerTask extends ExternalTaskUtils {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationControllerTask.class);

    @Autowired
    public Environment env;

    @Autowired
    public ApplicationControllerTaskImpl applicationControllerTaskImpl;

    @Autowired
    public ApplicationControllerSupport applicationControllerSupport;

    @Autowired
    private AuditMDCSetup mdcSetup;

    protected void executeExternalTask(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        mdcSetup.setupMDC(externalTask);
        ApplicationControllerTaskRequest request = externalTask.getVariable("appcOrchestratorRequest");
        String msoRequestId = externalTask.getVariable("mso-request-id");
        logger.debug("Starting External Task for RequestId: {} ", msoRequestId);
        Status status = null;
        ApplicationControllerCallback listener =
                new ApplicationControllerCallback(externalTask, externalTaskService, applicationControllerSupport);

        try {
            status = applicationControllerTaskImpl.execute(msoRequestId, request, listener);
        } catch (Exception e) {
            logger.error("Error while calling appc", e.getMessage());
        }
    }

}
