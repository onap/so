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

package org.onap.so.client.policy;


import java.util.Map;
import java.util.Optional;

import javax.ws.rs.core.UriBuilder;

import org.onap.so.client.RestClient;
import org.onap.so.client.policy.entities.PolicyServiceType;
import org.onap.so.utils.TargetEntity;

public class PolicyRestClient extends RestClient {

	private final PolicyRestProperties properties;

	public PolicyRestClient(PolicyRestProperties props, PolicyServiceType serviceType) {
		super(props, Optional.of(UriBuilder.fromPath(serviceType.toString()).build()));
		this.properties = props;
	}

    @Override
    public TargetEntity getTargetEntity(){
        return TargetEntity.POLICY;
    }

	@Override
	protected void initializeHeaderMap(Map<String, String> headerMap) {
		headerMap.put("ClientAuth", properties.getClientAuth());
		headerMap.put("Authorization", properties.getAuth());
		headerMap.put("Environment", properties.getEnvironment());
	}

}