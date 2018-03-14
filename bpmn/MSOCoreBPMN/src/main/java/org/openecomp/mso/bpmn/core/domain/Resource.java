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

package org.openecomp.mso.bpmn.core.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;



public abstract class Resource extends JsonWrapper  implements Serializable {

	private static final long serialVersionUID = 1L;
	private String resourceId; // TODO name this field just id instead, should be the id of the object as it is in aai
	protected ResourceType resourceType; // Enum of vnf or network or allotted resource
	protected ModelInfo modelInfo;
	private long concurrencyCounter = 1L;

	//private List modules;
	private ResourceInstance resourceInstance = new ResourceInstance(); // TODO possibly remove
	private HomingSolution homingSolution = new HomingSolution();
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private HomingSolution currentHomingSolution;

	//common parameters for all Resources
	private String toscaNodeType;

	// GET and SET
	public String getResourceId() {
		return resourceId;
	}
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	public ModelInfo getModelInfo() {
		return modelInfo;
	}
	public void setModelInfo(ModelInfo modelInfo) {
		this.modelInfo = modelInfo;
	}

	public ResourceInstance getResourceInstance() {
		return resourceInstance;
	}
	public void setResourceInstance(ResourceInstance resourceInstance) {
		this.resourceInstance = resourceInstance;
	}
	public HomingSolution getHomingSolution(){
		return homingSolution;
	}

	public void setHomingSolution(HomingSolution homingSolution){
		this.homingSolution = homingSolution;
	}
	public HomingSolution getCurrentHomingSolution() {
		return currentHomingSolution;
	}
	public void setCurrentHomingSolution(HomingSolution currentHomingSolution) {
		this.currentHomingSolution = currentHomingSolution;
	}
	public void setResourceType(ResourceType resourceType) {
		this.resourceType = resourceType;
	}

	public ResourceType getResourceType(){
		return resourceType;
	}

	public String getToscaNodeType() {
		return toscaNodeType;
	}
	public void setToscaNodeType(String toscaNodeType) {
		this.toscaNodeType = toscaNodeType;
	}

	//Utility methods

	public String getResourceInstanceId() {
		return this.getResourceInstance().getInstanceId();
	}
	public String getResourceInstanceName() {
		return this.getResourceInstance().getInstanceName();
	}
	//TODO
//	@JsonIgnore
//	public String getResourceHomingSolution() {
//	}

	public void setResourceInstanceId(String newInstanceId){
		this.getResourceInstance().setInstanceId(newInstanceId);
	}
	public void setResourceInstanceName(String newInstanceName){
		this.getResourceInstance().setInstanceName(newInstanceName);
	}

	//TODO
//	@JsonIgnore
//	public String setResourceHomingSolution() {
//	}
	/**
	 * To be used by macro flow to increment concurrency counter after update to it's structure was completed
	 */
	public void incrementConcurrencyCounter(){
		this.concurrencyCounter ++;
	}
	/**
	 * Method to get concurrency counter data
	 * @return long value for the counter
	 */
	@JsonIgnore
	public long getConcurrencyCounter(){
		return concurrencyCounter;
	}

}