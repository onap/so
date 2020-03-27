/*-
 * ============LICENSE_START=======================================================
 * SO
 * ================================================================================
 * Copyright (C) 2020 Samsung. All rights reserved.
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

package org.onap.so.adapters.vevnfm.service;

import org.onap.so.adapters.vevnfm.aai.AaiConnection;
import org.onap.so.adapters.vevnfm.constant.VnfNotificationFilterType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VnfAaiChecker {

    private final AaiConnection aaiConnection;

    @Autowired
    public VnfAaiChecker(final AaiConnection aaiConnection) {
        this.aaiConnection = aaiConnection;
    }

    public boolean vnfCheck(final VnfNotificationFilterType filterType, final String vnfId) {
        switch (filterType) {
            case ALL:
                return true;
            case AAI_CHECKED:
                return aaiConnection.checkGenericVnfId(vnfId);
            case NONE:
                return false;
            default:
                throw new IllegalArgumentException(
                        "The value of VnfNotificationFilterType is not supported: " + filterType);
        }
    }
}
