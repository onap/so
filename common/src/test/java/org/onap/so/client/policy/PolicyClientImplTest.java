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

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.ArgumentMatchers.any;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.so.client.defaultproperties.PolicyRestPropertiesImpl;
import org.onap.so.client.policy.entities.Config;
import org.onap.so.client.policy.entities.ConfigRequestParameters;
import org.onap.so.client.policy.entities.DictionaryData;
import org.onap.so.client.policy.entities.PolicyConfig;
import org.onap.so.client.policy.entities.PolicyDecision;
import org.onap.so.client.policy.entities.PolicyServiceType;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class PolicyClientImplTest {

    @BeforeClass
    public static void setUp() {
        System.setProperty("mso.config.path", "src/test/resources");
    }

    private static String RESOURCE_PATH = "src/test/resources/__files/Policy/";

    @Test
    public void successReadProperties() {
        PolicyRestClient client = new PolicyRestClient(new PolicyRestPropertiesImpl(), PolicyServiceType.GET_DECISION);
        Map<String, String> map = new HashMap<>();
        client.initializeHeaderMap(map);
        assertEquals("Found expected Client Auth", "Basic bTAzNzQzOnBvbGljeVIwY2sk", map.get("ClientAuth"));
        assertEquals("Found expected Authorization", "Basic dGVzdHBkcDphbHBoYTEyMw==", map.get("Authorization"));
        assertEquals("Found expected Environment", "TEST", map.get("Environment"));
    }

    @Test
    @Ignore
    public void getDecisionTest() {
        PolicyClient client = new PolicyClientImpl();
        PolicyDecision decision = client.getDecision("S", "V", "BB1", "1", "123");
        assertEquals("Decision is correct", decision.getDecision(), "PERMIT");
        assertEquals("Decision details is correct", decision.getDetails(), "Retry");
    }

    @Test
    @Ignore
    public void getAllowedTreatmentsTest() {
        PolicyClient client = new PolicyClientImpl();
        DictionaryData dictClient = client.getAllowedTreatments("BB1", "1");
        final String dictBbidString = dictClient.getBbid().getString();
        final String dictWorkStepString = dictClient.getWorkstep().getString();
        assertEquals("DictionaryData matches a response Bbid", dictBbidString, "BB1");
        assertEquals("DicitonaryData matches a response WorkStep", dictWorkStepString, "1");
    }

    @Test
    public void getDecisionMockTest() {
        String serviceType = "S";
        String vnfType = "V";
        String bbID = "BB1";
        String workStep = "1";
        String errorCode = "123";

        PolicyDecision expected = new PolicyDecision();
        expected.setDecision("PERMIT");
        expected.setDetails("Retry");

        DecisionAttributes decisionAttributes = new DecisionAttributes();
        decisionAttributes.setServiceType(serviceType);
        decisionAttributes.setVNFType(vnfType);
        decisionAttributes.setBBID(bbID);
        decisionAttributes.setWorkStep(workStep);
        decisionAttributes.setErrorCode(errorCode);
        PolicyClient client = Mockito.spy(PolicyClientImpl.class);

        doReturn(expected).when(client).getDecision(serviceType, vnfType, bbID, workStep, errorCode);

        PolicyDecision actual = client.getDecision(serviceType, vnfType, bbID, workStep, errorCode);
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void getConfigFromStringJsonTest() throws JsonParseException, JsonMappingException, IOException {
        PolicyClientImpl client = new PolicyClientImpl();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
        Config expected = mapper.readValue(new File(RESOURCE_PATH + "configJson.json"), Config.class);
        PolicyConfig[] returnedPolicyConfigList =
                mapper.readValue(new File(RESOURCE_PATH + "policyConfig.json"), PolicyConfig[].class);
        String configJson = returnedPolicyConfigList[0].getConfig();
        Config actual = client.getConfigFromStringJson(configJson);

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void getConfigWithPolicyNameTest() throws JsonParseException, JsonMappingException, IOException {
        PolicyClientImpl client = Mockito.spy(PolicyClientImpl.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
        PolicyConfig[] returnedPolicyConfigList =
                mapper.readValue(new File(RESOURCE_PATH + "policyConfig.json"), PolicyConfig[].class);
        Config expected = mapper.readValue(new File(RESOURCE_PATH + "configJson.json"), Config.class);

        PolicyRestClient mockedClient = Mockito.mock(PolicyRestClient.class);
        doReturn(mockedClient).when(client).getPolicyRestClient(PolicyServiceType.GET_CONFIG);
        doReturn(returnedPolicyConfigList).when(mockedClient).post(isA(ConfigRequestParameters.class), any());

        Config actual = client.getConfigWithPolicyName("policyName");

        assertThat(actual, sameBeanAs(expected));

    }
}
