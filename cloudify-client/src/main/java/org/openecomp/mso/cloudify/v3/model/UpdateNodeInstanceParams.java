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

package org.openecomp.mso.cloudify.v3.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Map;

public class UpdateNodeInstanceParams implements Serializable {

	private static final long serialVersionUID = 1L;
	
    @JsonProperty("state")
    private String state;

    @JsonProperty("version")
    private String version;

    @JsonProperty("runtime_properties")
    private Map<String, Object> runtimeProperties;


    public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Map<String, Object> getRuntimeProperties() {
		return runtimeProperties;
	}

	public void setRuntimeProperties(Map<String, Object> runtimeProperties) {
		this.runtimeProperties = runtimeProperties;
	}


	@Override
    public String toString() {
        return "UpdateNodeInstanceParams{" +
                "state='" + state + '\'' +
                "version='" + version + '\'' +
                ", runtimeProperties=" + runtimeProperties +
                '}';
    }

}
