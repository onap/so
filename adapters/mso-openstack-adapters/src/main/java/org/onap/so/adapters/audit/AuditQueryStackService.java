package org.onap.so.adapters.audit;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.onap.so.audit.beans.AuditInventory;
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

    protected void executeExternalTask(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        AuditInventory auditInventory = externalTask.getVariable("auditInventory");
        setupMDC(externalTask);
        boolean success = false;
        Map<String, Object> variables = new HashMap<>();
        try {
            logger.info("Executing External Task Query Audit Inventory. Audit Inventory: {} \n Retry Number: {}",
                    auditInventory.toString(), externalTask.getRetries());

            Optional<AAIObjectAuditList> auditList = heatStackAudit.queryHeatStack(auditInventory.getCloudOwner(),
                    auditInventory.getCloudRegion(), auditInventory.getTenantId(), auditInventory.getHeatStackName());

            if (auditList.isPresent()) {
                success = true;
                auditDataService.writeStackDataToRequestDb(auditInventory, auditList.get());
            }
            if (success) {
                externalTaskService.complete(externalTask, variables);
                logger.debug("The External Task {}  was Successful", externalTask.getId());
            } else {
                if (externalTask.getRetries() == null) {
                    logger.debug("The External Task {} Failed. Setting Retries to Default Start Value: {}",
                            externalTask.getId(), getRetrySequence().length);
                    externalTaskService.handleFailure(externalTask, UNABLE_TO_FIND_V_SERVERS_IN_OPENSTACK,
                            UNABLE_TO_FIND_V_SERVERS_IN_OPENSTACK, getRetrySequence().length, 10000);
                } else if (externalTask.getRetries() != null && externalTask.getRetries() - 1 == 0) {
                    logger.debug("The External Task {} Failed. All Retries Exhausted", externalTask.getId());
                    externalTaskService.complete(externalTask, variables);
                } else {
                    logger.debug("The External Task {} Failed. Decrementing Retries to {} , Retry Delay: ",
                            externalTask.getId(), externalTask.getRetries() - 1,
                            calculateRetryDelay(externalTask.getRetries()));
                    externalTaskService.handleFailure(externalTask, UNABLE_TO_FIND_V_SERVERS_IN_OPENSTACK,
                            UNABLE_TO_FIND_V_SERVERS_IN_OPENSTACK, externalTask.getRetries() - 1,
                            calculateRetryDelay(externalTask.getRetries()));
                }
                logger.debug("The External Task {} Failed", externalTask.getId());
            }
        } catch (Exception e) {
            logger.error("Error during audit query of stack", e);
        }
    }
}
