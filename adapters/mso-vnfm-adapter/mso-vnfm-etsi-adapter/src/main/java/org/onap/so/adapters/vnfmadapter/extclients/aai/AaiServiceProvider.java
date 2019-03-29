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

package org.onap.so.adapters.vnfmadapter.extclients.aai;

import com.google.common.base.Optional;
import org.onap.so.adapters.vnfmadapter.extclients.aai.model.EsrVnfm;
import org.onap.so.adapters.vnfmadapter.extclients.aai.model.EsrVnfmList;
import org.onap.so.adapters.vnfmadapter.extclients.aai.model.EsrvnfmEsrsysteminfolist;
import org.onap.so.adapters.vnfmadapter.extclients.aai.model.GenericVnf;

/**
 * Provides methods for invoking REST calls to AAI.
 */
public interface AaiServiceProvider {

    /**
     * Invoke a get request for a generic VNF.
     *
     * @param vnfId the VNF id
     * @return the generic VNF
     */
    Optional<GenericVnf> invokeGetGenericVnf(final String vnfId);

    /**
     * Invoke a GET request for the VNFMs.
     *
     * @return the VNFMs
     */
    Optional<EsrVnfmList> invokeGetVnfms();

    /**
     * Invoke a GET request for the esr system info list for a VNFM.
     *
     * @return the esr system info list for the VNFM
     */
    Optional<EsrvnfmEsrsysteminfolist> invokeGetVnfmEsrSystemInfoList(final String vnfmId);


    /**
     * Invoke a GET request for the a VNFM.
     *
     * @param vnfmId the ID of the VNFM
     * @return the VNFM
     */
    Optional<EsrVnfm> invokeGetVnfm(final String vnfmId);

    /**
     * Invoke a PUT request for a generic vnf.
     *
     * @param vnf the generic vnf
     * @return
     */
    Optional<Void> invokePutGenericVnf(GenericVnf vnf);

    /**
     * Invoke a GET request for a resource path
     *
     * @param <T>
     * @param clazz the class for the resource
     *
     * @return the resource
     */
    <T> Optional<T> invokeGet(String aaiResourcePath, final Class<T> clazz);


}
