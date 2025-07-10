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
package org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.utils;

import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.Constants.ADDITIONAL_PARAMS;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.Constants.EXT_VIRTUAL_LINKS;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.ExternalVirtualLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Waqas Ikram (waqas.ikram@ericsson.com)
 *
 */
public abstract class AbstractInputParametersProvider<T> implements InputParametersProvider<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractInputParametersProvider.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    protected Map<String, String> parseAdditionalParameters(final String additionalParamsString) {
        try {
            final TypeReference<Map<String, String>> typeRef = new TypeReference<Map<String, String>>() {};
            return mapper.readValue(additionalParamsString, typeRef);
        } catch (final Exception exception) {
            LOGGER.error("Unable to parse {} ", ADDITIONAL_PARAMS, exception);
        }
        return Collections.emptyMap();

    }

    protected List<ExternalVirtualLink> parseExternalVirtualLinks(final String extVirtualLinksString) {
        try {
            final TypeReference<List<ExternalVirtualLink>> extVirtualLinksStringTypeRef =
                    new TypeReference<List<ExternalVirtualLink>>() {};
            return mapper.readValue(extVirtualLinksString, extVirtualLinksStringTypeRef);
        } catch (final Exception exception) {
            LOGGER.error("Unable to parse {} ", EXT_VIRTUAL_LINKS, exception);
        }
        return Collections.emptyList();
    }


}
