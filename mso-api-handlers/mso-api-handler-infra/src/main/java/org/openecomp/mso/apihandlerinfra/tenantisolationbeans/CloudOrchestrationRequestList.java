package org.openecomp.mso.apihandlerinfra.tenantisolationbeans;

import java.util.List;

public class CloudOrchestrationRequestList {

	private List<CloudOrchestrationResponse> requestList;

	public List<CloudOrchestrationResponse> getRequestList() {
		return requestList;
	}

	public void setRequestList(List<CloudOrchestrationResponse> requestList) {
		this.requestList = requestList;
	}

}
