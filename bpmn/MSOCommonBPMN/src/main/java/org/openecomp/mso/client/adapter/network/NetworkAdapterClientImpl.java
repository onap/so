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

package org.openecomp.mso.client.adapter.network;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.openecomp.mso.adapters.nwrest.CreateNetworkRequest;
import org.openecomp.mso.adapters.nwrest.CreateNetworkResponse;
import org.openecomp.mso.adapters.nwrest.DeleteNetworkRequest;
import org.openecomp.mso.adapters.nwrest.DeleteNetworkResponse;
import org.openecomp.mso.adapters.nwrest.QueryNetworkResponse;
import org.openecomp.mso.adapters.nwrest.RollbackNetworkRequest;
import org.openecomp.mso.adapters.nwrest.RollbackNetworkResponse;
import org.openecomp.mso.adapters.nwrest.UpdateNetworkRequest;
import org.openecomp.mso.adapters.nwrest.UpdateNetworkResponse;
import org.openecomp.mso.client.adapter.vnf.AdapterRestClient;

public class NetworkAdapterClientImpl implements NetworkAdapterClient {

	private final NetworkAdapterRestProperties props;
	public NetworkAdapterClientImpl() {
		this.props = new NetworkAdapterRestProperties();
	}
	@Override
	public CreateNetworkResponse createNetwork(CreateNetworkRequest req) {
		return new AdapterRestClient(this.props, this.getUri("").build()).post(req,
				CreateNetworkResponse.class);
	}

	@Override
	public DeleteNetworkResponse deleteNetwork(String aaiNetworkId, DeleteNetworkRequest req) {
		return new AdapterRestClient(this.props, this.getUri("/" + aaiNetworkId).build()).delete(req,
				DeleteNetworkResponse.class);
	}

	@Override
	public RollbackNetworkResponse rollbackNetwork(String aaiNetworkId, RollbackNetworkRequest req) {
		return new AdapterRestClient(this.props, this.getUri("/" + aaiNetworkId).build()).delete(req,
				RollbackNetworkResponse.class);
	}

	@Override
	public QueryNetworkResponse queryNetwork(String aaiNetworkId, String cloudSiteId, String tenantId,
			String networkStackId, boolean skipAAI, String requestId, String serviceInstanceId) {
		UriBuilder builder = this.getUri("/" + aaiNetworkId);
		if (cloudSiteId != null) {
			builder.queryParam("cloudSiteId", cloudSiteId);
		}
		if (tenantId != null) {
			builder.queryParam("tenantId", tenantId);
		}
		if (networkStackId != null) {
			builder.queryParam("networkStackId", networkStackId);
		}

		builder.queryParam("skipAAI", skipAAI);

		if (requestId != null) {
			builder.queryParam("msoRequest.requestId", requestId);
		}
		if (serviceInstanceId != null) {
			builder.queryParam("msoRequest.serviceInstanceId", serviceInstanceId);
		}
		return new AdapterRestClient(this.props, builder.build(), MediaType.TEXT_XML, MediaType.TEXT_XML)
				.get(QueryNetworkResponse.class);
	}

	@Override
	public UpdateNetworkResponse updateNetwork(String aaiNetworkId, UpdateNetworkRequest req) {
		return new AdapterRestClient(this.props, this.getUri("/" + aaiNetworkId).build()).put(req,
				UpdateNetworkResponse.class);
	}

	protected UriBuilder getUri(String path) {
		return UriBuilder.fromPath(path);
	}

}
