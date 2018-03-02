package org.openecomp.mso.client.orchestration;

import java.util.Optional;
import java.util.logging.Logger;

import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition;
import org.openecomp.mso.client.sdnc.beans.SDNCRequest;
import org.openecomp.mso.client.sdnc.beans.SDNCSvcAction;
import org.openecomp.mso.client.sdnc.beans.SDNCSvcOperation;
import org.openecomp.mso.client.sdnc.mapper.ServiceTopologyOperationRequestMapper;
import org.openecomp.mso.client.sdnc.sync.SDNCSyncRpcClient;
import org.openecomp.mso.properties.MsoPropertiesFactory;

public class SDNCOrchestrator {

	private static MsoPropertiesFactory msoPF = new MsoPropertiesFactory();
	
	public void createServiceInstance (ServiceDecomposition serviceDecomp) {
	
		try{
			msoPF.initializeMsoProperties("MSO_PROP_SDNC_ADAPTER", "mso.sdnc.properties");
			Optional<String> msoAction = getMSOAction(serviceDecomp);
			ServiceTopologyOperationRequestMapper sdncRM = new ServiceTopologyOperationRequestMapper(msoAction, SDNCSvcOperation.SERVICE_TOPOLOGY_OPERATION, SDNCSvcAction.ASSIGN, "CreateServiceInstance");
			SDNCRequest request = sdncRM.reqMapper(serviceDecomp);
			SDNCSyncRpcClient sdncRC = new SDNCSyncRpcClient (request, msoPF);
			sdncRC.run();
		} catch (Exception ex) {
			throw new IllegalStateException();
		}
	}
	
	private Optional<String> getMSOAction (ServiceDecomposition serviceDecomp){
		String serviceType = serviceDecomp.getServiceInstance().getServiceType();
		if(serviceType == null || serviceType.equals("")){
			return Optional.empty();
		}
		
		return Optional.of(serviceType);
	}

}
