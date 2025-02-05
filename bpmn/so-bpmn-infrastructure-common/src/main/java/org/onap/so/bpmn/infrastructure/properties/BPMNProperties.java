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
package org.onap.so.bpmn.infrastructure.properties;

import java.util.Arrays;
import java.util.List;
import org.onap.so.bpmn.core.UrnPropertiesReader;

public class BPMNProperties {

    public static String getProperty(String key, String defaultValue) {
        String value = UrnPropertiesReader.getVariable(key);
        if (value == null) {
            return defaultValue;
        } else {
            return value;
        }
    }

    public static List<String> getResourceSequenceProp(String input) {
        String resourceSequence =
                UrnPropertiesReader.getVariable("mso.workflow.custom." + input + ".resource.sequence");
        if (resourceSequence != null) {
            return Arrays.asList(resourceSequence.split(","));
        }
        return null;
    }
}
