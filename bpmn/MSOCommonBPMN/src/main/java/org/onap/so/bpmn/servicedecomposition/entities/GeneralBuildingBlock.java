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

package org.onap.so.bpmn.servicedecomposition.entities;

import java.io.Serializable;
import java.util.Map;

import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Tenant;
import org.onap.so.bpmn.servicedecomposition.generalobjects.OrchestrationContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;

public class GeneralBuildingBlock implements Serializable {
	private static final String INVALID_INPUT_MISSING = "Expected building block input of \"%s\" not found in decomposition";
	private static final String INVALID_INPUT_CLASS_CAST = "Expected building block input of \"%s\" was the wrong object type in the decomposition";

	private static final long serialVersionUID = -429247436428110843L;

	private RequestContext requestContext;
	private OrchestrationContext orchContext;
	private Map<String, String> userInput;
	private CloudRegion cloudRegion;
	private Tenant tenant;

	private Customer customer;
	private ServiceInstance serviceInstance;

	
	public Tenant getTenant() {
		return tenant;
	}

	public void setTenant(Tenant tenant) {
		this.tenant = tenant;
	}

	public CloudRegion getCloudRegion() {
		return cloudRegion;
	}

	public void setCloudRegion(CloudRegion cloudRegion) {
		this.cloudRegion = cloudRegion;
	}

	public RequestContext getRequestContext() {
		return requestContext;
	}

	public void setRequestContext(RequestContext requestContext) {
		this.requestContext = requestContext;
	}

	public OrchestrationContext getOrchContext() {
		return orchContext;
	}

	public void setOrchContext(OrchestrationContext orchContext) {
		this.orchContext = orchContext;
	}

	public Map<String, String> getUserInput() {
		return userInput;
	}

	public void setUserInput(Map<String, String> userInput) {
		this.userInput = userInput;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public ServiceInstance getServiceInstance() {
		return serviceInstance;
	}

	public void setServiceInstance(ServiceInstance serviceInstance) {
		this.serviceInstance = serviceInstance;
	}
}
