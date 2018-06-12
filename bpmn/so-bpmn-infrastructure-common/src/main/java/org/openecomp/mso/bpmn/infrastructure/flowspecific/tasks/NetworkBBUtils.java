package org.openecomp.mso.bpmn.infrastructure.flowspecific.tasks;

import java.util.List;
import java.util.Optional;

import org.openecomp.mso.bpmn.common.BuildingBlockExecution;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.openecomp.mso.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.openecomp.mso.logger.MsoLogger;
import org.springframework.stereotype.Component;

@Component
public class NetworkBBUtils {

	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, NetworkBBUtils.class);
	
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
