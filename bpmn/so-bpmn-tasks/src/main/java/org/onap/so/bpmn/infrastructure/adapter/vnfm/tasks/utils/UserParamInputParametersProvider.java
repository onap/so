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
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author Waqas Ikram (waqas.ikram@ericsson.com)
 *
 */
@Service
public class UserParamInputParametersProvider extends AbstractInputParametersProvider<Map<String, Object>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserParamInputParametersProvider.class);

    @Override
    public InputParameter getInputParameter(final Map<String, Object> userParams) {
        if (userParams != null) {
            final InputParameter inputParameter = new InputParameter();
            final Object additionalParams = userParams.get(ADDITIONAL_PARAMS);

            if (additionalParams instanceof String) {
                inputParameter.setAdditionalParams(parseAdditionalParameters(additionalParams.toString()));
            }

            final Object extVirtualLinks = userParams.get(EXT_VIRTUAL_LINKS);
            if (extVirtualLinks instanceof String) {
                inputParameter.setExtVirtualLinks(parseExternalVirtualLinks(extVirtualLinks.toString()));
            }
            LOGGER.info("InputParameter found in userParams : {}", inputParameter);
            return inputParameter;
        }
        LOGGER.warn("No input parameters found ...");
        return NullInputParameter.NULL_INSTANCE;
    }

}
