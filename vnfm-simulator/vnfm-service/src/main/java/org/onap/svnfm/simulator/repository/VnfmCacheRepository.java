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

import org.onap.svnfm.simulator.services.SvnfmService;
import org.onap.vnfm.v1.model.CreateVnfRequest;
import org.onap.vnfm.v1.model.InlineResponse201;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Cacheable(value = "inlineResponse201", key = "#createVnfRequest.vnfdId")
    public InlineResponse201 createVnf(final CreateVnfRequest createVnfRequest) {
        return svnfmService.createVnf(createVnfRequest);
    }

    @Cacheable(value = "inlineResponse201", key = "#id")
    public InlineResponse201 getVnf(final String id) {
        return svnfmService.getVnf(id);
    }
  
    /**
     * @param vnfId
     * @return
     */
    public InlineResponse201 deleteVnf(String vnfId) {
        // TODO 
        return null;
    }
}
