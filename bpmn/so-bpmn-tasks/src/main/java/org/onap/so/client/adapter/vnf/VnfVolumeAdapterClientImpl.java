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

package org.onap.so.client.adapter.vnf;

import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;
import org.onap.so.adapters.vnfrest.CreateVolumeGroupRequest;
import org.onap.so.adapters.vnfrest.CreateVolumeGroupResponse;
import org.onap.so.adapters.vnfrest.DeleteVolumeGroupRequest;
import org.onap.so.adapters.vnfrest.DeleteVolumeGroupResponse;
import org.onap.so.adapters.vnfrest.RollbackVolumeGroupRequest;
import org.onap.so.adapters.vnfrest.RollbackVolumeGroupResponse;
import org.onap.so.adapters.vnfrest.UpdateVolumeGroupRequest;
import org.onap.so.adapters.vnfrest.UpdateVolumeGroupResponse;
import org.onap.so.client.RestClient;
import org.onap.so.client.adapter.rest.AdapterRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VnfVolumeAdapterClientImpl implements VnfVolumeAdapterClient {

    private static final Logger logger = LoggerFactory.getLogger(VnfVolumeAdapterClientImpl.class);

    private final VnfVolumeAdapterRestProperties props;

    public VnfVolumeAdapterClientImpl() {
        this.props = new VnfVolumeAdapterRestProperties();
    }

    @Override
    public CreateVolumeGroupResponse createVNFVolumes(CreateVolumeGroupRequest req) throws VnfAdapterClientException {
        try {
            return this.getAdapterRestClient("").post(req, CreateVolumeGroupResponse.class);
        } catch (InternalServerErrorException e) {
            logger.error("InternalServerErrorException in createVNFVolumes", e);
            throw new VnfAdapterClientException(e.getMessage());
        }
    }

    @Override
    public DeleteVolumeGroupResponse deleteVNFVolumes(String aaiVolumeGroupId, DeleteVolumeGroupRequest req)
            throws VnfAdapterClientException {
        try {
            return this.getAdapterRestClient("/" + aaiVolumeGroupId).delete(req, DeleteVolumeGroupResponse.class);
        } catch (InternalServerErrorException e) {
            logger.error("InternalServerErrorException in deleteVNFVolumes", e);
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
            logger.error("InternalServerErrorException in rollbackVNFVolumes", e);
            throw new VnfAdapterClientException(e.getMessage());
        }
    }

    @Override
    public UpdateVolumeGroupResponse updateVNFVolumes(String aaiVolumeGroupId, UpdateVolumeGroupRequest req)
            throws VnfAdapterClientException {
        try {
            return this.getAdapterRestClient("/" + aaiVolumeGroupId).put(req, UpdateVolumeGroupResponse.class);
        } catch (InternalServerErrorException e) {
            logger.error("InternalServerErrorException in updateVNFVolumes", e);
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
        return builder.build().toString();
    }

    protected UriBuilder getUri(String path) {
        return UriBuilder.fromPath(path);
    }

    protected RestClient getAdapterRestClient(String path) {
        return new AdapterRestClient(props, this.getUri(path).build(), MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_JSON);
    }
}
