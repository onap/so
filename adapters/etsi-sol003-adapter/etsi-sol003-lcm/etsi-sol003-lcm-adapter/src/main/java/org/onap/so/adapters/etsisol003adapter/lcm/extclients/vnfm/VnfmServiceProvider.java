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

import org.onap.aai.domain.yang.EsrVnfm;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.vnfm.model.CreateVnfRequest;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.vnfm.model.InlineResponse200;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.vnfm.model.InlineResponse2001;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.vnfm.model.InlineResponse201;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.vnfm.model.InstantiateVnfRequest;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.vnfm.model.LccnSubscriptionRequest;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.vnfm.model.TerminateVnfRequest;
import com.google.common.base.Optional;

/**
 * Provides methods for invoking REST calls to a VNFM.
 */
public interface VnfmServiceProvider {

    /**
     * Invoke a get request for a VNF.
     *
     * @param vnfm the VNFM in AAI
     * @param vnfSelfLink the link to the VNF in the VNFM
     * @return the VNF from the VNFM
     */
    Optional<InlineResponse201> getVnf(final EsrVnfm vnfm, final String vnfSelfLink);

    /**
     * Invoke an instantiate request for a VNF.
     *
     * @param vnfm the VNFM in AAI
     * @param vnfSelfLink the link to he VNF on the VNFM
     * @param instantiateVnfRequest the instantiate request
     * @return the operation ID of the instantiation operation
     */
    String instantiateVnf(final EsrVnfm vnfm, final String vnfSelfLink,
            final InstantiateVnfRequest instantiateVnfRequest);

    /**
     * Invoke a notification subscription request to a VNFM.
     *
     * @param vnfm the VNFM in AAI
     * @param subscriptionRequest
     * @return the response to the subscription request
     */
    InlineResponse2001 subscribeForNotifications(final EsrVnfm vnfm, final LccnSubscriptionRequest subscriptionRequest);

    /**
     * Invoke a terminate request for a VNF.
     *
     * @param vnfm the VNFM in AAI
     * @param vnfSelfLink the link to he VNF on the VNFM
     * @param terminateVnfRequest the terminate request
     * @return the operation ID of the termination operation
     */
    String terminateVnf(final EsrVnfm vnfm, final String vnfSelfLink, final TerminateVnfRequest terminateVnfRequest);

    /**
     * Invoke a delete request for a VNF.
     *
     * @param vnfm the VNFM in AAI
     * @param vnfSelfLink the link to he VNF on the VNFM
     * @return the operation ID of the instantiation operation
     */
    void deleteVnf(final EsrVnfm vnfm, final String vnfSelfLink);

    /**
     * Invoke a get request for a VNFM operation.
     *
     * @param vnfm the VNFM in AAI
     * @param operationId the id of the operation on the VNFM
     * @return the operation from the VNFM
     */
    Optional<InlineResponse200> getOperation(final EsrVnfm vnfm, final String operationId);

    /**
     * Invoke a create request to a VNFM
     *
     * @param vnfm the VNFM in AAI
     * @param createVnfRequest the parameters for creating a VNF
     * @return the newly created VNF
     */
    Optional<InlineResponse201> createVnf(final EsrVnfm vnfm, final CreateVnfRequest createVnfRequest);

}
