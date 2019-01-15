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

package org.onap.so.adapters.valet.beans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * This class represents the body of an Update response on a Valet Placement API call
 */
public class ValetUpdateResponse implements Serializable {
	private static final long serialVersionUID = 768026109321305392L;
	@JsonProperty("status")
	private ValetStatus status;
	@JsonProperty("parameters")
	private HashMap<String, Object> parameters;
	
	public ValetUpdateResponse() {
		super();
	}
	
	public ValetStatus getStatus() {
		return this.status;
	}
	public void setStatus(ValetStatus status) {
		this.status = status;
	}
	public HashMap<String, Object> getParameters() {
		return this.parameters;
	}
	public void setParameters(HashMap<String, Object> parameters) {
		this.parameters = parameters;
	}

	@Override
	public int hashCode() {
		return Objects.hash(status, parameters);
	}
	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof ValetUpdateResponse)) {
			return false;
		}
		ValetUpdateResponse vur = (ValetUpdateResponse) o;
		return Objects.equals(status, vur.status) 
				&& Objects.equals(parameters, vur.parameters);	
	}
}
