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
 * This class represents the specifics of a tunnel
 * cross connect piece of a resource
 *
 * @author cb645j
 *
 *TODO This may change to house both isp speeds
 */
@JsonRootName("tunnelConnect")
public class TunnelConnect extends JsonWrapper implements Serializable {

	private static final long serialVersionUID = 1L;
	private String id;
	private String upBandwidth;
	private String downBandwidth;
	private String upBandwidth2;
	private String downBandwidth2;


	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUpBandwidth() {
		return upBandwidth;
	}
	public void setUpBandwidth(String upBandwidth) {
		this.upBandwidth = upBandwidth;
	}
	public String getDownBandwidth() {
		return downBandwidth;
	}
	public void setDownBandwidth(String downBandwidth) {
		this.downBandwidth = downBandwidth;
	}
	public String getUpBandwidth2() {
		return upBandwidth2;
	}
	public void setUpBandwidth2(String upBandwidth2) {
		this.upBandwidth2 = upBandwidth2;
	}
	public String getDownBandwidth2() {
		return downBandwidth2;
	}
	public void setDownBandwidth2(String downBandwidth2) {
		this.downBandwidth2 = downBandwidth2;
	}

}
