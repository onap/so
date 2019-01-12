package org.onap.so.client.sniro.beans;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("subscriberInfo")
public class SubscriberInfo implements Serializable{

	private static final long serialVersionUID = -6350949051379748872L;

	@JsonProperty("globalSubscriberId")
	private String globalSubscriberId;
	@JsonProperty("subscriberName")
	private String subscriberName;
	@JsonProperty("subscriberCommonSiteId")
	private String subscriberCommonSiteId;


	public String getGlobalSubscriberId(){
		return globalSubscriberId;
	}

	public void setGlobalSubscriberId(String globalSubscriberId){
		this.globalSubscriberId = globalSubscriberId;
	}

	public String getSubscriberName(){
		return subscriberName;
	}

	public void setSubscriberName(String subscriberName){
		this.subscriberName = subscriberName;
	}

	public String getSubscriberCommonSiteId(){
		return subscriberCommonSiteId;
	}

	public void setSubscriberCommonSiteId(String subscriberCommonSiteId){
		this.subscriberCommonSiteId = subscriberCommonSiteId;
	}

}
