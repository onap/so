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

package org.onap.so.client.orchestration;

import java.util.Optional;
import javax.ws.rs.core.UriBuilder;
import org.onap.aaiclient.client.aai.entities.AAIEdgeLabel;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.aai.entities.uri.AAIClientUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;
import org.onap.so.bpmn.common.InjectionHelper;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.client.aai.mapper.AAIObjectMapper;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AAIConfigurationResources {
    @Autowired
    private InjectionHelper injectionHelper;

    @Autowired
    private AAIObjectMapper aaiObjectMapper;

    /**
     * A&AI call to create configuration
     *
     * @param configuration
     */
    public void createConfiguration(Configuration configuration) {
        AAIResourceUri configurationURI = AAIUriFactory
                .createResourceUri(AAIFluentTypeBuilder.network().configuration(configuration.getConfigurationId()));
        configuration.setOrchestrationStatus(OrchestrationStatus.INVENTORIED);
        org.onap.aai.domain.yang.Configuration aaiConfiguration = aaiObjectMapper.mapConfiguration(configuration);
        injectionHelper.getAaiClient().createIfNotExists(configurationURI, Optional.of(aaiConfiguration));
    }

    /**
     * method to get Configuration details from A&AI
     *
     * @param configurationId
     * @return
     */
    public Optional<org.onap.aai.domain.yang.Configuration> getConfiguration(String configurationId) {
        AAIResourceUri aaiResourceUri =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().configuration(configurationId));
        return injectionHelper.getAaiClient().get(org.onap.aai.domain.yang.Configuration.class, aaiResourceUri);
    }

    /**
     * A&AI call to update configuration
     *
     * @param configuration
     */
    public void updateConfiguration(Configuration configuration) {
        AAIResourceUri configurationURI = AAIUriFactory
                .createResourceUri(AAIFluentTypeBuilder.network().configuration(configuration.getConfigurationId()));
        org.onap.aai.domain.yang.Configuration aaiConfiguration = aaiObjectMapper.mapConfiguration(configuration);
        injectionHelper.getAaiClient().update(configurationURI, aaiConfiguration);
    }

    /**
     * A&AI call to disconnect configuration relation with service instance
     *
     * @param configurationId
     * @param serviceInstanceId
     */

    public void disconnectConfigurationToServiceInstance(String configurationId, String serviceInstanceId) {
        AAIResourceUri configurationURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().configuration(configurationId));
        AAIResourceUri serviceInstanceURI =
                AAIClientUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(serviceInstanceId));
        injectionHelper.getAaiClient().disconnect(configurationURI, serviceInstanceURI);
    }

    /**
     * A&AI call to add vrf configuration relationship with Vnr Configuration
     *
     * @param vrfConfigurationId
     * @param vnrConfigurationId
     */
    public void connectVrfConfigurationToVnrConfiguration(String vrfConfigurationId, String vnrConfigurationId) {
        AAIResourceUri vnrConfigurationUri =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().configuration(vnrConfigurationId));
        AAIResourceUri vrfConfigurationUri =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().configuration(vrfConfigurationId));
        injectionHelper.getAaiClient().connect(vrfConfigurationUri, vnrConfigurationUri);
    }

    /**
     * A&AI call to add configuration relationship with PnfObject
     *
     * @param pnfId
     * @param configurationId
     */
    public void connectConfigurationToPnfObject(String pnfId, String configurationId) {
        AAIResourceUri pnfUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().pnf(pnfId));
        AAIResourceUri configurationUri =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().configuration(configurationId));
        injectionHelper.getAaiClient().connect(configurationUri, pnfUri);
    }

    /**
     * A&AI call to add configuration relationship with service instance
     *
     * @param configurationId
     * @param serviceInstanceId
     */
    public void connectConfigurationToServiceInstance(String configurationId, String serviceInstanceId) {
        AAIResourceUri configurationURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().configuration(configurationId));
        AAIResourceUri serviceInstanceURI =
                AAIClientUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(serviceInstanceId));
        injectionHelper.getAaiClient().connect(configurationURI, serviceInstanceURI);
    }

    /**
     * A&AI call to add configuration relationship with service instance
     *
     * @param configurationId
     * @param serviceInstanceId
     * @param aaiLabel
     */
    public void connectConfigurationToServiceInstance(String configurationId, String serviceInstanceId,
            AAIEdgeLabel aaiLabel) {
        AAIResourceUri configurationURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().configuration(configurationId));
        AAIResourceUri serviceInstanceURI =
                AAIClientUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(serviceInstanceId));
        injectionHelper.getAaiClient().connect(configurationURI, serviceInstanceURI, aaiLabel);
    }

    /**
     * A&AI call to add configuration relationship with generic-vnf
     *
     * @param configurationId
     * @param genericVnfId
     */
    public void connectConfigurationToGenericVnf(String configurationId, String genericVnfId) {
        AAIResourceUri configurationURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().configuration(configurationId));
        AAIResourceUri genericVnfURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(genericVnfId));
        injectionHelper.getAaiClient().connect(configurationURI, genericVnfURI);
    }

    /**
     * A&AI call to add configuration relationship with vpn-binding
     *
     * @param configurationId
     * @param vpnId
     *
     */
    public void connectConfigurationToVpnBinding(String configurationId, String vpnId) {
        AAIResourceUri configurationURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().configuration(configurationId));
        AAIResourceUri vpnBindingURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().vpnBinding(vpnId));
        injectionHelper.getAaiClient().connect(configurationURI, vpnBindingURI);
    }

    public void connectConfigurationToVfModule(String configurationId, String vnfId, String vfModuleId) {
        AAIResourceUri configurationURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().configuration(configurationId));
        AAIResourceUri vfModuleURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId).vfModule(vfModuleId));
        injectionHelper.getAaiClient().connect(configurationURI, vfModuleURI);
    }

    public void connectConfigurationToVnfc(String configurationId, String vnfcName) {
        AAIResourceUri configurationURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().configuration(configurationId));
        AAIResourceUri vnfcURI = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().vnfc(vnfcName));
        injectionHelper.getAaiClient().connect(configurationURI, vnfcURI);
    }

    public void connectConfigurationToL3Network(String configurationId, String networkId) {
        AAIResourceUri configurationURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().configuration(configurationId));
        AAIResourceUri networkURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().l3Network(networkId));
        injectionHelper.getAaiClient().connect(configurationURI, networkURI);
    }

    /**
     * method to delete Configuration details in A&AI
     *
     * @param configurationId
     */
    public void deleteConfiguration(String configurationId) {
        AAIResourceUri aaiResourceUri =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().configuration(configurationId));
        injectionHelper.getAaiClient().delete(aaiResourceUri);
    }

    /**
     * method to delete Configuration details in A&AI
     *
     * @param configuration
     */
    public void deleteConfiguration(Configuration configuration) {
        AAIResourceUri aaiResourceUri = AAIUriFactory
                .createResourceUri(AAIFluentTypeBuilder.network().configuration(configuration.getConfigurationId()));
        injectionHelper.getAaiClient().delete(aaiResourceUri);
    }

    /**
     * Get Configuration from AAI using related Link
     *
     * @param relatedLink related link - URI
     * @return AAI Configuration object
     */
    public Optional<org.onap.aai.domain.yang.Configuration> getConfigurationFromRelatedLink(String relatedLink) {
        return injectionHelper.getAaiClient().get(org.onap.aai.domain.yang.Configuration.class, AAIUriFactory
                .createResourceFromExistingURI(Types.CONFIGURATION, UriBuilder.fromPath(relatedLink).build()));
    }

    public void updateOrchestrationStatusConfiguration(Configuration configuration,
            OrchestrationStatus orchestrationStatus) {
        AAIResourceUri aaiResourceUri = AAIUriFactory
                .createResourceUri(AAIFluentTypeBuilder.network().configuration(configuration.getConfigurationId()));
        configuration.setOrchestrationStatus(orchestrationStatus);
        org.onap.aai.domain.yang.Configuration aaiConfiguration = aaiObjectMapper.mapConfiguration(configuration);
        injectionHelper.getAaiClient().update(aaiResourceUri, aaiConfiguration);
    }

    public void updateConfigurationOrchestrationStatus(Configuration configuration,
            OrchestrationStatus orchestrationStatus) {
        AAIResourceUri aaiResourceUri = AAIUriFactory
                .createResourceUri(AAIFluentTypeBuilder.network().configuration(configuration.getConfigurationId()));
        org.onap.aai.domain.yang.Configuration aaiConfiguration = new org.onap.aai.domain.yang.Configuration();
        aaiConfiguration.setOrchestrationStatus(orchestrationStatus.name());
        injectionHelper.getAaiClient().update(aaiResourceUri, aaiConfiguration);
    }

}
