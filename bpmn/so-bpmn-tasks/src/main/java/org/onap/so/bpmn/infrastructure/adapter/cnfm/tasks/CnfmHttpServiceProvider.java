/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
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
package org.onap.so.bpmn.infrastructure.adapter.cnfm.tasks;

import java.util.Optional;
import org.onap.so.cnfm.lcm.model.AsInstance;
import org.onap.so.cnfm.lcm.model.CreateAsRequest;
import org.onap.so.cnfm.lcm.model.InstantiateAsRequest;

/**
 * @author Sagar Shetty (sagar.shetty@est.tech)
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public interface CnfmHttpServiceProvider {

    Optional<AsInstance> invokeCreateAsRequest(final CreateAsRequest createAsRequest);

    void invokeInstantiateAsRequest(InstantiateAsRequest createAsRequest, String asInstanceId);

}
