package org.openecomp.mso.client.grm;

import org.openecomp.mso.client.grm.beans.ServiceEndPointList;
import org.openecomp.mso.client.grm.beans.ServiceEndPointLookup;
import org.openecomp.mso.client.grm.beans.ServiceEndPointLookupRequest;
import org.openecomp.mso.client.grm.beans.ServiceEndPointRequest;
import org.openecomp.mso.client.grm.beans.VersionLookup;
import org.openecomp.mso.client.grm.exceptions.GRMClientCallFailed;

public class GRMClient {

	public String findRunningServicesAsString(String name, int majorVersion, String env) throws Exception {
		
		ServiceEndPointLookupRequest request = buildServiceEndPointlookupRequest(name, majorVersion, env);		
		try {
			GRMRestInvoker invoker = this.getInvoker(GRMAction.FIND_RUNNING);
			return invoker.post(request, String.class);
		}
		catch(Exception e) {
			throw new GRMClientCallFailed("Call to GRM findRunning failed: " + e.getMessage(), e);
		}
	}
	
	public ServiceEndPointList findRunningServices(String name, int majorVersion, String env) throws Exception {
		
		ServiceEndPointLookupRequest request = buildServiceEndPointlookupRequest(name, majorVersion, env);
		try {
			GRMRestInvoker invoker = this.getInvoker(GRMAction.FIND_RUNNING);
			return invoker.post(request, ServiceEndPointList.class);
		}
		catch(Exception e) {
			throw new GRMClientCallFailed("Call to GRM findRunning failed: " + e.getMessage(), e);
		}
	}
	
	protected ServiceEndPointLookupRequest buildServiceEndPointlookupRequest(String name, int majorVersion, String env) {
		VersionLookup version = new VersionLookup();
		version.setMajor(majorVersion);
		
		ServiceEndPointLookup endpoint = new ServiceEndPointLookup();
		endpoint.setName(name);
		endpoint.setVersion(version);
		
		ServiceEndPointLookupRequest request = new ServiceEndPointLookupRequest();
		request.setServiceEndPoint(endpoint);
		request.setEnv(env);
		return request;
	}
	
	public void addServiceEndPoint(ServiceEndPointRequest request) throws Exception {
		try {
			GRMRestInvoker invoker = this.getInvoker(GRMAction.ADD);
			invoker.post(request);
		}
		catch(Exception e) {
			throw new GRMClientCallFailed("Call to GRM addServiceEndPoint failed: " + e.getMessage(), e);
		}
	}
	
	protected GRMRestInvoker getInvoker(GRMAction action) {
		return new GRMRestInvoker(action);
	}
}
