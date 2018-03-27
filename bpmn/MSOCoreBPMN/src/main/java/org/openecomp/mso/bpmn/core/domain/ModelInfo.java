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

import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("modelInfo")
public class ModelInfo  extends JsonWrapper implements Serializable {

	/**
	 * This is domain object defining structure for MODEL INFO
	 * It will be valid for each Resource type object
	 */
	private static final long serialVersionUID = 1L;
	
	private String modelName = "";
	private String modelUuid = "";
	private String modelInvariantUuid = "";
	private String modelVersion = "";
	//additionally on resource level
	private String modelCustomizationUuid = "";
	private String modelCustomizationName = "";
	private String modelInstanceName = "";
	private String modelType = "";

	//GET and SET methods
	
	public String getModelName() {
		return modelName;
	}
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}
	public String getModelUuid() {
		return modelUuid;
	}
	public void setModelUuid(String modelUuid) {
		this.modelUuid = modelUuid;
	}
	public String getModelInvariantUuid() {
		return modelInvariantUuid;
	}
	public void setModelInvariantUuid(String modelInvariantUuid) {
		this.modelInvariantUuid = modelInvariantUuid;
	}
	public String getModelVersion() {
		return modelVersion;
	}
	public void setModelVersion(String modelVersion) {
		this.modelVersion = modelVersion;
	}
	public String getModelCustomizationUuid() {
		return modelCustomizationUuid;
	}
	public void setModelCustomizationUuid(String modelCustomizationUuid) {
		this.modelCustomizationUuid = modelCustomizationUuid;
	}
	public String getModelCustomizationName() {
		return modelCustomizationName;
	}
	public void setModelCustomizationName(String modelCustomizationName) {
		this.modelCustomizationName = modelCustomizationName;
	}
	public String getModelInstanceName() {
		return modelInstanceName;
	}
	public void setModelInstanceName(String modelInstanceName) {
		this.modelInstanceName = modelInstanceName;
	}
	public String getModelType() {
		return modelType;
	}
	public void setModelType(String modelType) {
		this.modelType = modelType;
	}
		
}