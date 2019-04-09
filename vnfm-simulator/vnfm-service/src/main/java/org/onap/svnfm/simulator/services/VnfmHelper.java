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
import org.apache.commons.beanutils.BeanUtils;
import org.onap.svnfm.simulator.constants.Constant;
import org.onap.svnfm.simulator.model.VnfInstance;
import org.onap.vnfm.v1.model.CreateVnfRequest;
import org.onap.vnfm.v1.model.InlineResponse201;
import org.onap.vnfm.v1.model.InlineResponse201.InstantiationStateEnum;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Lathishbabu Ganesan (lathishbabu.ganesan@est.tech)
 * @author Ronan Kenny (ronan.kenny@est.tech)
 */
@Component
public class VnfmHelper {

    /**
     * 
     * @param createVNFRequest
     * @return vnfInstance
     */
    public VnfInstance createVnfInstance(final CreateVnfRequest createVNFRequest) {
        final VnfInstance vnfInstance = new VnfInstance();
        final String vnfId = createVNFRequest.getVnfdId();
        vnfInstance.setId(vnfId);
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
        return inlineResponse201;
    }
}
