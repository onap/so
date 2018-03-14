/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

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
