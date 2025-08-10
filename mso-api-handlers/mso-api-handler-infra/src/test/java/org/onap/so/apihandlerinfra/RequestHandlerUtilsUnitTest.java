/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.apihandlerinfra;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.apihandlerinfra.exceptions.VfModuleNotFoundException;
import org.onap.so.constants.Status;
import org.onap.so.db.catalog.beans.VfModule;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.ModelType;

@RunWith(MockitoJUnitRunner.class)
public class RequestHandlerUtilsUnitTest {

    @Mock
    private CatalogDbClient catDbClient;

    @InjectMocks
    @Spy
    private RequestHandlerUtils requestHandler;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static final String CURRENT_REQUEST_ID = "eca3a1b1-43ab-457e-ab1c-367263d148b4";
    private static final String RESUMED_REQUEST_ID = "59c7247f-839f-4923-90c3-05faa3ab354d";
    private static final String SERVICE_INSTANCE_ID = "00032ab7-na18-42e5-965d-8ea592502018";
    private static final String SERVICE_INSTANCE_NAME = "serviceInstanceName";
    private static final String VNF_ID = "00032ab7-na18-42e5-965d-8ea592502017";
    private static final String VFMODULE_ID = "00032ab7-na18-42e5-965d-8ea592502016";
    private static final String NETWORK_ID = "00032ab7-na18-42e5-965d-8ea592502015";
    private static final String VOLUME_GROUP_ID = "00032ab7-na18-42e5-965d-8ea592502014";
    private static final String VNF_NAME = "vnfName";
    private static final String VFMODULE_NAME = "vfModuleName";
    private static final String NETWORK_NAME = "networkName";
    private static final String VOLUME_GROUP_NAME = "volumeGroupName";
    private static final String MODEL_VERSION_ID = "883f4a7a-b5a5-44e0-8738-361c6413d24c";
    private static final String MODEL_VERSION = "1.0";
    private static final String MODEL_INVARIANT_ID = "d358b828-e7f8-4833-ac96-2782bed1a9a9";
    private static final String MODEL_NAME = "modelName";
    private final Timestamp startTimeStamp = new Timestamp(System.currentTimeMillis());
    private String requestUri =
            "http:localhost:6746/onap/so/infra/orchestrationRequests/v7/00032ab7-na18-42e5-965d-8ea592502019/resume";
    private InfraActiveRequests infraActiveRequest = new InfraActiveRequests();
    private InfraActiveRequests currentActiveRequest = new InfraActiveRequests();
    private InfraActiveRequests currentActiveRequestIARNull = new InfraActiveRequests();
    private Action action = Action.createInstance;
    private String vnfType = "vnfType";
    private String sdcServiceModelVersion = "7";

    public String getRequestBody(String request) throws IOException {
        request = "src/test/resources/ResumeOrchestrationRequest" + request;
        return new String(Files.readAllBytes(Paths.get(request)));
    }

    @Before
    public void setup() throws IOException {
        setInfraActiveRequest();
        setCurrentActiveRequest();
        setCurrentActiveRequestNullInfraActive();
    }

    private void setInfraActiveRequest() throws IOException {
        infraActiveRequest.setTenantId("tenant-id");
        infraActiveRequest.setRequestBody(getRequestBody("/RequestBody.json"));
        infraActiveRequest.setCloudRegion("cloudRegion");
        infraActiveRequest.setRequestScope("service");
        infraActiveRequest.setServiceInstanceId(SERVICE_INSTANCE_ID);
        infraActiveRequest.setServiceInstanceName(SERVICE_INSTANCE_NAME);
        infraActiveRequest.setRequestStatus(Status.IN_PROGRESS.toString());
        infraActiveRequest.setRequestAction(Action.createInstance.toString());
        infraActiveRequest.setServiceType("serviceType");
    }

