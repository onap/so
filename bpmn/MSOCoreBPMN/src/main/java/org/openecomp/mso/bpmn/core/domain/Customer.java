package org.openecomp.mso.bpmn.core.domain;

import java.io.Serializable;
/**
 * This class is used to store customer
 * data of services aka ServiceDecomposition
 *
 * @author bb3476
 *
 */

public class Customer extends JsonWrapper implements Serializable {

	private static final long serialVersionUID = 1L;
	private String subscriptionServiceType;
	private String globalSubscriberId;


	public String getSubscriptionServiceType() {
		return subscriptionServiceType;
	}
	public void setSubscriptionServiceType(String subscriptionServiceType) {
		this.subscriptionServiceType = subscriptionServiceType;
	}
	public String getGlobalSubscriberId() {
		return globalSubscriberId;
	}
	public void setGlobalSubscriberId(String globalSubscriberId) {
		this.globalSubscriberId = globalSubscriberId;
	}
	
}