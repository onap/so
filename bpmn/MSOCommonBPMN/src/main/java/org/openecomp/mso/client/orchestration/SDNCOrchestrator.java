/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

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
