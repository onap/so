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
import org.onap.aai.domain.yang.VpnBindings;
import org.onap.so.bpmn.common.InjectionHelper;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VpnBinding;
import org.onap.aaiclient.client.aai.AAIObjectPlurals;
import org.onap.aaiclient.client.aai.AAIObjectType;
import org.onap.aaiclient.client.aai.entities.uri.AAIPluralResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.client.aai.mapper.AAIObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AAIVpnBindingResources {
    @Autowired
    private InjectionHelper injectionHelper;

    @Autowired
    private AAIObjectMapper aaiObjectMapper;

    /**
     * @param customer
     */
    public boolean existsCustomer(Customer customer) {
        AAIResourceUri uriCustomer =
                AAIUriFactory.createResourceUri(AAIObjectType.CUSTOMER, customer.getGlobalCustomerId());
        return injectionHelper.getAaiClient().exists(uriCustomer);
    }

    /**
     * @param customerVpnId
     * @return
     */
    public Optional<VpnBindings> getVpnBindingByCustomerVpnId(String customerVpnId) {
        AAIPluralResourceUri aaiVpnBindingsResourceUri = AAIUriFactory.createResourceUri(AAIObjectPlurals.VPN_BINDING)
                .queryParam("customer-vpn-id", customerVpnId);
        return injectionHelper.getAaiClient().get(VpnBindings.class, aaiVpnBindingsResourceUri);

    }

    /**
     * @param vpnBinding
     */
    public void createVpnBinding(VpnBinding vpnBinding) {
        AAIResourceUri aaiVpnBindingResourceUri =
                AAIUriFactory.createResourceUri(AAIObjectType.VPN_BINDING, vpnBinding.getVpnId());
        injectionHelper.getAaiClient().create(aaiVpnBindingResourceUri, aaiObjectMapper.mapVpnBinding(vpnBinding));
    }

    /**
     * @param customer
     */
    public void createCustomer(Customer customer) {
        AAIResourceUri uriCustomer =
                AAIUriFactory.createResourceUri(AAIObjectType.CUSTOMER, customer.getGlobalCustomerId());
        injectionHelper.getAaiClient().create(uriCustomer, aaiObjectMapper.mapCustomer(customer));
    }

    /**
     * Retrieve VPN Binding from AAI using vpn-id
     * 
     * @param vpnId - vpn-id required VPN Binding
     * @return AAI VPN Binding
     */
    public Optional<org.onap.aai.domain.yang.VpnBinding> getVpnBinding(String vpnId) {
        return injectionHelper.getAaiClient().get(org.onap.aai.domain.yang.VpnBinding.class,
                AAIUriFactory.createResourceUri(AAIObjectType.VPN_BINDING, vpnId));
    }


    /**
     * @param globalSubscriberId
     * @param vpnId
     */
    public void connectCustomerToVpnBinding(String globalSubscriberId, String vpnId) {
        AAIResourceUri customerURI = AAIUriFactory.createResourceUri(AAIObjectType.CUSTOMER, globalSubscriberId);
        AAIResourceUri vpnBindingURI = AAIUriFactory.createResourceUri(AAIObjectType.VPN_BINDING, vpnId);
        injectionHelper.getAaiClient().connect(customerURI, vpnBindingURI);
    }
}