    private void setCurrentActiveRequest() throws IOException {
        currentActiveRequest.setRequestId(CURRENT_REQUEST_ID);
        currentActiveRequest.setSource("VID");
        currentActiveRequest.setStartTime(startTimeStamp);
        currentActiveRequest.setTenantId("tenant-id");
        currentActiveRequest.setRequestBody(getRequestBody("/RequestBodyNewRequestorId.json"));
        currentActiveRequest.setCloudRegion("cloudRegion");
        currentActiveRequest.setRequestScope("service");
        currentActiveRequest.setRequestStatus(Status.IN_PROGRESS.toString());
        currentActiveRequest.setLastModifiedBy(Constants.MODIFIED_BY_APIHANDLER);
        currentActiveRequest.setRequestAction(Action.createInstance.toString());
        currentActiveRequest.setRequestUrl(requestUri);
        currentActiveRequest.setRequestorId("yyyyyy");
        currentActiveRequest.setProgress(new Long(5));
        currentActiveRequest.setOriginalRequestId(RESUMED_REQUEST_ID);
    }

    private void setCurrentActiveRequestNullInfraActive() {
        currentActiveRequestIARNull.setRequestId(CURRENT_REQUEST_ID);
        currentActiveRequestIARNull.setSource("VID");
        currentActiveRequestIARNull.setStartTime(startTimeStamp);
        currentActiveRequestIARNull.setRequestStatus(Status.IN_PROGRESS.toString());
        currentActiveRequestIARNull.setLastModifiedBy(Constants.MODIFIED_BY_APIHANDLER);
        currentActiveRequestIARNull.setRequestUrl(requestUri);
        currentActiveRequestIARNull.setRequestorId("xxxxxx");
        currentActiveRequestIARNull.setProgress(new Long(5));
        currentActiveRequestIARNull.setOriginalRequestId(RESUMED_REQUEST_ID);
    }


    @Test
    public void createNewRecordCopyFromInfraActiveRequestTest() throws IOException, ApiException {
        doNothing().when(requestHandler).setInstanceIdAndName(infraActiveRequest, currentActiveRequest);
        doReturn(getRequestBody("/RequestBodyNewRequestorId.json")).when(requestHandler)
                .updateRequestorIdInRequestBody(infraActiveRequest, "yyyyyy");
        InfraActiveRequests result = requestHandler.createNewRecordCopyFromInfraActiveRequest(infraActiveRequest,
                CURRENT_REQUEST_ID, startTimeStamp, "VID", requestUri, "yyyyyy", RESUMED_REQUEST_ID);
        assertThat(currentActiveRequest, sameBeanAs(result));
    }

    @Test
    public void createNewRecordCopyFromInfraActiveRequestNullIARTest() throws ApiException {
        InfraActiveRequests result = requestHandler.createNewRecordCopyFromInfraActiveRequest(null, CURRENT_REQUEST_ID,
                startTimeStamp, "VID", requestUri, "xxxxxx", RESUMED_REQUEST_ID);
        assertThat(currentActiveRequestIARNull, sameBeanAs(result));
    }

    @Test
    public void setInstanceIdAndNameServiceTest() throws ApiException {
        InfraActiveRequests serviceRequest = new InfraActiveRequests();

        InfraActiveRequests expected = new InfraActiveRequests();
        expected.setServiceInstanceId(SERVICE_INSTANCE_ID);
        expected.setServiceInstanceName(SERVICE_INSTANCE_NAME);

        requestHandler.setInstanceIdAndName(infraActiveRequest, serviceRequest);
        assertThat(serviceRequest, sameBeanAs(expected));
    }

    @Test
    public void setInstanceIdAndNameServiceNullInstanceNameTest() throws ApiException {
        InfraActiveRequests serviceRequest = new InfraActiveRequests();

        InfraActiveRequests request = new InfraActiveRequests();
        request.setServiceInstanceId(SERVICE_INSTANCE_ID);
        request.setRequestScope(ModelType.service.toString());

        InfraActiveRequests expected = new InfraActiveRequests();
        expected.setServiceInstanceId(SERVICE_INSTANCE_ID);

        requestHandler.setInstanceIdAndName(request, serviceRequest);
        assertThat(serviceRequest, sameBeanAs(expected));
    }

