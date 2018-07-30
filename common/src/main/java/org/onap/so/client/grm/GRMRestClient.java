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

package org.onap.so.client.grm;


import java.net.URI;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import org.onap.so.client.RestClient;
import org.onap.so.client.RestProperties;
import org.onap.so.utils.TargetEntity;

public class GRMRestClient extends RestClient {

	private final String username;
	private final String password;
	
	public GRMRestClient(RestProperties props, URI path, String username, String password) {
		super(props, Optional.of(path));
		this.username = username;
		this.password = password;
	}

    @Override
    public TargetEntity getTargetEntity(){
        return TargetEntity.GRM;
    }

	@Override
	protected void initializeHeaderMap(Map<String, String> headerMap) {
		headerMap.put("Authorization", "Basic " + Base64.getEncoder().encodeToString(new String(username + ":" + password).getBytes()));
	}

}
