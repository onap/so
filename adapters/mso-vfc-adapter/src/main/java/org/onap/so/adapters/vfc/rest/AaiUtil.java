/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.adapters.vfc.rest;

import org.onap.so.adapters.vfc.model.RestfulResponse;

/**
 * Implement class of operating aai database table <br>
 * <p>
 * </p>
 * 
 * @author
 * @version ONAP Amsterdam Release 2017-08-28
 */
public class AaiUtil {

    public static RestfulResponse addRelation(String globalSubsriberId, String serviceType, String serviceInstanceId,
            String resourceInstanceId) {
        // sent rest to aai to add relation for service and ns.

        return null;
    }

    public static RestfulResponse removeRelation(String globalSubsriberId, String serviceType, String serviceInstanceId,
            String resourceInstanceId) {
        // sent rest to aai to remove relation between service an ns.
        return null;
    }
}
