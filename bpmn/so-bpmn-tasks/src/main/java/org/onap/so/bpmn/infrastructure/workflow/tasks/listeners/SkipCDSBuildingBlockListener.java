package org.onap.so.bpmn.infrastructure.workflow.tasks.listeners;

import java.util.List;

import org.onap.so.bpmn.common.BBConstants;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.listener.flowmanipulator.FlowManipulator;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.springframework.beans.factory.annotation.Autowired;

public class SkipCDSBuildingBlockListener implements FlowManipulator {
	@Autowired
    private CatalogDbClient catalogDbClient;
	@Override
	public boolean shouldRunFor(String currentBBName, boolean isFirst, BuildingBlockExecution execution) {
		
		 return "GenericCDSBB".equals(currentBBName);
	}

	@Override
	public void run(List<ExecuteBuildingBlock> flowsToExecute, ExecuteBuildingBlock currentBB,
			BuildingBlockExecution execution) {
		String customizationUUID = currentBB.getBuildingBlock().getKey();
		        // String vnfCustomizationUUID = currentBB.getBuildingBlock().getKey();
		        // check for vnf scope
		        if (currentBB.getBuildingBlock().getBpmnScope().equals("VNF")) {
		            List<VnfResourceCustomization> vnfResourceCustomizations =
		                    catalogDbClient.getVnfResourceCustomizationByModelUuid(
		                            currentBB.getRequestDetails().getModelInfo().getModelUuid());
		            if (vnfResourceCustomizations != null && !vnfResourceCustomizations.isEmpty()) {
		                VnfResourceCustomization vrc = catalogDbClient.findVnfResourceCustomizationInList(customizationUUID,
		                        vnfResourceCustomizations);
		                boolean skipConfigVNF = vrc.isSkipPostInstConf();
		                if (skipConfigVNF) {
		                    execution.setVariable(BBConstants.G_CURRENT_SEQUENCE,
		                            ((int) execution.getVariable(BBConstants.G_CURRENT_SEQUENCE)) + 1);
		                }
		            }
		        }
		        
		        else if(currentBB.getBuildingBlock().getBpmnScope().equals("VFModule")) {
		        	//TODO: Need to find logic for getting list of vfModules associated with resp. VNF's 
		            List<VfModuleCustomization> vfResourceCustomizations = catalogDbClient
		                    .getVfModuleResourceCustomizationByModelUuid(currentBB.getRequestDetails().getModelInfo().getModelUuid());
		            if (vfResourceCustomizations != null && !vfResourceCustomizations.isEmpty()) {
		                VfModuleCustomization vmc =
		                        catalogDbClient.findVfModuleCustomizationInList(customizationUUID, vfResourceCustomizations);
		                boolean skipConfigVFModule = vmc.isSkipPostInstConf();
		                if (skipConfigVFModule) {
		                    execution.setVariable(BBConstants.G_CURRENT_SEQUENCE,
		                            ((int) execution.getVariable(BBConstants.G_CURRENT_SEQUENCE)) + 1);
		                }
		            
		            }
		        }
		    }
		
	}


