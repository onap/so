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

package org.openecomp.mso.bpmn.common.recipe;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *  The bpmn workflow input param object
 */
public class BpmnParam {
	
	@JsonProperty("value")
	private String value;
	@JsonProperty("type")
	private String type = "String";


	public BpmnParam() {
		/* Empty constructor */
	}
	
	@JsonProperty("value")
	public String getValue() {
		return value;
	}
	
	@JsonProperty("value")
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "CamundaInput [value=" + value + ", type=" + type + "]";
	}	
	
}
