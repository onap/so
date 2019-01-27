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

package org.onap.so.openstack.beans;

import java.io.Serializable;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;

public class Pool implements Serializable {

	private String start;
	private String end;
	private static final long serialVersionUID = 768026109321305392L;

	/**
	 * @return the start
	 */
	public String getStart() {
		return start;
	}
	/**
	 * @param start the start to set
	 */
	public void setStart(String start) {
		this.start = start;
	}
	/**
	 * @return the end
	 */
	public String getEnd() {
		return end;
	}
	/**
	 * @param end the end to set
	 */
	public void setEnd(String end) {
		this.end = end;
	}

	@Override
	public String toString() {
		return "Allocation_pool [start=" + start + ", end=" + end + "]";
	}
	
	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof Pool)) {
			return false;
		}
		Pool castOther = (Pool) other;
		return new EqualsBuilder().append(start, castOther.start).append(end, castOther.end).isEquals();
	}
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(start).append(end).toHashCode();
	}
	

}
