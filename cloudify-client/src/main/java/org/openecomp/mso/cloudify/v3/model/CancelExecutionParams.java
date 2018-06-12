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

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CancelExecutionParams implements Serializable {

	private static final long serialVersionUID = 1L;
	
    @JsonProperty("action")
    private String action;
    
    public final static String CANCEL_ACTION = "cancel";
    public final static String FORCE_CANCEL_ACTION = "force-cancel";
    
    public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}


	@Override
    public String toString() {
        return "CancelExecutionParams{" +
                "action='" + action + '\'' +
                '}';
    }

}