    @Test
    public void setInstanceIdAndNameServiceNullInstanceNameVfModuleTest() throws ApiException {
        InfraActiveRequests vfModuleRequest = new InfraActiveRequests();
        String errorMessage =
                "vfModule for requestId: 59c7247f-839f-4923-90c3-05faa3ab354d being resumed does not have an instanceName.";
        doNothing().when(requestHandler).updateStatus(vfModuleRequest, Status.FAILED, errorMessage);

        InfraActiveRequests request = new InfraActiveRequests();
        request.setServiceInstanceId(VFMODULE_ID);
        request.setRequestScope(ModelType.vfModule.toString());
        request.setRequestId(RESUMED_REQUEST_ID);

        thrown.expect(ValidateException.class);
        thrown.expectMessage(errorMessage);

        requestHandler.setInstanceIdAndName(request, vfModuleRequest);
    }

    @Test
    public void setInstanceIdAndNameRequestScopeNotValidTest() throws ApiException {
        InfraActiveRequests originalServiceRequest = new InfraActiveRequests();
        originalServiceRequest.setRequestScope("test");
        InfraActiveRequests serviceRequest = new InfraActiveRequests();

        InfraActiveRequests expected = new InfraActiveRequests();

        requestHandler.setInstanceIdAndName(originalServiceRequest, serviceRequest);
        assertThat(serviceRequest, sameBeanAs(expected));
    }

    @Test
    public void setInstanceIdAndNameVnfTest() throws ApiException {
        InfraActiveRequests vnfRequestOriginal = new InfraActiveRequests();
        vnfRequestOriginal.setRequestScope("vnf");
        vnfRequestOriginal.setVnfId(VNF_ID);
        vnfRequestOriginal.setVnfName(VNF_NAME);
        InfraActiveRequests vnfRequest = new InfraActiveRequests();

        InfraActiveRequests expected = new InfraActiveRequests();
        expected.setVnfId(VNF_ID);
        expected.setVnfName(VNF_NAME);

        requestHandler.setInstanceIdAndName(vnfRequestOriginal, vnfRequest);
        assertThat(vnfRequest, sameBeanAs(expected));
    }

    @Test
    public void setInstanceIdAndNameVfModuleTest() throws ApiException {
        InfraActiveRequests vfModuleRequestOriginal = new InfraActiveRequests();
        vfModuleRequestOriginal.setRequestScope("vfModule");
        vfModuleRequestOriginal.setVfModuleId(VFMODULE_ID);
        vfModuleRequestOriginal.setVfModuleName(VFMODULE_NAME);
        InfraActiveRequests vfModuleRequest = new InfraActiveRequests();

        InfraActiveRequests expected = new InfraActiveRequests();
        expected.setVfModuleId(VFMODULE_ID);
        expected.setVfModuleName(VFMODULE_NAME);

        requestHandler.setInstanceIdAndName(vfModuleRequestOriginal, vfModuleRequest);
        assertThat(vfModuleRequest, sameBeanAs(expected));
    }

    @Test
    public void setInstanceIdAndNameNetworkTest() throws ApiException {
        InfraActiveRequests networkRequestOriginal = new InfraActiveRequests();
        networkRequestOriginal.setRequestScope("network");
        networkRequestOriginal.setNetworkId(NETWORK_ID);
        networkRequestOriginal.setNetworkName(NETWORK_NAME);
        InfraActiveRequests networkRequest = new InfraActiveRequests();

        InfraActiveRequests expected = new InfraActiveRequests();
        expected.setNetworkId(NETWORK_ID);
        expected.setNetworkName(NETWORK_NAME);

        requestHandler.setInstanceIdAndName(networkRequestOriginal, networkRequest);
        assertThat(networkRequest, sameBeanAs(expected));
    }

    @Test
    public void setInstanceIdAndNameVolumeGroupTest() throws ApiException {
        InfraActiveRequests volumeGroupRequestOriginal = new InfraActiveRequests();
        volumeGroupRequestOriginal.setRequestScope("volumeGroup");
        volumeGroupRequestOriginal.setVolumeGroupId(VOLUME_GROUP_ID);
        volumeGroupRequestOriginal.setVolumeGroupName(VOLUME_GROUP_NAME);
        InfraActiveRequests volumeGroupRequest = new InfraActiveRequests();

        InfraActiveRequests expected = new InfraActiveRequests();
        expected.setVolumeGroupId(VOLUME_GROUP_ID);
        expected.setVolumeGroupName(VOLUME_GROUP_NAME);

        requestHandler.setInstanceIdAndName(volumeGroupRequestOriginal, volumeGroupRequest);
        assertThat(volumeGroupRequest, sameBeanAs(expected));
    }

