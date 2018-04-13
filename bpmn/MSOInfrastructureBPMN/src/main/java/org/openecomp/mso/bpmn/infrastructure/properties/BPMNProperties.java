/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Intellectual Property. All rights reserved.
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
package org.openecomp.mso.bpmn.infrastructure.properties;

import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.properties.MsoPropertiesException;
import org.openecomp.mso.properties.MsoPropertiesFactory;

import java.util.Arrays;
import java.util.List;

public class BPMNProperties {

    public static String MSO_PROP_APIHANDLER_INFRA = "MSO_PROP_APIHANDLER_INFRA";

    private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA);

    public static String getProperty(String key, String defaultValue) {
        String value;
        try {
            value =  new MsoPropertiesFactory().getMsoJavaProperties(MSO_PROP_APIHANDLER_INFRA).getProperty(key, defaultValue);
        } catch (MsoPropertiesException e) {
             msoLogger.error (MessageEnum.NO_PROPERTIES, "Unknown. Mso Properties ID not found in cache: "
                     + MSO_PROP_APIHANDLER_INFRA, "BPMN", "", MsoLogger.ErrorCode.DataError,
                     "Exception - Mso Properties ID not found in cache", e);
            return null;
        }
        msoLogger.debug("Config read for " + MSO_PROP_APIHANDLER_INFRA + " - key:" + key + " value:" + value);
        return value;
    }

    public static List<String> getResourceSequenceProp() {
        String resourceSequence = getProperty("mso.workflow.custom.VolTE.resource.sequence", null);
        if (resourceSequence != null) {
            return Arrays.asList(resourceSequence.split(","));
        }
        return Arrays.asList(ResourceSequence.RESOURCE_EPC,
                ResourceSequence.RESOURCE_IMS,
                ResourceSequence.RESOUCE_OVERLAY,
                ResourceSequence.RESOURCE_UNDERLAY);
    }
}
