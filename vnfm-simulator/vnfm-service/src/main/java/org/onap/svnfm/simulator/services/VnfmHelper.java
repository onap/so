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

package org.onap.svnfm.simulator.services;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.beanutils.BeanUtils;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.CreateVnfRequest;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201.InstantiationStateEnum;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201Links;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201LinksSelf;
import org.onap.svnfm.simulator.config.ApplicationConfig;
import org.onap.svnfm.simulator.constants.Constant;
import org.onap.svnfm.simulator.model.VnfInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Lathishbabu Ganesan (lathishbabu.ganesan@est.tech)
 * @author Ronan Kenny (ronan.kenny@est.tech)
 */
@Component
public class VnfmHelper {

    @Autowired
    private ApplicationConfig applicationConfig;

    /**
     *
     * @param createVNFRequest
     * @return vnfInstance
     */
    public VnfInstance createVnfInstance(final CreateVnfRequest createVNFRequest, final String id) {
        final VnfInstance vnfInstance = new VnfInstance();
        vnfInstance.setId(id);
        vnfInstance.setVnfInstanceName(createVNFRequest.getVnfInstanceName());
        vnfInstance.setVnfInstanceDescription(createVNFRequest.getVnfInstanceDescription());
        vnfInstance.setVnfdId(createVNFRequest.getVnfdId());
        vnfInstance.setVnfProvider(Constant.VNF_PROVIDER);
        vnfInstance.setVnfProductName(Constant.VNF_PROVIDER_NAME);
        return vnfInstance;
    }

    /**
     *
     * @param vnfInstance
     * @return inlineResponse201
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public InlineResponse201 getInlineResponse201(final VnfInstance vnfInstance)
            throws IllegalAccessException, InvocationTargetException {
        final InlineResponse201 inlineResponse201 = new InlineResponse201();
        BeanUtils.copyProperties(inlineResponse201, vnfInstance);
        inlineResponse201.setVnfdVersion(Constant.VNFD_VERSION);
        inlineResponse201.setVnfSoftwareVersion(Constant.VNF_SOFTWARE_VERSION);
        inlineResponse201.setInstantiationState(InstantiationStateEnum.NOT_INSTANTIATED);
        inlineResponse201.setVnfConfigurableProperties(getConfigProperties());
        addAdditionalPRopertyInlineResponse201(inlineResponse201);
        return inlineResponse201;
    }

    private Map<String, String> getConfigProperties() {
        final Map<String, String> configProperties = new HashMap<>();
        configProperties.put("ipAddress", "10.11.12.13");
        return configProperties;
    }

    private void addAdditionalPRopertyInlineResponse201(final InlineResponse201 inlineResponse201) {
        final InlineResponse201LinksSelf VnfInstancesLinksSelf = new InlineResponse201LinksSelf();
        VnfInstancesLinksSelf
                .setHref(applicationConfig.getBaseUrl() + "/vnflcm/v1/vnf_instances/" + inlineResponse201.getId());
        final InlineResponse201LinksSelf VnfInstancesLinksSelfInstantiate = new InlineResponse201LinksSelf();
        VnfInstancesLinksSelfInstantiate.setHref(applicationConfig.getBaseUrl() + "/vnflcm/v1/vnf_instances/"
                + inlineResponse201.getId() + "/instantiate");
        final InlineResponse201Links inlineResponse201Links = new InlineResponse201Links();
        inlineResponse201Links.setSelf(VnfInstancesLinksSelf);
        inlineResponse201Links.setInstantiate(VnfInstancesLinksSelfInstantiate);
        inlineResponse201.setLinks(inlineResponse201Links);
    }
}
