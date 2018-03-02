package org.openecomp.mso.apihandlerinfra.tenantisolation.process;

import org.openecomp.mso.apihandlerinfra.tenantisolation.CloudOrchestrationRequest;
import org.openecomp.mso.apihandlerinfra.tenantisolation.helpers.AAIClientHelper;
import org.openecomp.mso.apihandlerinfra.tenantisolation.helpers.AAIClientObjectBuilder;
import org.openecomp.mso.requestsdb.RequestsDBHelper;

public abstract class OperationalEnvironmentProcess {

    protected String requestId;
    protected CloudOrchestrationRequest request;
    protected AAIClientObjectBuilder aaiClientObjectBuilder;
    protected AAIClientHelper aaiHelper;
    protected RequestsDBHelper requestDb;
    
	public OperationalEnvironmentProcess(CloudOrchestrationRequest request, String requestId) {
		this.requestId = requestId;
		this.request = request;
		this.aaiClientObjectBuilder = new AAIClientObjectBuilder(getRequest());
	}

	protected String getRequestId() {
		return this.requestId;
	}

	protected CloudOrchestrationRequest getRequest() {
		return this.request;
	}

	protected AAIClientHelper getAaiHelper() {
		if(this.aaiHelper == null) {
			this.aaiHelper = new AAIClientHelper(getServiceName(), getRequestId());
		}
		return this.aaiHelper;
	}

	protected void setAaiHelper(AAIClientHelper helper) {
		this.aaiHelper = helper;
	}
	
	protected AAIClientObjectBuilder getAaiClientObjectBuilder() {
		return this.aaiClientObjectBuilder;
	}

	protected RequestsDBHelper getRequestDb() {
		if(requestDb == null) {
			requestDb = new RequestsDBHelper();
		}
		return requestDb;
	}
	
	protected void setRequestsDBHelper(RequestsDBHelper helper) {
		this.requestDb = helper;
	}
	
	protected abstract String getServiceName();
	public abstract void execute();
}
