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

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.openecomp.mso.adapters.vnfrest.CreateVolumeGroupRequest;
import org.openecomp.mso.adapters.vnfrest.CreateVolumeGroupResponse;
import org.openecomp.mso.adapters.vnfrest.DeleteVolumeGroupRequest;
import org.openecomp.mso.adapters.vnfrest.DeleteVolumeGroupResponse;
import org.openecomp.mso.adapters.vnfrest.QueryVolumeGroupResponse;
import org.openecomp.mso.adapters.vnfrest.RollbackVolumeGroupRequest;
import org.openecomp.mso.adapters.vnfrest.RollbackVolumeGroupResponse;
import org.openecomp.mso.adapters.vnfrest.UpdateVolumeGroupRequest;
import org.openecomp.mso.adapters.vnfrest.UpdateVolumeGroupResponse;
import org.openecomp.mso.client.RestClient;
import org.openecomp.mso.client.adapter.rest.AdapterRestClient;
import org.springframework.stereotype.Component;

@Component
public class VnfVolumeAdapterClientImpl implements VnfVolumeAdapterClient {

	private final VnfVolumeAdapterRestProperties props;

	public VnfVolumeAdapterClientImpl() {
		this.props = new VnfVolumeAdapterRestProperties();
	}

	@Override
	public CreateVolumeGroupResponse createVNFVolumes(CreateVolumeGroupRequest req) throws VnfAdapterClientException {
		try {
			return this.getAdapterRestClient("").post(req, CreateVolumeGroupResponse.class);
		} catch (InternalServerErrorException e) {
			throw new VnfAdapterClientException(e.getMessage());
		}
	}

	@Override
	public DeleteVolumeGroupResponse deleteVNFVolumes(String aaiVolumeGroupId, DeleteVolumeGroupRequest req)
			throws VnfAdapterClientException {
		try {
			return this.getAdapterRestClient("/" + aaiVolumeGroupId).delete(req, DeleteVolumeGroupResponse.class);
		} catch (InternalServerErrorException e) {
			throw new VnfAdapterClientException(e.getMessage());
		}
	}

	@Override
	public RollbackVolumeGroupResponse rollbackVNFVolumes(String aaiVolumeGroupId, RollbackVolumeGroupRequest req)
			throws VnfAdapterClientException {
		try {
			return this.getAdapterRestClient("/" + aaiVolumeGroupId + "/rollback").delete(req,
					RollbackVolumeGroupResponse.class);
		} catch (InternalServerErrorException e) {
			throw new VnfAdapterClientException(e.getMessage());
		}
	}

	@Override
	public UpdateVolumeGroupResponse updateVNFVolumes(String aaiVolumeGroupId, UpdateVolumeGroupRequest req)
			throws VnfAdapterClientException {
		try {
			return this.getAdapterRestClient("/" + aaiVolumeGroupId).put(req, UpdateVolumeGroupResponse.class);
		} catch (InternalServerErrorException e) {
			throw new VnfAdapterClientException(e.getMessage());
		}
	}

	@Override
	public QueryVolumeGroupResponse queryVNFVolumes(String aaiVolumeGroupId, String cloudSiteId, String tenantId,
			String volumeGroupStackId, Boolean skipAAI, String requestId, String serviceInstanceId)
			throws VnfAdapterClientException {
		try {
			String path = buildQueryPath(aaiVolumeGroupId, cloudSiteId, tenantId, volumeGroupStackId, skipAAI,
					requestId, serviceInstanceId);
			return this.getAdapterRestClient(path).get(QueryVolumeGroupResponse.class).get();
		} catch (InternalServerErrorException e) {
			throw new VnfAdapterClientException(e.getMessage());
		}
	}

	protected String buildQueryPath(String aaiVolumeGroupId, String cloudSiteId, String tenantId,
			String volumeGroupStackId, Boolean skipAAI, String requestId, String serviceInstanceId) {
		UriBuilder builder = this.getUri("/" + aaiVolumeGroupId);
		builder.queryParam("cloudSiteId", cloudSiteId).queryParam("tenantId", tenantId)
				.queryParam("volumeGroupStackId", volumeGroupStackId).queryParam("skipAAI", skipAAI)
				.queryParam("msoRequest.requestId", requestId)
				.queryParam("msoRequest.serviceInstanceId", serviceInstanceId);
		return builder.toTemplate();
	}

	protected UriBuilder getUri(String path) {
		return UriBuilder.fromPath(path);
	}

	protected RestClient getAdapterRestClient(String path) {
		return new AdapterRestClient(this.props, this.getUri(path).build(), MediaType.APPLICATION_JSON,
				MediaType.APPLICATION_JSON);
	}
}
