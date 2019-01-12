package org.onap.so.client.sniro.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonPropertyOrder({
    "subscriberInfo",
    "placementDemands",
    "requestParameters"
})
@JsonRootName("placementInfo")
public class PlacementInfo implements Serializable{

	private static final long serialVersionUID = -964488472247386556L;

	@JsonProperty("subscriberInfo")
	private SubscriberInfo subscriberInfo;
	@JsonProperty("placementDemands")
	private List<Demand> demands = new ArrayList<Demand>();
	@JsonRawValue
	@JsonProperty("requestParameters")
	private String requestParameters;


	public SubscriberInfo getSubscriberInfo(){
		return subscriberInfo;
	}

	public void setSubscriberInfo(SubscriberInfo subscriberInfo){
		this.subscriberInfo = subscriberInfo;
	}

	public List<Demand> getDemands(){
		return demands;
	}

	public void setDemands(List<Demand> demands){
		this.demands = demands;
	}

	public String getRequestParameters(){
		return requestParameters;
	}

	public void setRequestParameters(String requestParameters){
		this.requestParameters = requestParameters;
	}

}
