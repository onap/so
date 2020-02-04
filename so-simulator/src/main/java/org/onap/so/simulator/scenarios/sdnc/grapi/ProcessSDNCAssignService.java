package org.onap.so.simulator.scenarios.sdnc.grapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;

public class ProcessSDNCAssignService extends AbstractTestAction {

    @Override
    public void doExecute(TestContext context) {
        final Logger logger = LoggerFactory.getLogger(ProcessSDNCAssignService.class);

        try {
            String serviceName = context.getVariable("serviceName");
            String action = context.getVariable("action");
            if (("Robot_SI_For_Service_Failure".equals(serviceName) && "assign".equals(action))
                    || ("Robot_SI_For_Service_Rollback_Failure".equals(serviceName))) {
                context.setVariable("responseMessage", "SDNC is throwing errors");
                context.setVariable("responseCode", "500");
            } else {
                context.setVariable("responseMessage", "Success");
                context.setVariable("responseCode", "200");
            }

        } catch (Exception e) {
            logger.debug("Exception in ProcessSDNCAssignService.doExecute", e);
        }

    }

}
