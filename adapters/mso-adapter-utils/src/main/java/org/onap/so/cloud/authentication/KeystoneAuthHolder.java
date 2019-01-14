/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.cloud.authentication;

import java.io.Serializable;
import java.util.Calendar;

public class KeystoneAuthHolder implements Serializable {

	private static final long serialVersionUID = -9073252905181739224L;
	
	private String id;
	private Calendar expiration;
	private String serviceUrl;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Calendar getexpiration() {
		return expiration;
	}
	public void setexpiration(Calendar expiration) {
		this.expiration = expiration;
	}
	public String getServiceUrl() {
		return serviceUrl;
	}
	public void setHeatUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}
}
