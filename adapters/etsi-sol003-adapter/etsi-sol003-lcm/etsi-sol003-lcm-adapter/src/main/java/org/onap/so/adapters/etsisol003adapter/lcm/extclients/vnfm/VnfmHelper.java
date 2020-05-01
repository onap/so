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

package org.onap.so.adapters.etsisol003adapter.lcm.extclients.vnfm;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.onap.aai.domain.yang.EsrSystemInfo;
import org.onap.so.adapters.etsi.sol003.adapter.common.VnfmAdapterUrlProvider;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.aai.AaiServiceProvider;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.vim.model.AccessInfo;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.vim.model.InterfaceInfo;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.vim.model.VimCredentials;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.vnfm.model.InstantiateVnfRequest;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.vnfm.model.LccnSubscriptionRequest;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.vnfm.model.SubscriptionsAuthentication;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.vnfm.model.SubscriptionsAuthentication.AuthTypeEnum;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.vnfm.model.SubscriptionsAuthenticationParamsBasic;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.vnfm.model.SubscriptionsAuthenticationParamsOauth2ClientCredentials;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.vnfm.model.SubscriptionsFilter;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.vnfm.model.SubscriptionsFilter.NotificationTypesEnum;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.vnfm.model.SubscriptionsFilterVnfInstanceSubscriptionFilter;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.vnfm.model.VnfInstancesvnfInstanceIdinstantiateExtVirtualLinks;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.vnfm.model.VnfInstancesvnfInstanceIdinstantiateVimConnectionInfo;
import org.onap.so.adapters.etsisol003adapter.lcm.grant.model.InlineResponse201VimConnections;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.CreateVnfRequest;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.ExternalVirtualLink;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.Tenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Provides helper methods for interactions with VNFM.
 */
@Service
public class VnfmHelper {

    private static final Logger logger = LoggerFactory.getLogger(VnfmHelper.class);
    private static final String SEPARATOR = "_";
    private final AaiServiceProvider aaiServiceProvider;
    private final VnfmAdapterUrlProvider vnfmAdapterUrlProvider;

    @Autowired
    public VnfmHelper(final AaiServiceProvider aaiServiceProvider,
            final VnfmAdapterUrlProvider vnfmAdapterUrlProvider) {
        this.aaiServiceProvider = aaiServiceProvider;
        this.vnfmAdapterUrlProvider = vnfmAdapterUrlProvider;
    }

    /**
     * Create an {@link InstantiateVnfRequest} to send in an instantiation request to a VNFM.
     *
     * @param tenant the tenant the request is to be fulfilled on
     * @param createVnfRequest the request received by the VNFM adapter
     */
    public InstantiateVnfRequest createInstantiateRequest(final Tenant tenant, final CreateVnfRequest createVnfRequest,
            final String flavourId) {
        final InstantiateVnfRequest instantiateVnfRequest = new InstantiateVnfRequest();
        instantiateVnfRequest.setFlavourId(flavourId);
        instantiateVnfRequest.setVimConnectionInfo(getVimConnectionInfos(tenant));
        instantiateVnfRequest
                .setAdditionalParams(getAdditionalParametersAsJsonObject(createVnfRequest.getAdditionalParams()));
        instantiateVnfRequest.setExtVirtualLinks(getExternalVirtualLinks(createVnfRequest.getExternalVirtualLinks()));
        createVnfRequest.getExternalVirtualLinks();
        return instantiateVnfRequest;
    }

    private List<VnfInstancesvnfInstanceIdinstantiateVimConnectionInfo> getVimConnectionInfos(final Tenant tenant) {
        final List<VnfInstancesvnfInstanceIdinstantiateVimConnectionInfo> connectionInfos = new ArrayList<>();
        connectionInfos.add(getVimConnectionInfo(tenant));
        return connectionInfos;
    }

    private VnfInstancesvnfInstanceIdinstantiateVimConnectionInfo getVimConnectionInfo(final Tenant tenant) {
        final EsrSystemInfo esrSystemInfo =
                aaiServiceProvider.invokeGetCloudRegionEsrSystemInfoList(tenant.getCloudOwner(), tenant.getRegionName())
                        .getEsrSystemInfo().iterator().next();

        final VnfInstancesvnfInstanceIdinstantiateVimConnectionInfo vnfInstancesVimConnectionInfo =
                new VnfInstancesvnfInstanceIdinstantiateVimConnectionInfo();
        final String vimId = createVimId(tenant.getCloudOwner(), tenant.getRegionName());
        vnfInstancesVimConnectionInfo.setId(vimId);
        vnfInstancesVimConnectionInfo.setVimId(vimId);
        vnfInstancesVimConnectionInfo.setVimType(esrSystemInfo.getType());
        vnfInstancesVimConnectionInfo.setInterfaceInfo(getInterfaceInfo(esrSystemInfo.getServiceUrl()));
        vnfInstancesVimConnectionInfo.setAccessInfo(getAccessInfo(esrSystemInfo, tenant.getTenantId()));
        return vnfInstancesVimConnectionInfo;
    }

    private InterfaceInfo getInterfaceInfo(final String url) {
        final InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setIdentityEndPoint(url);
        return interfaceInfo;
    }

