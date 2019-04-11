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

import org.onap.aai.domain.yang.EsrSystemInfoList;
import org.onap.aai.domain.yang.EsrVnfm;
import org.onap.aai.domain.yang.EsrVnfmList;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.Vserver;
import org.onap.vnfmadapter.v1.model.Tenant;
import java.util.List;

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
    GenericVnf invokeGetGenericVnf(final String vnfId);

    /**
     * Invoke a query for a generic VNF with the given selfLink
     *
     * @param selfLink the selfLink
     * @return the matching generic vnfs
     */
    List<GenericVnf> invokeQueryGenericVnf(final String selfLink);

    /**
     * Invoke a GET request for the VNFMs.
     *
     * @return the VNFMs
     */
    EsrVnfmList invokeGetVnfms();

    /**
     * Invoke a GET request for the esr system info list for a VNFM.
     *
     * @return the esr system info list for the VNFM
     */
    EsrSystemInfoList invokeGetVnfmEsrSystemInfoList(final String vnfmId);

    /**
     * Invoke a GET request for the a VNFM.
     *
     * @param vnfmId the ID of the VNFM
     * @return the VNFM
     */
    EsrVnfm invokeGetVnfm(final String vnfmId);

    /**
     * Invoke a PUT request for a generic vnf.
     *
     * @param vnf the generic vnf
     * @return
     */
    void invokePutGenericVnf(GenericVnf vnf);

    /**
     * Invoke a PUT request for a vserver.
     *
     * @param cloudOwner the cloud owner
     * @param cloudRegion the cloud region
     * @param tenantId the ID of the tenant
     * @param vserver the vserver
     * @return
     */
    void invokePutVserver(final String cloudOwner, final String cloudRegion, final String tenantId,
            final Vserver vserver);

    /**
     * Invoke a DELETE request for a vserver.
     *
     * @param cloudOwner the cloud owner
     * @param cloudRegion the cloud region
     * @param tenantId the ID of the tenant
     * @param vserverId the ID of the vserver
     * @return
     */
    void invokeDeleteVserver(final String cloudOwner, final String cloudRegion, final String tenantId,
            final String vserverId);

    /**
     * Invoke a GET request for the a tenant.
     *
     * @param cloudOwner the cloud owner
     * @param cloudRegion the cloud region
     * @param tenantId the ID of the tenant
     * @return the tenant
     */
    Tenant invokeGetTenant(final String cloudOwner, final String cloudRegion, final String tenantId);

    /**
     * Invoke a GET request for the esr system info list for a cloud region.
     *
     * @param cloudOwner the cloud owner
     * @param cloudRegion the cloud region
     * @return the esr system info list for the VNFM
     */
    EsrSystemInfoList invokeGetCloudRegionEsrSystemInfoList(final String cloudOwner, final String cloudRegion);

}
