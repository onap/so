package org.openecomp.mso.client.policy;

import org.openecomp.mso.client.policy.entities.AllowedTreatments;
import org.openecomp.mso.client.policy.entities.DictionaryData;
import org.openecomp.mso.client.policy.entities.PolicyDecision;

public interface PolicyClient {

	public PolicyDecision getDecision(String serviceType, String vnfType, String bbID, String workStep,
			String errorCode);
	
	public DictionaryData getAllowedTreatments(String bbID, String workStep);
}