    private AccessInfo getAccessInfo(final EsrSystemInfo esrSystemInfo, final String tenantId) {
        final AccessInfo accessInfo = new AccessInfo();
        accessInfo.setProjectId(tenantId);
        accessInfo.setDomainName(esrSystemInfo.getCloudDomain());

        final VimCredentials vimCredentials = new VimCredentials();
        vimCredentials.setUsername(esrSystemInfo.getUserName());
        vimCredentials.setPassword(esrSystemInfo.getPassword());
        accessInfo.setCredentials(vimCredentials);
        return accessInfo;
    }

    private String createVimId(final String cloudOwner, final String cloudRegion) {
        return cloudOwner + SEPARATOR + cloudRegion;
    }

    private JsonObject getAdditionalParametersAsJsonObject(final Map<String, String> additionalParameters) {
        final JsonObject additionalParametersJsonObject = new JsonObject();
        if (additionalParameters != null) {
            for (final Map.Entry<String, JsonElement> item : new Gson().toJsonTree(additionalParameters)
                    .getAsJsonObject().entrySet()) {
                additionalParametersJsonObject.add(item.getKey(), item.getValue());
            }
        } else {
            logger.warn("No additional parameters were specified for the operation");
        }
        return additionalParametersJsonObject;
    }

    private List<VnfInstancesvnfInstanceIdinstantiateExtVirtualLinks> getExternalVirtualLinks(
            final List<ExternalVirtualLink> extVirtualLinks) {
        if (extVirtualLinks != null) {
            final String extVirtualLinksJsonObject =
                    new Gson().toJson(extVirtualLinks, new TypeToken<List<ExternalVirtualLink>>() {}.getType());
            return new Gson().fromJson(extVirtualLinksJsonObject,
                    new TypeToken<List<VnfInstancesvnfInstanceIdinstantiateExtVirtualLinks>>() {}.getType());
        }
        return null;
    }

    /**
     * Create a {@link LccnSubscriptionRequest} to send in an notification subscription request to a VNFM.
     *
     * @param the ID of the VNF notifications are required for
     * @return the request
     * @throws GeneralSecurityException
     */
    public LccnSubscriptionRequest createNotificationSubscriptionRequest(final String vnfId)
            throws GeneralSecurityException {
        final LccnSubscriptionRequest lccnSubscriptionRequest = new LccnSubscriptionRequest();
        lccnSubscriptionRequest.setAuthentication(getSubscriptionsAuthentication());
        lccnSubscriptionRequest.setCallbackUri(vnfmAdapterUrlProvider.getVnfLcmOperationOccurrenceNotificationUrl());
        final SubscriptionsFilter filter = new SubscriptionsFilter();
        filter.addNotificationTypesItem(NotificationTypesEnum.VNFLCMOPERATIONOCCURRENCENOTIFICATION);
        final SubscriptionsFilterVnfInstanceSubscriptionFilter vnfInstanceSubscriptionFilter =
                new SubscriptionsFilterVnfInstanceSubscriptionFilter();
        vnfInstanceSubscriptionFilter.addVnfInstanceIdsItem(vnfId);
        filter.setVnfInstanceSubscriptionFilter(vnfInstanceSubscriptionFilter);
        lccnSubscriptionRequest.setFilter(filter);
        return lccnSubscriptionRequest;
    }

    private SubscriptionsAuthentication getSubscriptionsAuthentication() throws GeneralSecurityException {
        final SubscriptionsAuthentication authentication = new SubscriptionsAuthentication();

        final ImmutablePair<String, String> decrypedAuth = vnfmAdapterUrlProvider.getDecryptAuth();

        final SubscriptionsAuthenticationParamsOauth2ClientCredentials oauthParams =
                new SubscriptionsAuthenticationParamsOauth2ClientCredentials();
        oauthParams.setTokenEndpoint(vnfmAdapterUrlProvider.getOauthTokenUrl());
        oauthParams.clientId(decrypedAuth.getLeft());
        oauthParams.setClientPassword(decrypedAuth.getRight());
        authentication.addAuthTypeItem(AuthTypeEnum.OAUTH2_CLIENT_CREDENTIALS);
        authentication.paramsOauth2ClientCredentials(oauthParams);

        final SubscriptionsAuthenticationParamsBasic basicAuthParams = new SubscriptionsAuthenticationParamsBasic();
        basicAuthParams.setUserName(decrypedAuth.getLeft());
        basicAuthParams.setPassword(decrypedAuth.getRight());
        authentication.addAuthTypeItem(AuthTypeEnum.BASIC);
        authentication.paramsBasic(basicAuthParams);

        authentication.addAuthTypeItem(AuthTypeEnum.TLS_CERT);
        return authentication;
    }

    /**
     * Get the VIM connections for a tenant
     *
     * @param tenant the tenant
     * @return the VIM connections
     */
    public InlineResponse201VimConnections getVimConnections(final Tenant tenant) {
        final EsrSystemInfo esrSystemInfo =
                aaiServiceProvider.invokeGetCloudRegionEsrSystemInfoList(tenant.getCloudOwner(), tenant.getRegionName())
                        .getEsrSystemInfo().iterator().next();

        final InlineResponse201VimConnections vimConnection = new InlineResponse201VimConnections();
        vimConnection.setId(createVimId(tenant.getCloudOwner(), tenant.getRegionName()));
        vimConnection.setVimId(vimConnection.getId());
        vimConnection.setVimType(esrSystemInfo.getType());
        vimConnection.setInterfaceInfo(getInterfaceInfo(esrSystemInfo.getServiceUrl()));
        vimConnection.setAccessInfo(getAccessInfo(esrSystemInfo, tenant.getTenantId()));
        return vimConnection;
    }


}
