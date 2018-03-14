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

/**
 * This class is used to store instance
 * data of services aka ServiceDecomposition
 *
 * @author bb3476
 *
 */

public class Request extends JsonWrapper implements Serializable {

	private static final long serialVersionUID = 1L;
	private String sdncRequestId;
	private String requestId;
	private ModelInfo modelInfo;
	private String productFamilyId;
	
	public String getSdncRequestId() {
		return sdncRequestId;
	}
	public void setSdncRequestId(String sdncRequestId) {
		this.sdncRequestId = sdncRequestId;
	}
	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	public ModelInfo getModelInfo() {
		return modelInfo;
	}
	public void setModelInfo(ModelInfo modelInfo) {
		this.modelInfo = modelInfo;
	}
	public String getProductFamilyId() {
		return productFamilyId;
	}
	public void setProductFamilyId(String productFamilyId) {
		this.productFamilyId = productFamilyId;
	}
	
	
}