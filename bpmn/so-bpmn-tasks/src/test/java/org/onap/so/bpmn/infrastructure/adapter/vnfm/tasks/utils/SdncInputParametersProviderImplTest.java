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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.Constants.FORWARD_SLASH;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.Constants.PRELOAD_VNFS_URL;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.TestConstants.DUMMY_GENERIC_VND_ID;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.ExternalVirtualLink;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoGenericVnf;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.client.exception.MapperException;
import org.onap.so.client.sdnc.SDNCClient;

/**
 * @author waqas.ikram@est.tech
 */
@RunWith(MockitoJUnitRunner.class)
public class SdncInputParametersProviderImplTest {

    private static final String BASE_DIR = "src/test/resources/__files/";

    private static final Path PRE_LOAD_SDNC_RESPONSE = Paths.get(BASE_DIR + "SDNCClientPrelaodDataResponse.json");

    private static final Path INVALID_PRE_LOAD_SDNC_RESPONSE =
            Paths.get(BASE_DIR + "SDNCClientPrelaodDataResponseWithInvalidData.json");

    private static final Path INVALID_ADDITIONAL_AND_EXT_VM_DATA =
            Paths.get(BASE_DIR + "SDNCClientPrelaodDataResponseWithInvalidAdditionalAndExtVmData.json");


    private static final Path INVALID_VNF_PARAMS =
            Paths.get(BASE_DIR + "SDNCClientPrelaodDataResponseWithInvalidVnfParamsTag.json");


    private static final String MODEL_NAME = "MODEL_NAME";

    private static final String GENERIC_VNF_NAME = "GENERIC_VNF_NAME";

    private static final String URL = PRELOAD_VNFS_URL + GENERIC_VNF_NAME + FORWARD_SLASH + MODEL_NAME;

    private static final String GENERIC_VNF_TYPE = MODEL_NAME;

    @Mock
    private SDNCClient mockedSdncClient;

    @Test
    public void testGetInputParameter_ValidResponseFromSdnc_NotEmptyInputParameter() throws Exception {
        assertValues(getGenericVnf());
    }

    @Test
    public void testGetInputParameter_ValidResponseFromSdncAndVnfType_NotEmptyInputParameter() throws Exception {
        assertValues(getGenericVnf(GENERIC_VNF_TYPE));
    }

    @Test
    public void testGetInputParameter_ValidResponseFromSdncInvalidData_EmptyInputParameter() throws Exception {
        when(mockedSdncClient.get(Mockito.eq(URL))).thenReturn(getReponseAsString(INVALID_PRE_LOAD_SDNC_RESPONSE));
        final InputParametersProvider<GenericVnf> objUnderTest = new SdncInputParametersProvider(mockedSdncClient);
        final InputParameter actual = objUnderTest.getInputParameter(getGenericVnf());
        assertNotNull(actual);
        assertTrue(actual.getAdditionalParams().isEmpty());
        assertTrue(actual.getExtVirtualLinks().isEmpty());
    }

    @Test
    public void testGetInputParameter_ExceptionThrownFromSdnc_EmptyInputParameter() throws Exception {
        when(mockedSdncClient.get(Mockito.eq(URL))).thenThrow(RuntimeException.class);
        final InputParametersProvider<GenericVnf> objUnderTest = new SdncInputParametersProvider(mockedSdncClient);
        final InputParameter actual = objUnderTest.getInputParameter(getGenericVnf());
        assertNotNull(actual);
        assertTrue(actual instanceof NullInputParameter);
        assertTrue(actual.getAdditionalParams().isEmpty());
        assertTrue(actual.getExtVirtualLinks().isEmpty());
    }

    @Test
    public void testGetInputParameter_InvalidResponseData_EmptyInputParameter() throws Exception {
        when(mockedSdncClient.get(Mockito.eq(URL))).thenReturn(getReponseAsString(INVALID_ADDITIONAL_AND_EXT_VM_DATA));
        final InputParametersProvider<GenericVnf> objUnderTest = new SdncInputParametersProvider(mockedSdncClient);
        final InputParameter actual = objUnderTest.getInputParameter(getGenericVnf());
        assertNotNull(actual);
        assertTrue(actual.getAdditionalParams().isEmpty());
        assertTrue(actual.getExtVirtualLinks().isEmpty());
    }

    @Test
    public void testGetInputParameter_EmptyResponseData_EmptyInputParameter() throws Exception {
        when(mockedSdncClient.get(Mockito.eq(URL))).thenReturn("");
        final InputParametersProvider<GenericVnf> objUnderTest = new SdncInputParametersProvider(mockedSdncClient);
        final InputParameter actual = objUnderTest.getInputParameter(getGenericVnf());
        assertNotNull(actual);
        assertTrue(actual instanceof NullInputParameter);
        assertTrue(actual.getAdditionalParams().isEmpty());
        assertTrue(actual.getExtVirtualLinks().isEmpty());
    }

    @Test
    public void testGetInputParameter_InvalidVnfParamsResponseData_EmptyInputParameter() throws Exception {
        when(mockedSdncClient.get(Mockito.eq(URL))).thenReturn(getReponseAsString(INVALID_VNF_PARAMS));
        final InputParametersProvider<GenericVnf> objUnderTest = new SdncInputParametersProvider(mockedSdncClient);
        final InputParameter actual = objUnderTest.getInputParameter(getGenericVnf());
        assertNotNull(actual);
        assertTrue(actual.getAdditionalParams().isEmpty());
        assertTrue(actual.getExtVirtualLinks().isEmpty());
    }

    private void assertValues(final GenericVnf genericVnf) throws MapperException, BadResponseException, IOException {
        when(mockedSdncClient.get(Mockito.eq(URL))).thenReturn(getReponseAsString(PRE_LOAD_SDNC_RESPONSE));
        final InputParametersProvider<GenericVnf> objUnderTest = new SdncInputParametersProvider(mockedSdncClient);
        final InputParameter actual = objUnderTest.getInputParameter(genericVnf);
        assertNotNull(actual);

        final Map<String, String> actualAdditionalParams = actual.getAdditionalParams();
        assertEquals(3, actualAdditionalParams.size());

        final String actualInstanceType = actualAdditionalParams.get("instance_type");
        assertEquals("m1.small", actualInstanceType);

        final List<ExternalVirtualLink> actualExtVirtualLinks = actual.getExtVirtualLinks();
        assertEquals(1, actualExtVirtualLinks.size());

        final ExternalVirtualLink actualExternalVirtualLink = actualExtVirtualLinks.get(0);
        assertEquals("ac1ed33d-8dc1-4800-8ce8-309b99c38eec", actualExternalVirtualLink.getId());
    }

    private String getReponseAsString(final Path filePath) throws IOException {
        return new String(Files.readAllBytes(filePath));
    }

    private GenericVnf getGenericVnf() {
        final GenericVnf genericVnf = getGenericVnf(GENERIC_VNF_TYPE);
        final ModelInfoGenericVnf modelInfoGenericVnf = new ModelInfoGenericVnf();
        modelInfoGenericVnf.setModelName(MODEL_NAME);
        genericVnf.setModelInfoGenericVnf(modelInfoGenericVnf);
        return genericVnf;
    }

    private GenericVnf getGenericVnf(final String vnfType) {
        final GenericVnf genericVnf = new GenericVnf();
        genericVnf.setVnfId(DUMMY_GENERIC_VND_ID);
        genericVnf.setVnfName(GENERIC_VNF_NAME);
        genericVnf.setVnfType(vnfType);
        return genericVnf;
    }
}
