package org.onap.so.adapters.tasks.audit;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.audit.beans.AuditInventory;
import org.onap.so.logging.tasks.AuditMDCSetup;
import org.onap.so.objects.audit.AAIObjectAuditList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuditQueryStackService extends AbstractAuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditQueryStackService.class);

    @Autowired
    protected HeatStackAudit heatStackAudit;

    @Autowired
    protected AuditDataService auditDataService;

    @Autowired
    public AuditMDCSetup mdcSetup;

    public AuditQueryStackService() {
        super();
    }

    public void executeExternalTask(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        mdcSetup.setupMDC(externalTask);
        AuditInventory auditInventory = externalTask.getVariable("auditInventory");
        boolean success = false;
        Map<String, Object> variables = new HashMap<>();
        try {
            Integer retryCount = externalTask.getRetries();
            logger.info("Executing External Task Query Audit Inventory. Audit Inventory: {} \n Retry Number: {}",
                    auditInventory.toString(), retryCount);

            Optional<AAIObjectAuditList> auditList = heatStackAudit.queryHeatStack(auditInventory.getCloudOwner(),
                    auditInventory.getCloudRegion(), auditInventory.getTenantId(), auditInventory.getHeatStackName());

            if (auditList.isPresent()) {
                mdcSetup.setResponseCode(ONAPLogConstants.ResponseStatus.COMPLETE.toString());
                success = true;
                auditDataService.writeStackDataToRequestDb(auditInventory, auditList.get());
            }
            mdcSetup.setElapsedTime();
            String externalTaskId = externalTask.getId();
            if (success) {
                externalTaskService.complete(externalTask, variables);
                mdcSetup.setResponseCode(ONAPLogConstants.ResponseStatus.COMPLETE.toString());
                logger.debug("The External Task {}  was Successful", externalTaskId);
                logger.info(ONAPLogConstants.Markers.EXIT, "Exiting");
                mdcSetup.clearClientMDCs();
            } else {
                if (retryCount == null) {
                    logger.debug("The External Task {} Failed. Setting Retries to Default Start Value: {}",
                            externalTaskId, getRetrySequence().length);
                    externalTaskService.handleFailure(externalTask, UNABLE_TO_FIND_V_SERVERS_IN_OPENSTACK,
                            UNABLE_TO_FIND_V_SERVERS_IN_OPENSTACK, getRetrySequence().length, 10000);
                } else if (retryCount == 1) {
                    externalTaskService.complete(externalTask, variables);
                    mdcSetup.setResponseCode(ONAPLogConstants.ResponseStatus.ERROR.toString());
                    logger.debug("The External Task {} Failed. All Retries Exhausted", externalTaskId);
                    logger.info(ONAPLogConstants.Markers.EXIT, "Exiting");
                    mdcSetup.clearClientMDCs();
                } else {
                    logger.debug("The External Task {} Failed. Decrementing Retries to {} , Retry Delay: ",
                            externalTaskId, retryCount - 1, calculateRetryDelay(retryCount));
                    externalTaskService.handleFailure(externalTask, UNABLE_TO_FIND_V_SERVERS_IN_OPENSTACK,
                            UNABLE_TO_FIND_V_SERVERS_IN_OPENSTACK, retryCount - 1, calculateRetryDelay(retryCount));
                }
                logger.debug("The External Task {} Failed", externalTaskId);
            }
        } catch (Exception e) {
            logger.error("Error during audit query of stack", e);
        }
    }
}
