package org.openecomp.mso.client.avpn.dmaap.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AVPNDmaapBean {
	
	@JsonProperty("asyncRequestStatus")
	private AsyncRequestStatus asyncRequestStatus;

	public AsyncRequestStatus getAsyncRequestStatus() {
		return asyncRequestStatus;
	}

	public void setAsyncRequestStatus(AsyncRequestStatus asyncRequestStatus) {
		this.asyncRequestStatus = asyncRequestStatus;
	}

}
