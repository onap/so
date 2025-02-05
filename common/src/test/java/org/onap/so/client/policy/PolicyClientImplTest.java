/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2019 Nokia.
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.javatuples.Pair;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.onap.so.client.RestClient;
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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class PolicyClientImplTest {

    private static final String SERVICE_TYPE = "serviceTest";
    private static final String VNF_TYPE = "vTypeTest";
    private static final String BB_ID = "bbTest";
    private static final String WORK_STEP = "wStepTest";
    private static final String ERROR_CODE = "errCodeTest";

    @BeforeClass
    public static void setUp() {
        System.setProperty("mso.config.path", "src/test/resources");
    }

    private static String RESOURCE_PATH = "src/test/resources/__files/Policy/";

    @Test
    public void successReadProperties() {
        PolicyRestClient client = new PolicyRestClient(new PolicyRestPropertiesImpl(), PolicyServiceType.GET_DECISION);
        MultivaluedMap<String, Pair<String, String>> map = new MultivaluedHashMap<>();
        client.initializeHeaderMap(map);
        assertTrue("Found expected Client Auth",
                map.get("ALL").contains(Pair.with("ClientAuth", "Basic bTAzNzQzOnBvbGljeVIwY2sk")));
        assertTrue("Found expected Authorization",
                map.get("ALL").contains(Pair.with("Authorization", "Basic dGVzdHBkcDphbHBoYTEyMw==")));
        assertTrue("Found expected Environment", map.get("ALL").contains(Pair.with("Environment", "TEST")));
    }

    @Test
    public void getDecision_success() {
        // given
        PolicyClientImpl testedObject = Mockito.spy(PolicyClientImpl.class);
        PolicyRestClient policyRestClientMock = Mockito.mock(PolicyRestClient.class);
        when(testedObject.getPolicyRestClient(PolicyServiceType.GET_DECISION)).thenReturn(policyRestClientMock);
        // when
        testedObject.getDecision(SERVICE_TYPE, VNF_TYPE, BB_ID, WORK_STEP, ERROR_CODE);
        // then
        ArgumentCaptor<PolicyDecisionRequest> captor1 = ArgumentCaptor.forClass(PolicyDecisionRequest.class);
        verify(policyRestClientMock).post(captor1.capture(), eq(PolicyDecision.class));
        verifyPolicyDecisionRequestArg(captor1.getValue());
    }

    private void verifyPolicyDecisionRequestArg(PolicyDecisionRequest actual) {
        assertThat(actual.getEcompcomponentName()).isEqualTo(RestClient.ECOMP_COMPONENT_NAME);
        DecisionAttributes decisionAttributes = actual.getDecisionAttributes();
        assertThat(decisionAttributes.getServiceType()).isEqualTo(SERVICE_TYPE);
        assertThat(decisionAttributes.getvNFType()).isEqualTo(VNF_TYPE);
        assertThat(decisionAttributes.getBbID()).isEqualTo(BB_ID);
        assertThat(decisionAttributes.getWorkStep()).isEqualTo(WORK_STEP);
        assertThat(decisionAttributes.getErrorCode()).isEqualTo(ERROR_CODE);
    }

    @Test
    public void getAllowedTreatments_success() {
        // given
        PolicyClientImpl testedObject = Mockito.spy(PolicyClientImpl.class);
        PolicyRestClient policyRestClientMock = Mockito.mock(PolicyRestClient.class);
        when(testedObject.getPolicyRestClient(PolicyServiceType.GET_DICTIONARY_ITEMS)).thenReturn(policyRestClientMock);
        when(policyRestClientMock.post(any(DictionaryItemsRequest.class), eq(AllowedTreatments.class)))
                .thenReturn(createAllowedTreatments());
        // when
        DictionaryData dictionaryDataResult = testedObject.getAllowedTreatments(BB_ID, WORK_STEP);
        // then
        assertThat(dictionaryDataResult.getBbid().getString()).isEqualTo(BB_ID);
        assertThat(dictionaryDataResult.getWorkstep().getString()).isEqualTo(WORK_STEP);
    }

    private AllowedTreatments createAllowedTreatments() {
        AllowedTreatments allowedTreatments = new AllowedTreatments();
        DictionaryJson dictionaryJson = new DictionaryJson();
        dictionaryJson.setDictionaryDatas(createDictionaryDataList());
        allowedTreatments.setDictionaryJson(dictionaryJson);
        return allowedTreatments;
    }

    private List<DictionaryData> createDictionaryDataList() {
        DictionaryData dictionaryData = new DictionaryData();
        Bbid bbid = new Bbid();
        bbid.setString(BB_ID);
        dictionaryData.setBbid(bbid);
        Workstep workstep = new Workstep();
        workstep.setString(WORK_STEP);
        dictionaryData.setWorkstep(workstep);
        return Arrays.asList(dictionaryData, new DictionaryData());
    }

    @Test
    public void getConfigFromStringJsonTest() throws IOException {
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
    public void getConfigWithPolicyNameTest() throws IOException {
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
