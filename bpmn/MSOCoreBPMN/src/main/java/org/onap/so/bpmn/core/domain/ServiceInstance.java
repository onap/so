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

package org.onap.so.bpmn.core.domain;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * This class is used to store instance
 * data of services aka ServiceDecomposition
 *
 * @author cb645j
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceInstance extends JsonWrapper implements Serializable {

	private static final long serialVersionUID = 1L;
	private String instanceId;
	private String instanceName;
	private String orchestrationStatus;
	private Configuration configuration;
	private String serviceType;
	private String serviceRole;
	private String serviceId;
	private ModelInfo modelInfo;
	private String environmentContext;
	private String workloadContext;
	private Map serviceParams;
	private Customer customer = new Customer();
	private String e2eVpnKey;

	public String getServiceType() {
		return serviceType;
	}
	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}
	public String getServiceId() {
		return serviceId;
	}
	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}
	public Map getServiceParams() {
		return serviceParams;
	}
	public void setServiceParams(Map serviceParams) {
		this.serviceParams = serviceParams;
	}
	public String getInstanceId() {
		return instanceId;
	}
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	public String getInstanceName() {
		return instanceName;
	}
	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}
	public String getOrchestrationStatus() {
		return orchestrationStatus;
	}
	public void setOrchestrationStatus(String orchestrationStatus) {
		this.orchestrationStatus = orchestrationStatus;
	}
	public Configuration getConfiguration() {
		return configuration;
	}
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
	public ModelInfo getModelInfo() {
		return modelInfo;
	}
	public void setModelInfo(ModelInfo modelInfo) {
		this.modelInfo = modelInfo;
	}
	public String getEnvironmentContext() {
		return environmentContext;
	}
	public void setEnvironmentContext(String environmentContext) {
		this.environmentContext = environmentContext;
	}
	public String getWorkloadContext() {
		return workloadContext;
	}
	public void setWorkloadContext(String workloadContext) {
		this.workloadContext = workloadContext;
	}
	public String getServiceRole() {
		return serviceRole;
	}
	public void setServiceRole(String serviceRole) {
		this.serviceRole = serviceRole;
	}
	public Customer getCustomer(){
		return customer;
	}

	public String getE2eVpnKey(){
		return e2eVpnKey;
	}

	public void setE2eVpnKey(String e2eVpnKey){
		this.e2eVpnKey = e2eVpnKey;
	}
}
