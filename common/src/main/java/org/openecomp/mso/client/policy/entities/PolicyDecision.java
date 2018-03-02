
package org.openecomp.mso.client.policy.entities;



import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "decision", "details" })
public class PolicyDecision {

	@JsonProperty("decision")
	private String decision;
	@JsonProperty("details")
	private String details;

	@JsonProperty("decision")
	public String getDecision() {
		return decision;
	}

	@JsonProperty("decision")
	public void setDecision(String decision) {
		this.decision = decision;
	}

	@JsonProperty("details")
	public String getDetails() {
		return details;
	}

	@JsonProperty("details")
	public void setDetails(String details) {
		this.details = details;
	}
}
