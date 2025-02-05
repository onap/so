/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Ericsson. All rights reserved.
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
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.Constants.FORWARD_SLASH;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.Constants.PRELOAD_VNFS_URL;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.ExternalVirtualLink;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoGenericVnf;
import org.onap.so.jsonpath.JsonPathUtil;
import org.onap.so.client.sdnc.SDNCClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class retrieve pre-load data from SDNC using <br/>
 * <b>GET</b> /config/VNF-API:preload-vnfs/vnf-preload-list/{vnf-name}/{vnf-type}
 * 
 * @author waqas.ikram@est.tech
 */
@Service
// @Primary // added
public class SdncInputParametersProvider extends AbstractInputParametersProvider<GenericVnf> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SdncInputParametersProvider.class);
    private static final String VNF_PARAMETERS_PATH = "$..vnf-parameters";
    private final SDNCClient sdncClient;
    private final ObjectMapper mapper;

    @Autowired
    public SdncInputParametersProvider(final SDNCClient sdncClient) {
        this.sdncClient = sdncClient;
        this.mapper = new ObjectMapper();
    }

    @Override
    public InputParameter getInputParameter(final GenericVnf genericVnf) {
        final String vnfName = genericVnf.getVnfName();
        final String vnfType = getVnfType(genericVnf);
        final String url = getPreloadVnfsUrl(vnfName, vnfType);
        final InputParameter inputParameter = parseInputParametersUsingUrl(url);

        if (inputParameter != null)
            return inputParameter;
        LOGGER.warn("No input parameters found ...");
        return NullInputParameter.NULL_INSTANCE;
    }

    private List<ExternalVirtualLink> getExtVirtualLinks(final Map<String, String> vnfParametersMap) {
        final String extVirtualLinksString = vnfParametersMap.get(EXT_VIRTUAL_LINKS);

        if (extVirtualLinksString != null && !extVirtualLinksString.isEmpty()) {
            return parseExternalVirtualLinks(extVirtualLinksString);
        }
        return Collections.emptyList();
    }

    private Map<String, String> getAdditionalParameters(final Map<String, String> vnfParametersMap) {
        final String additionalParamsString = vnfParametersMap.get(ADDITIONAL_PARAMS);

        if (additionalParamsString != null && !additionalParamsString.isEmpty()) {
            return parseAdditionalParameters(additionalParamsString);
        }
        return Collections.emptyMap();
    }

    private Map<String, String> getVnfParameterMap(final List<VnfParameter> array) {
        if (array != null) {
            return array.stream().filter(vnfParam -> vnfParam.getName() != null && vnfParam.getValue() != null)
                    .collect(Collectors.toMap(VnfParameter::getName, VnfParameter::getValue));
        }
        return Collections.emptyMap();
    }

    private String getPreloadVnfsUrl(final String vnfName, final String vnfType) {
        return PRELOAD_VNFS_URL + vnfName + FORWARD_SLASH + vnfType;
    }

    private String getVnfType(final GenericVnf genericVnf) {
        final ModelInfoGenericVnf modelInfoGenericVnf = genericVnf.getModelInfoGenericVnf();
        if (modelInfoGenericVnf != null) {
            return modelInfoGenericVnf.getModelName();
        }
        return genericVnf.getVnfType();
    }

    private InputParameter parseInputParametersUsingUrl(String url) {
        try {
            LOGGER.debug("Will query sdnc for input parameters using url: {}", url);
            final String jsonResponse = sdncClient.get(url);
            final String json = JsonPathUtil.getInstance().locateResult(jsonResponse, VNF_PARAMETERS_PATH).orElse(null);
            final InputParameter inputParameter = parseVnfParameters(json);

            if (inputParameter != null)
                return inputParameter;
        } catch (final Exception exception) {
            LOGGER.error("Unable to retrieve/parse input parameters using URL: {} ", url, exception);
        }
        return null;
    }

    private InputParameter parseVnfParameters(String json) {
        try {
            if (json != null) {
                final List<VnfParameter> vnfParametersArray =
                        mapper.readValue(json, new TypeReference<List<VnfParameter>>() {});
                final Map<String, String> vnfParametersMap = getVnfParameterMap(vnfParametersArray);
                final Map<String, String> additionalParameters = getAdditionalParameters(vnfParametersMap);
                final List<ExternalVirtualLink> extVirtualLinks = getExtVirtualLinks(vnfParametersMap);
                final InputParameter inputParameter = new InputParameter(additionalParameters, extVirtualLinks);
                LOGGER.info("InputParameter found in sdnc response : {}", inputParameter);
                return inputParameter;
            }

        } catch (final IOException exception) {
            LOGGER.error("Unable to parse vnf parameters : {}", json, exception);
        }
        return null;
    }
}