    @Test
    public void getIsBaseVfModuleTrueTest() throws ApiException {
        VfModule vfModule = new VfModule();
        vfModule.setIsBase(true);
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelVersionId(MODEL_VERSION_ID);

        doReturn(vfModule).when(catDbClient).getVfModuleByModelUUID(MODEL_VERSION_ID);
        Boolean expected = true;

        Boolean result = requestHandler.getIsBaseVfModule(modelInfo, action, vnfType, sdcServiceModelVersion,
                currentActiveRequest);
        assertEquals(result, expected);
    }

    @Test
    public void getIsBaseVfModuleFalseTest() throws ApiException {
        VfModule vfModule = new VfModule();
        vfModule.setIsBase(false);
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelVersionId(MODEL_VERSION_ID);

        doReturn(vfModule).when(catDbClient).getVfModuleByModelUUID(MODEL_VERSION_ID);
        Boolean expected = false;

        Boolean result = requestHandler.getIsBaseVfModule(modelInfo, action, vnfType, sdcServiceModelVersion,
                currentActiveRequest);
        assertEquals(result, expected);
    }

    @Test
    public void getIsBaseVfModuleNullTest() throws ApiException {
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelVersionId(MODEL_VERSION_ID);
        modelInfo.setModelName(MODEL_NAME);
        String errorMessage =
                "VnfType vnfType and VF Module Model Name modelName with version 7 not found in MSO Catalog DB";

        doNothing().when(requestHandler).updateStatus(currentActiveRequest, Status.FAILED, errorMessage);
        doReturn(null).when(catDbClient).getVfModuleByModelUUID(MODEL_VERSION_ID);

        thrown.expect(VfModuleNotFoundException.class);
        thrown.expectMessage(errorMessage);
        requestHandler.getIsBaseVfModule(modelInfo, action, vnfType, sdcServiceModelVersion, currentActiveRequest);
    }

    @Test
    public void getIsBaseVfModuleModelVersionIdNullTest() throws ApiException {
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelInvariantId(MODEL_INVARIANT_ID);
        modelInfo.setModelVersion(MODEL_VERSION);
        modelInfo.setModelName(MODEL_NAME);
        Boolean expected = false;

        doReturn(null).when(catDbClient).getVfModuleByModelInvariantUUIDAndModelVersion(MODEL_INVARIANT_ID,
                MODEL_VERSION);

        Boolean result = requestHandler.getIsBaseVfModule(modelInfo, Action.deleteInstance, vnfType,
                sdcServiceModelVersion, currentActiveRequest);
        assertEquals(result, expected);
    }

    @Test
    public void getIsBaseVfModuleModelInfoNotSetTest() throws ApiException {
        ModelInfo modelInfo = new ModelInfo();
        Boolean expected = false;

        Boolean result = requestHandler.getIsBaseVfModule(modelInfo, Action.deleteInstance, vnfType,
                sdcServiceModelVersion, currentActiveRequest);
        assertEquals(result, expected);
    }

    @Test
    public void getIsBaseVfModuleModelVersionNullTest() throws ApiException {
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelInvariantId(MODEL_INVARIANT_ID);
        modelInfo.setModelName(MODEL_NAME);
        Boolean expected = false;

        Boolean result = requestHandler.getIsBaseVfModule(modelInfo, Action.deleteInstance, vnfType,
                sdcServiceModelVersion, currentActiveRequest);
        assertEquals(result, expected);
    }

    @Test
    public void getIsBaseVfModuleModelVersionNullUpdateTest() throws ApiException {
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelInvariantId(MODEL_INVARIANT_ID);
        modelInfo.setModelName(MODEL_NAME);
        String errorMessage = "VnfType vnfType and VF Module Model Name modelName not found in MSO Catalog DB";

        doNothing().when(requestHandler).updateStatus(currentActiveRequest, Status.FAILED, errorMessage);

        thrown.expect(VfModuleNotFoundException.class);
        thrown.expectMessage(errorMessage);
        requestHandler.getIsBaseVfModule(modelInfo, Action.updateInstance, vnfType, null, currentActiveRequest);
    }

