/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Bell Canada
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
package org.onap.so.client.cds;

import com.google.gson.JsonArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigProperties {

    private static final Logger logger = LoggerFactory.getLogger(ConfigProperties.class);

    /**
     *   "config-"(assign/deploy)-(vnf/vf-module/pnf)properties": {
     *      "vnfs/pnfs/vf-modules" : {
     *
     *      }
     *    }
     */
    public static String buildPropertyPayloadFromJsonElements(String action, String scope, JsonArray jsonElements) {

        final StringBuilder propertyBuilder = new StringBuilder();
        propertyBuilder.append("\"");
        propertyBuilder.append("config-").append(action).append("-").append(scope).append("-properties\":{");
        propertyBuilder.append("\"");
        propertyBuilder.append(scope);
        propertyBuilder.append("\":");
        propertyBuilder.append(jsonElements).append("}}");

        logger.info("The property payload is {}", propertyBuilder);

        return propertyBuilder.toString();
    }
}

