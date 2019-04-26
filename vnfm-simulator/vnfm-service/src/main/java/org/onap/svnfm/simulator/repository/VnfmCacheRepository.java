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

package org.onap.svnfm.simulator.repository;

import java.util.List;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.CreateVnfRequest;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201.InstantiationStateEnum;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201InstantiatedVnfInfo;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201VimConnectionInfo;
import org.onap.svnfm.simulator.constants.Constant;
import org.onap.svnfm.simulator.services.SvnfmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Lathishbabu Ganesan (lathishbabu.ganesan@est.tech)
 * @author Ronan Kenny (ronan.kenny@est.tech)
 */
@Repository
public class VnfmCacheRepository {

    @Autowired
    private SvnfmService svnfmService;

    @Cacheable(value = Constant.IN_LINE_RESPONSE_201_CACHE, key = "#id")
    public InlineResponse201 createVnf(final CreateVnfRequest createVnfRequest, final String id) {
        return svnfmService.createVnf(createVnfRequest, id);
    }

    @CachePut(value = Constant.IN_LINE_RESPONSE_201_CACHE, key = "#id")
    public InlineResponse201 updateVnf(final InlineResponse201InstantiatedVnfInfo instantiatedVnfInfo, final String id,
            final List<InlineResponse201VimConnectionInfo> vimConnectionInfo) {
        final InlineResponse201 vnf = getVnf(id);
        vnf.setInstantiatedVnfInfo(instantiatedVnfInfo);
        vnf.setInstantiationState(InstantiationStateEnum.INSTANTIATED);
        vnf.setVimConnectionInfo(vimConnectionInfo);
        return vnf;
    }

    public InlineResponse201 getVnf(final String id) {
        return svnfmService.getVnf(id);
    }

    /**
     * @param vnfId
     * @return
     */
    public InlineResponse201 deleteVnf(final String vnfId) {
        // TODO
        return null;
    }
}
