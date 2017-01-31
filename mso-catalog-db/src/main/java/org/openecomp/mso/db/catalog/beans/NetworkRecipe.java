/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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

package org.openecomp.mso.db.catalog.beans;


public class NetworkRecipe extends Recipe {
	private String networkType;
	private String networkParamXSD;
	public NetworkRecipe() {}

	public String getNetworkType() {
		return networkType;
	}
	public void setNetworkType(String networkType) {
		this.networkType = networkType;
	}

	public String getNetworkParamXSD() {
		return networkParamXSD;
	}
	public void setNetworkParamXSD(String networkParamXSD) {
		this.networkParamXSD = networkParamXSD;
	}
	
	@Override
	public String toString () {
		StringBuffer sb = new StringBuffer();
		sb.append(super.toString());
		sb.append (",networkType=" + networkType);
		sb.append (",networkParamXSD=" + networkParamXSD);
		return sb.toString();
	}
}
