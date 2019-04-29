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

import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.CreateVnfRequest;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201;
import org.onap.svnfm.simulator.constants.Constant;
import org.onap.svnfm.simulator.services.SvnfmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
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



    public InlineResponse201 getVnf(final String id) {
        return svnfmService.getVnf(id);
    }

    /**
     * @param vnfId
     * @return
     */
    @CacheEvict(value = Constant.IN_LINE_RESPONSE_201_CACHE, key = "#id")
    public void deleteVnf(final String id) {}
}
