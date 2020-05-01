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

package org.onap.so.adapters.etsisol003adapter.lcm;

import static org.slf4j.LoggerFactory.getLogger;
import java.util.ArrayList;
import java.util.Collection;
import org.slf4j.Logger;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class NvfmAdapterUtils {
    private static Logger logger = getLogger(NvfmAdapterUtils.class);

    public static JsonObject child(final JsonObject parent, final String name) {
        return childElement(parent, name).getAsJsonObject();
    }

    public static JsonElement childElement(final JsonObject parent, final String name) {
        final JsonElement child = parent.get(name);
        if (child == null) {
            throw abortOperation("Missing child " + name);
        }
        return child;
    }

    public static Collection<JsonObject> children(final JsonObject parent) {
        final ArrayList<JsonObject> childElements = new ArrayList<>();
        for (final String childKey : parent.keySet()) {
            if (parent.get(childKey).isJsonObject()) {
                childElements.add(parent.get(childKey).getAsJsonObject());
            }
        }
        return childElements;
    }

    public static RuntimeException abortOperation(final String msg, final Exception e) {
        logger.error(msg, e);
        return new RuntimeException(msg, e);
    }

    public static RuntimeException abortOperation(final String msg) {
        logger.error(msg);
        return new RuntimeException(msg);
    }
}
