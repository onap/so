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
import org.onap.so.adapters.vnfrest.CreateVfModuleRequest;
import org.onap.so.adapters.vnfrest.CreateVfModuleResponse;
import org.onap.so.adapters.vnfrest.DeleteVfModuleRequest;
import org.onap.so.adapters.vnfrest.DeleteVfModuleResponse;
import org.onap.so.adapters.vnfrest.QueryVfModuleResponse;
import org.onap.so.adapters.vnfrest.RollbackVfModuleRequest;
import org.onap.so.adapters.vnfrest.RollbackVfModuleResponse;
import org.onap.so.adapters.vnfrest.UpdateVfModuleRequest;
import org.onap.so.adapters.vnfrest.UpdateVfModuleResponse;
import org.onap.so.client.adapter.rest.AdapterRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VnfAdapterClientImpl implements VnfAdapterClient {

    private static final Logger logger = LoggerFactory.getLogger(VnfAdapterClientImpl.class);

    private static final String VF_MODULES = "/vf-modules/";

    private VnfAdapterRestProperties props;

    public VnfAdapterClientImpl() {
        this.props = new VnfAdapterRestProperties();
    }

    public VnfAdapterClientImpl(VnfAdapterRestProperties props) {
        this.props = props;
    }

    @Override
    public CreateVfModuleResponse createVfModule(String aaiVnfId, CreateVfModuleRequest req)
            throws VnfAdapterClientException {
        try {
            return new AdapterRestClient(this.props, this.getUri("/" + aaiVnfId + "/vf-modules").build()).post(req,
                    CreateVfModuleResponse.class);
        } catch (InternalServerErrorException e) {
            logger.error("InternalServerErrorException in createVfModule", e);
            throw new VnfAdapterClientException(e.getMessage());
        }
    }

    @Override
    public RollbackVfModuleResponse rollbackVfModule(String aaiVnfId, String aaiVfModuleId, RollbackVfModuleRequest req)
            throws VnfAdapterClientException {
        try {
            return new AdapterRestClient(this.props,
                    this.getUri("/" + aaiVnfId + VF_MODULES + aaiVfModuleId + "/rollback").build()).delete(req,
                            RollbackVfModuleResponse.class);
        } catch (InternalServerErrorException e) {
            logger.error("InternalServerErrorException in rollbackVfModule", e);
            throw new VnfAdapterClientException(e.getMessage());
        }
    }

    @Override
    public DeleteVfModuleResponse deleteVfModule(String aaiVnfId, String aaiVfModuleId, DeleteVfModuleRequest req)
            throws VnfAdapterClientException {
        try {
            return new AdapterRestClient(this.props, this.getUri("/" + aaiVnfId + VF_MODULES + aaiVfModuleId).build())
                    .delete(req, DeleteVfModuleResponse.class);
        } catch (InternalServerErrorException e) {
            logger.error("InternalServerErrorException in deleteVfModule", e);
            throw new VnfAdapterClientException(e.getMessage());
        }
    }

    @Override
    public UpdateVfModuleResponse updateVfModule(String aaiVnfId, String aaiVfModuleId, UpdateVfModuleRequest req)
            throws VnfAdapterClientException {
        try {
            return new AdapterRestClient(this.props, this.getUri("/" + aaiVnfId + VF_MODULES + aaiVfModuleId).build())
                    .put(req, UpdateVfModuleResponse.class);
        } catch (InternalServerErrorException e) {
            logger.error("InternalServerErrorException in updateVfModule", e);
            throw new VnfAdapterClientException(e.getMessage());
        }
    }

    @Override
    public QueryVfModuleResponse queryVfModule(String aaiVnfId, String aaiVfModuleId, String cloudSiteId,
            String tenantId, String vfModuleName, boolean skipAAI, String requestId, String serviceInstanceId)
            throws VnfAdapterClientException {
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
        try {
            return new AdapterRestClient(this.props, builder.build(), MediaType.APPLICATION_JSON,
                    MediaType.APPLICATION_JSON).get(QueryVfModuleResponse.class).get();
        } catch (InternalServerErrorException e) {
            logger.error("InternalServerErrorException in queryVfModule", e);
            throw new VnfAdapterClientException(e.getMessage());
        }
    }

    public UriBuilder getUri(String path) {
        return UriBuilder.fromPath(path);
    }

}
