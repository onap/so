package org.onap.so.simulator.scenarios.sdnc.grapi;

import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aaiclient.client.aai.AAIObjectType;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;

public class ProcessSDNCAssignService extends AbstractTestAction {


    @Override
    public void doExecute(TestContext context) {
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
            e.printStackTrace();
        }

    }

}
