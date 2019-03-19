/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.flowspecific.tasks;

import java.util.List;
import java.util.Optional;

import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.springframework.stereotype.Component;

@Component
public class NetworkBBUtils {	
	
	private static final String CLOUD_REGION_VER25 = "2.5"; 
	private static final String CLOUD_REGION_AAIAIC25 = "AAIAIC25";
	
	/**
	 * BPMN access method to check if Relationship's Related-To value exists.
	 * 
	 * @param l3Network - L3Network object
	 * @param relatedToValue - String, ex: 'vf-module'
	 * @return boolean
	 */
	public boolean isRelationshipRelatedToExists(Optional<org.onap.aai.domain.yang.L3Network> l3network, String relatedToValue) {
		boolean isRelatedToExists = false;
		if (l3network.isPresent()) {
			List<org.onap.aai.domain.yang.Relationship> relationshipList = l3network.get().getRelationshipList().getRelationship();
			for (org.onap.aai.domain.yang.Relationship relationship : relationshipList) {
			    if (relationship.getRelatedTo().equals(relatedToValue)) { 
			    	isRelatedToExists = true; 
			    	break; 
			    }
			}
		}
		return isRelatedToExists;
	}
	
	/**
	 * BPMN access method to extract Cloud Region data
	 * @param execution
	 * @param motsValue (ex: SourceSystem.SDNC or SourceSystem.PO)
	 */
	public String getCloudRegion(BuildingBlockExecution execution, SourceSystem sourceValue) {
		GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
		CloudRegion cloudRegion = gBBInput.getCloudRegion();
		String cloudRegionId = cloudRegion.getLcpCloudRegionId();
		if (sourceValue.equals(SourceSystem.SDNC) && CLOUD_REGION_VER25.equalsIgnoreCase(cloudRegion.getCloudRegionVersion())) { 
			cloudRegionId = CLOUD_REGION_AAIAIC25;
		}
		return cloudRegionId;
	}
		
	
}
