package org.openecomp.mso.bpmn.infrastructure.adapter.network.tasks;

import java.util.Optional;

import org.openecomp.mso.adapters.nwrest.UpdateNetworkResponse;
import org.openecomp.mso.bpmn.common.BuildingBlockExecution;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.L3Network;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.openecomp.mso.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.openecomp.mso.bpmn.servicedecomposition.entities.ResourceKey;
import org.openecomp.mso.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.openecomp.mso.client.exception.ExceptionBuilder;
import org.openecomp.mso.client.orchestration.NetworkAdapterResources;
import org.openecomp.mso.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NetworkAdapterUpdateTasks {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, NetworkAdapterUpdateTasks.class);
	
	@Autowired
	private ExtractPojosForBB extractPojosForBB;
	@Autowired
	private NetworkAdapterResources networkAdapterResources;
	@Autowired
	private ExceptionBuilder exceptionUtil;
	
	public void updateNetwork(BuildingBlockExecution execution) {
		try {
			GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
			ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID));
			L3Network l3Network = extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID, execution.getLookupMap().get(ResourceKey.NETWORK_ID));
			Optional<UpdateNetworkResponse> oUpdateNetworkResponse = networkAdapterResources.updateNetwork(gBBInput.getRequestContext(), gBBInput.getCloudRegion(), gBBInput.getOrchContext(), serviceInstance, l3Network, gBBInput.getUserInput(), gBBInput.getCustomer());
			
			if(oUpdateNetworkResponse.isPresent()) {
				UpdateNetworkResponse updateNetworkResponse = oUpdateNetworkResponse.get();
				execution.setVariable("NetworkAdapterUpdateNetworkResponse", updateNetworkResponse);
			}
		} catch(Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
}
