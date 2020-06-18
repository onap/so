package org.onap.so.bpmn.infrastructure.workflow.tasks.listeners;

import java.util.List;
import org.onap.so.bpmn.common.BBConstants;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.listener.flowmanipulator.FlowManipulator;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SkipConfigVnfListener implements FlowManipulator {

    private final CatalogDbClient catalogDbClient;

    public SkipConfigVnfListener(CatalogDbClient catalogDbClient) {
        this.catalogDbClient = catalogDbClient;
    }

    @Override
    public boolean shouldRunFor(String currentBBName, boolean isFirst, BuildingBlockExecution execution) {
        return "ConfigAssignVnfBB".equals(currentBBName) || "ConfigDeployVnfBB".equals(currentBBName);
    }

    @Override
    public void run(List<ExecuteBuildingBlock> flowsToExecute, ExecuteBuildingBlock currentBB,
            BuildingBlockExecution execution) {
        String vnfCustomizationUUID = currentBB.getBuildingBlock().getKey();

        List<VnfResourceCustomization> vnfResourceCustomizations = catalogDbClient
                .getVnfResourceCustomizationByModelUuid(currentBB.getRequestDetails().getModelInfo().getModelUuid());
        if (vnfResourceCustomizations != null && !vnfResourceCustomizations.isEmpty()) {
            VnfResourceCustomization vrc =
                    catalogDbClient.findVnfResourceCustomizationInList(vnfCustomizationUUID, vnfResourceCustomizations);
            boolean skipConfigVNF = vrc.isSkipPostInstConf();
            if (skipConfigVNF) {
                execution.setVariable(BBConstants.G_CURRENT_SEQUENCE,
                        ((int) execution.getVariable(BBConstants.G_CURRENT_SEQUENCE)) + 1);
            }
        }
    }



}
