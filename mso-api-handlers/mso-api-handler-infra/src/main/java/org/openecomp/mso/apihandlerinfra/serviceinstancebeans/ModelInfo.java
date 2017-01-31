/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.03.30 at 02:48:23 PM CDT 
//


package org.openecomp.mso.apihandlerinfra.serviceinstancebeans;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import org.openecomp.mso.apihandlerinfra.ModelType;

@JsonSerialize(include=JsonSerialize.Inclusion.NON_DEFAULT)
public class ModelInfo {

    protected String modelCustomizationName;
    protected String modelInvariantId;
	protected ModelType modelType;
    protected String modelNameVersionId;
    protected String modelName;
    protected String modelVersion;
    
    
	public String getModelCustomizationName() {
		return modelCustomizationName;
	}
	public void setModelCustomizationName(String modelCustomizationName) {
		this.modelCustomizationName = modelCustomizationName;
	}
	public String getModelNameVersionId() {
		return modelNameVersionId;
	}
	public void setModelNameVersionId(String modelNameVersionId) {
		this.modelNameVersionId = modelNameVersionId;
	}
	public String getModelName() {
		return modelName;
	}
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}
	public String getModelVersion() {
		return modelVersion;
	}
	public void setModelVersion(String modelVersion) {
		this.modelVersion = modelVersion;
	}
	public ModelType getModelType() {
		return modelType;
	}
	public void setModelType(ModelType modelType) {
		this.modelType = modelType;
	}
	public String getModelInvariantId() {
		return modelInvariantId;
	}
	public void setModelInvariantId(String modelInvariantId) {
		this.modelInvariantId = modelInvariantId;
	}
	

}
