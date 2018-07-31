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
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
/*
 * This class represents the body of a Confirm operation on a Valet Placement API call
 */
public class ValetConfirmRequest implements Serializable {
	private static final long serialVersionUID = 768026109321305392L;
	
	@JsonProperty("stack_id")
	private String stackId;
	
	public ValetConfirmRequest() {
		super();
	}
	public ValetConfirmRequest(String stackId) {
		super();
		this.stackId = stackId;
	}
	
	public String getStackId() {
		return this.stackId;
	}
	public void setStackId(String stackId) {
		this.stackId = stackId;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(stackId);
	}
	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof ValetConfirmRequest)) {
			return false;
		}
		ValetConfirmRequest vcr = (ValetConfirmRequest) o;
		return Objects.equals(stackId, vcr.stackId);
	}
}
