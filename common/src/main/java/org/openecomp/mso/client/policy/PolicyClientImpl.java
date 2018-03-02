package org.openecomp.mso.client.policy;

import org.openecomp.mso.client.RestPropertiesLoader;
import org.openecomp.mso.client.defaultproperties.PolicyRestPropertiesImpl;
import org.openecomp.mso.client.policy.entities.AllowedTreatments;
import org.openecomp.mso.client.policy.entities.Bbid;
import org.openecomp.mso.client.policy.entities.DecisionAttributes;
import org.openecomp.mso.client.policy.entities.DictionaryData;
import org.openecomp.mso.client.policy.entities.DictionaryItemsRequest;
import org.openecomp.mso.client.policy.entities.DictionaryJson;
import org.openecomp.mso.client.policy.entities.PolicyDecision;
import org.openecomp.mso.client.policy.entities.PolicyDecisionRequest;
import org.openecomp.mso.client.policy.entities.PolicyServiceType;
import org.openecomp.mso.client.policy.entities.Workstep;

import java.util.List;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class PolicyClientImpl implements PolicyClient {

	protected final EELFLogger metricsLogger = EELFManager.getInstance().getMetricsLogger();
	private PolicyRestProperties props;
	public PolicyClientImpl() {
		props = RestPropertiesLoader.getInstance().getNewImpl(PolicyRestProperties.class);
		if (props == null) {
			metricsLogger.error("No RestProperty.PolicyRestProperties implementation found on classpath");
			props = new PolicyRestPropertiesImpl();
		}
	}
	public PolicyDecision getDecision(String serviceType, String vnfType, String bbID, String workStep,
			String errorCode) {
		DecisionAttributes decisionAttributes = new DecisionAttributes();
		decisionAttributes.setServiceType(serviceType);
		decisionAttributes.setVNFType(vnfType);
		decisionAttributes.setBBID(bbID);
		decisionAttributes.setWorkStep(workStep);
		decisionAttributes.setErrorCode(errorCode);

		return this.getDecision(decisionAttributes);
	}

	private PolicyDecision getDecision(DecisionAttributes decisionAttributes) {
		PolicyRestClient client = new PolicyRestClient(this.props, PolicyServiceType.GET_DECISION);
		PolicyDecisionRequest decisionRequest = new PolicyDecisionRequest();
		decisionRequest.setDecisionAttributes(decisionAttributes);
		decisionRequest.setEcompcomponentName(RestClient.ECOMP_COMPONENT_NAME);
		
		return client.post(decisionRequest, PolicyDecision.class);
	}
	
	public DictionaryData getAllowedTreatments(String bbID, String workStep)
	{
		PolicyRestClient client = new PolicyRestClient(this.props, PolicyServiceType.GET_DICTIONARY_ITEMS);
		DictionaryItemsRequest dictionaryItemsRequest = new DictionaryItemsRequest();
		dictionaryItemsRequest.setDictionaryType("Decision");
		dictionaryItemsRequest.setDictionary("RainyDayTreatments");
		final AllowedTreatments response = client.post(dictionaryItemsRequest, AllowedTreatments.class);
		final DictionaryJson dictionaryJson = response.getDictionaryJson();
		final List<DictionaryData> dictionaryDataList = dictionaryJson.getDictionaryDatas();
		for(DictionaryData dictData : dictionaryDataList){
			Bbid bBid = dictData.getBbid();
			Workstep workstep = dictData.getWorkstep();
			String bBidString = bBid.getString();
			String workstepString = workstep.getString();
			if(bbID.equals(bBidString) && workStep.equals(workstepString)){
				return dictData;
			}
		}
		metricsLogger.error("There is no AllowedTreatments with that specified parameter set");
		return null;
	}

}
