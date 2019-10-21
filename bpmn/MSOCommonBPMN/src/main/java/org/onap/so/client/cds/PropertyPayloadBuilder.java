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

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.onap.so.client.cds.PayloadConstants.PROPERTIES;

public class PropertyPayloadBuilder {

    private static final Logger logger = LoggerFactory.getLogger(PropertyPayloadBuilder.class);

    /**
     * Build property payload.
     *
     * @param action - SO action
     * @param scope - SO scope
     * @param jsonObject - An object comprises configuration properties for CDS.
     * @return "<action>-<scope>-properties":{ // Configuration properties }
     */
    public static String buildConfigProperties(String action, String scope, JsonObject jsonObject) {
        String propertyBuilder = "\"" + action + "-" + scope + PROPERTIES + "\"" + ":" + jsonObject + "}";

        logger.info("The property payload is {}", propertyBuilder);

        return propertyBuilder;
    }
}

