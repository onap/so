
package org.openecomp.mso.client.policy.entities;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "decisionAttributes", "ecompcomponentName" })
public class PolicyDecisionRequest {

	@JsonProperty("decisionAttributes")
	private DecisionAttributes decisionAttributes;
	@JsonProperty("ecompcomponentName")
	private String ecompcomponentName;

	@JsonProperty("decisionAttributes")
	public DecisionAttributes getDecisionAttributes() {
		return decisionAttributes;
	}

	@JsonProperty("decisionAttributes")
	public void setDecisionAttributes(DecisionAttributes decisionAttributes) {
		this.decisionAttributes = decisionAttributes;
	}

	@JsonProperty("ecompcomponentName")
	public String getEcompcomponentName() {
		return ecompcomponentName;
	}

	@JsonProperty("ecompcomponentName")
	public void setEcompcomponentName(String ecompcomponentName) {
		this.ecompcomponentName = ecompcomponentName;
	}

}
