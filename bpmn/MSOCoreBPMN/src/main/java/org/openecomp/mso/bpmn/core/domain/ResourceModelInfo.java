/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
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

public class ResourceModelInfo  extends JsonWrapper implements Serializable{
	
	private static final long serialVersionUID = 1L;
	String resourceName;
	String resourceInvariantUuid;
	String resourceUuid;
	String resourceCustomizationUuid;
	
	public String getResourceName() {
		return resourceName;
	}
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}
	public String getResourceInvariantUuid() {
		return resourceInvariantUuid;
	}
	public void setResourceInvariantUuid(String resourceInvariantUuid) {
		this.resourceInvariantUuid = resourceInvariantUuid;
	}
	public String getResourceUuid() {
		return resourceUuid;
	}
	public void setResourceUuid(String resourceUuid) {
		this.resourceUuid = resourceUuid;
	}
	public String getResourceCustomizationUuid() {
		return resourceCustomizationUuid;
	}
	public void setResourceCustomizationUuid(String resourceCustomizationUuid) {
		this.resourceCustomizationUuid = resourceCustomizationUuid;
	}

}
