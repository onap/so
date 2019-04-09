/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.so.adapters.vnfmadapter.rest;

import org.onap.so.adapters.vnfmadapter.extclients.aai.AaiHelper;
import org.onap.so.adapters.vnfmadapter.extclients.aai.AaiServiceProvider;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.VnfmHelper;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.model.*;
import org.onap.vnfmadapter.v1.model.Tenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static org.onap.so.adapters.vnfmadapter.Constants.BASE_URL;

@Controller
@RequestMapping(value = BASE_URL, produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
public class Sol003GrantController {

    private static final String SEPARATOR = "_";
    private static final String VIM_TYPE = "OPENSTACK";
    private static final String CLOUD_OWNER = "myTestCloudOwner";
    private static final String REGION = "myTestRegion";
    private static final String TENANT_ID = "myTestTenantId";
    private static final Logger logger = LoggerFactory.getLogger(Sol003GrantController.class);
    public final AaiServiceProvider aaiServiceProvider;
    public final AaiHelper aaiHelper;
    public final VnfmHelper vnfmHelper;

    @Autowired
    public Sol003GrantController(final AaiServiceProvider aaiServiceProvider, final AaiHelper aaiHelper,
            final VnfmHelper vnfmHelper) {
        this.aaiServiceProvider = aaiServiceProvider;
        this.aaiHelper = aaiHelper;
        this.vnfmHelper = vnfmHelper;
    }

    @GetMapping(value = "/grants/{grantId}")
    public ResponseEntity<InlineResponse201> grantsGrantIdGet(@PathVariable("grantId") final String grantId) {
        logger.info("Get grant received from VNFM, grant id: " + grantId);
        return new ResponseEntity<InlineResponse201>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PostMapping(value = "/grants")
    public ResponseEntity<InlineResponse201> grantsPost(@RequestBody final GrantRequest grantRequest) {
        logger.info("Grant request received from VNFM: " + grantRequest);

        final InlineResponse201 grantResponse = createGrantResponse(grantRequest);
        logger.info("Grant request returning to VNFM: " + grantResponse);
        return new ResponseEntity<InlineResponse201>(grantResponse, HttpStatus.CREATED);
    }

    private InlineResponse201 createGrantResponse(final GrantRequest grantRequest) {
        final InlineResponse201 grantResponse = new InlineResponse201();
        grantResponse.setId(UUID.randomUUID().toString());
        grantResponse.setVnfInstanceId(grantRequest.getVnfInstanceId());
        grantResponse.setVnfLcmOpOccId(grantRequest.getVnfLcmOpOccId());
        final Tenant tenant =
                aaiHelper.getAssignedTenant(aaiServiceProvider.invokeGetGenericVnf((grantRequest.getVnfInstanceId())));

        String vimConnectionId = "";
        final InlineResponse201VimConnections vimConnection = vnfmHelper.getVimConnections(tenant);
        grantResponse.addVimConnectionsItem(vimConnection);
        vimConnectionId = vimConnection.getId();

        if (grantRequest.getOperation().equals(GrantRequest.OperationEnum.INSTANTIATE)) {
            grantResponse.addResources(getResources(grantRequest.getAddResources(), vimConnectionId));
        } else if (grantRequest.getOperation().equals(GrantRequest.OperationEnum.TERMINATE)) {
            grantResponse.addResources(getResources(grantRequest.getRemoveResources(), vimConnectionId));
        }
        return grantResponse;
    }

    private InlineResponse201VimConnections getVimConnectionsItem(final Tenant tenant) {
        final InlineResponse201VimConnections vimConnection = new InlineResponse201VimConnections();
        vimConnection.setId(createVimConnectionId(tenant.getCloudOwner(), tenant.getRegionName()));
        vimConnection.setVimId(vimConnection.getId());
        vimConnection.setVimType(VIM_TYPE);
        return vimConnection;
    }

    private List<InlineResponse201AddResources> getResources(final List<GrantsAddResources> requestResources,
            final String vimId) {
        final List<InlineResponse201AddResources> resources = new ArrayList<>();
        for (final GrantsAddResources requestResource : requestResources) {
            final InlineResponse201AddResources responseResource = new InlineResponse201AddResources();
            responseResource.setResourceDefinitionId(requestResource.getId());
            responseResource.setVimConnectionId(vimId);
            resources.add(responseResource);
        }
        return resources;
    }

    private String createVimConnectionId(String cloudOwner, String cloudRegionId) {
        return cloudOwner + SEPARATOR + cloudRegionId;
    }
}
