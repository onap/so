/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.policy;

import java.io.IOException;
import java.util.List;
import org.onap.so.client.RestClient;
import org.onap.so.client.RestPropertiesLoader;
import org.onap.so.client.defaultproperties.PolicyRestPropertiesImpl;
import org.onap.so.client.policy.entities.AllowedTreatments;
import org.onap.so.client.policy.entities.Bbid;
import org.onap.so.client.policy.entities.Config;
import org.onap.so.client.policy.entities.ConfigRequestParameters;
import org.onap.so.client.policy.entities.DecisionAttributes;
import org.onap.so.client.policy.entities.DictionaryData;
import org.onap.so.client.policy.entities.DictionaryItemsRequest;
import org.onap.so.client.policy.entities.DictionaryJson;
import org.onap.so.client.policy.entities.PolicyConfig;
import org.onap.so.client.policy.entities.PolicyDecision;
import org.onap.so.client.policy.entities.PolicyDecisionRequest;
import org.onap.so.client.policy.entities.PolicyServiceType;
import org.onap.so.client.policy.entities.Workstep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


public class PolicyClientImpl implements PolicyClient {

    private static Logger logger = LoggerFactory.getLogger(PolicyClientImpl.class);
    private PolicyRestProperties props;
    private static final ObjectMapper mapper = new ObjectMapper();

    public PolicyClientImpl() {
        props = RestPropertiesLoader.getInstance().getNewImpl(PolicyRestProperties.class);
        if (props == null) {
            logger.error("No RestProperty.PolicyRestProperties implementation found on classpath");
            props = new PolicyRestPropertiesImpl();
        }
    }

    public PolicyDecision getDecision(String serviceType, String vnfType, String bbID, String workStep,
            String errorCode) {
        DecisionAttributes decisionAttributes = new DecisionAttributes();
        decisionAttributes.setServiceType(serviceType);
        decisionAttributes.setvNFType(vnfType);
        decisionAttributes.setBbID(bbID);
        decisionAttributes.setWorkStep(workStep);
        decisionAttributes.setErrorCode(errorCode);

        return this.getDecision(decisionAttributes);
    }

    protected PolicyDecision getDecision(DecisionAttributes decisionAttributes) {
        PolicyRestClient client = this.getPolicyRestClient(PolicyServiceType.GET_DECISION);
        PolicyDecisionRequest decisionRequest = new PolicyDecisionRequest();
        decisionRequest.setDecisionAttributes(decisionAttributes);
        decisionRequest.setEcompcomponentName(RestClient.ECOMP_COMPONENT_NAME);

        return client.post(decisionRequest, PolicyDecision.class);
    }

    public DictionaryData getAllowedTreatments(String bbID, String workStep) {
        PolicyRestClient client = this.getPolicyRestClient(PolicyServiceType.GET_DICTIONARY_ITEMS);
        DictionaryItemsRequest dictionaryItemsRequest = new DictionaryItemsRequest();
        dictionaryItemsRequest.setDictionaryType("Decision");
        dictionaryItemsRequest.setDictionary("RainyDayTreatments");
        final AllowedTreatments response = client.post(dictionaryItemsRequest, AllowedTreatments.class);
        final DictionaryJson dictionaryJson = response.getDictionaryJson();
        final List<DictionaryData> dictionaryDataList = dictionaryJson.getDictionaryDatas();
        for (DictionaryData dictData : dictionaryDataList) {
            Bbid bBid = dictData.getBbid();
            Workstep workstep = dictData.getWorkstep();
            String bBidString = bBid.getString();
            String workstepString = workstep.getString();
            if (bbID.equals(bBidString) && workStep.equals(workstepString)) {
                return dictData;
            }
        }
        logger.error("There is no AllowedTreatments with that specified parameter set");
        return null;
    }

    @Override
    public Config getConfigWithPolicyName(String policyName) {
        PolicyRestClient client = this.getPolicyRestClient(PolicyServiceType.GET_CONFIG);
        ConfigRequestParameters configReqParameters = new ConfigRequestParameters();
        configReqParameters.setPolicyName(policyName);
        PolicyConfig[] policyConfigList = client.post(configReqParameters, PolicyConfig[].class);
        PolicyConfig policyConfig = null;
        if (policyConfigList.length > 1) {
            logger.debug("Too many configs for policyName: " + policyName);
            return null;
        }
        try {
            policyConfig = policyConfigList[0];
            return this.getConfigFromStringJson(policyConfig.getConfig());
        } catch (IOException e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    protected Config getConfigFromStringJson(String configJson)
            throws JsonParseException, JsonMappingException, IOException {
        String unescapedJson = configJson.replaceAll("\\\\", "");
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
        return mapper.readValue(unescapedJson, Config.class);
    }

    protected PolicyRestClient getPolicyRestClient(PolicyServiceType policyServiceType) {
        return new PolicyRestClient(this.props, policyServiceType);
    }
}
