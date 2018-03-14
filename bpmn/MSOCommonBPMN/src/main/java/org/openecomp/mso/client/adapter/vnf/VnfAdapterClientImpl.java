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

package org.openecomp.mso.client.adapter.vnf;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.openecomp.mso.adapters.vnfrest.CreateVfModuleRequest;
import org.openecomp.mso.adapters.vnfrest.CreateVfModuleResponse;
import org.openecomp.mso.adapters.vnfrest.DeleteVfModuleRequest;
import org.openecomp.mso.adapters.vnfrest.DeleteVfModuleResponse;
import org.openecomp.mso.adapters.vnfrest.QueryVfModuleResponse;
import org.openecomp.mso.adapters.vnfrest.RollbackVfModuleRequest;
import org.openecomp.mso.adapters.vnfrest.RollbackVfModuleResponse;
import org.openecomp.mso.adapters.vnfrest.UpdateVfModuleRequest;
import org.openecomp.mso.adapters.vnfrest.UpdateVfModuleResponse;

public class VnfAdapterClientImpl implements VnfAdapterClient {

	private static final String VF_MODULES = "/vf-modules/";

	private final VnfAdapterRestProperties props;
	public VnfAdapterClientImpl() {
		this.props = new VnfAdapterRestProperties();
	}
	
	@Override
	public CreateVfModuleResponse createVfModule(String aaiVnfId, CreateVfModuleRequest req) {
		return new AdapterRestClient(this.props, this.getUri("/" + aaiVnfId + "/vf-modules").build()).post(req,
				CreateVfModuleResponse.class);
	}

	@Override
	public RollbackVfModuleResponse rollbackVfModule(String aaiVnfId, String aaiVfModuleId,
			RollbackVfModuleRequest req) {
		return new AdapterRestClient(this.props,
				this.getUri("/" + aaiVnfId + VF_MODULES + aaiVfModuleId + "/rollback").build()).delete(req,
						RollbackVfModuleResponse.class);
	}

	@Override
	public DeleteVfModuleResponse deleteVfModule(String aaiVnfId, String aaiVfModuleId, DeleteVfModuleRequest req) {
		return new AdapterRestClient(this.props, this.getUri("/" + aaiVnfId + VF_MODULES + aaiVfModuleId).build())
				.delete(req, DeleteVfModuleResponse.class);
	}

	@Override
	public UpdateVfModuleResponse updateVfModule(String aaiVnfId, String aaiVfModuleId, UpdateVfModuleRequest req) {
		return new AdapterRestClient(this.props, this.getUri("/" + aaiVnfId + VF_MODULES + aaiVfModuleId).build())
				.put(req, UpdateVfModuleResponse.class);
	}

	@Override
	public QueryVfModuleResponse queryVfModule(String aaiVnfId, String aaiVfModuleId, String cloudSiteId,
			String tenantId, String vfModuleName, boolean skipAAI, String requestId, String serviceInstanceId) {
		UriBuilder builder = this.getUri("/" + aaiVnfId + VF_MODULES + aaiVfModuleId);
		if (cloudSiteId != null) {
			builder.queryParam("cloudSiteId", cloudSiteId);
		}
		if (tenantId != null) {
			builder.queryParam("tenantId", tenantId);
		}
		if (vfModuleName != null) {
			builder.queryParam("vfModuleName", vfModuleName);
		}

		builder.queryParam("skipAAI", skipAAI);

		if (requestId != null) {
			builder.queryParam("msoRequest.requestId", requestId);
		}
		if (serviceInstanceId != null) {
			builder.queryParam("msoRequest.serviceInstanceId", serviceInstanceId);
		}
		return new AdapterRestClient(this.props, builder.build(), MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON)
				.get(QueryVfModuleResponse.class);
	}

	@Override
	public String healthCheck() {
		return new AdapterRestClient(this.props, this.getUri("/healthcheck").build()).get(String.class);
	}

	public UriBuilder getUri(String path) {
		return UriBuilder.fromPath(path);
	}

}