    @Test
    public void getIsBaseVfModulesdcModelVersionEmptyTest() throws ApiException {
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelInvariantId(MODEL_INVARIANT_ID);
        modelInfo.setModelName(MODEL_NAME);
        String errorMessage = "VnfType vnfType and VF Module Model Name modelName not found in MSO Catalog DB";

        doNothing().when(requestHandler).updateStatus(currentActiveRequest, Status.FAILED, errorMessage);

        thrown.expect(VfModuleNotFoundException.class);
        thrown.expectMessage(errorMessage);
        requestHandler.getIsBaseVfModule(modelInfo, Action.updateInstance, vnfType, "", currentActiveRequest);
    }

    @Test
    public void getModelTypeApplyUpdatedConfigTest() {
        ModelType modelTypeExpected = ModelType.vnf;

        ModelType modelTypeResult = requestHandler.getModelType(Action.applyUpdatedConfig, null);
        assertEquals(modelTypeResult, modelTypeExpected);
    }

    @Test
    public void getModelTypeInPlaceSoftwareUpdateTest() {
        ModelType modelTypeExpected = ModelType.vnf;

        ModelType modelTypeResult = requestHandler.getModelType(Action.inPlaceSoftwareUpdate, null);
        assertEquals(modelTypeResult, modelTypeExpected);
    }

    @Test
    public void getModelTypeAddMembersTest() {
        ModelType modelTypeExpected = ModelType.instanceGroup;

        ModelType modelTypeResult = requestHandler.getModelType(Action.addMembers, null);
        assertEquals(modelTypeResult, modelTypeExpected);
    }

    @Test
    public void getModelTypeRemoveMembersTest() {
        ModelType modelTypeExpected = ModelType.instanceGroup;

        ModelType modelTypeResult = requestHandler.getModelType(Action.removeMembers, null);
        assertEquals(modelTypeResult, modelTypeExpected);
    }

    @Test
    public void getModelTypeTest() {
        ModelType modelTypeExpected = ModelType.service;
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelType(ModelType.service);

        ModelType modelTypeResult = requestHandler.getModelType(Action.createInstance, modelInfo);
        assertEquals(modelTypeResult, modelTypeExpected);
    }

    @Test
    public void updateRequestorIdInRequestBodyTest() throws IOException {
        String newRequestorId = "yyyyyy";
        String expected = getRequestBody("/RequestBodyNewRequestorId.json");
        String result = requestHandler.updateRequestorIdInRequestBody(infraActiveRequest, newRequestorId);
        assertEquals(expected, result);
    }

    @Test
    public void checkForDuplicateRequestsTest() throws ApiException {
        InfraActiveRequests currentActiveReq = new InfraActiveRequests();
        currentActiveReq.setCloudRegion("testRegion");
        currentActiveReq.setRequestId("792a3158-d9a3-49fd-b3ac-ab09842d6a1a");
        Action action = Action.createInstance;
        String requestScope = ModelType.service.toString();

        InfraActiveRequests duplicate = new InfraActiveRequests();

        HashMap<String, String> instanceIdMap = new HashMap<String, String>();
        String instanceName = "instanceName";

        doReturn(duplicate).when(requestHandler).duplicateCheck(action, instanceIdMap, instanceName, requestScope,
                currentActiveReq);
        doReturn(true).when(requestHandler).camundaHistoryCheck(duplicate, currentActiveReq);
        doNothing().when(requestHandler).buildErrorOnDuplicateRecord(currentActiveReq, action, instanceIdMap,
                instanceName, requestScope, duplicate);

        requestHandler.checkForDuplicateRequests(action, instanceIdMap, requestScope, currentActiveReq, instanceName);
        verify(requestHandler).buildErrorOnDuplicateRecord(currentActiveReq, action, instanceIdMap, instanceName,
                requestScope, duplicate);
    }

}
