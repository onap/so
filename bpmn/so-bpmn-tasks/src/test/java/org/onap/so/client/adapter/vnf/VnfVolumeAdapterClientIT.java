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

package org.onap.so.client.adapter.vnf;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.UriBuilder;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.onap.so.adapters.vnfrest.CreateVolumeGroupRequest;
import org.onap.so.adapters.vnfrest.CreateVolumeGroupResponse;
import org.onap.so.adapters.vnfrest.DeleteVolumeGroupRequest;
import org.onap.so.adapters.vnfrest.DeleteVolumeGroupResponse;
import org.onap.so.adapters.vnfrest.RollbackVolumeGroupRequest;
import org.onap.so.adapters.vnfrest.RollbackVolumeGroupResponse;
import org.onap.so.adapters.vnfrest.UpdateVolumeGroupRequest;
import org.onap.so.adapters.vnfrest.UpdateVolumeGroupResponse;
import org.onap.so.client.adapter.rest.AdapterRestClient;
import org.onap.so.BaseIntegrationTest;

public class VnfVolumeAdapterClientIT extends BaseIntegrationTest {

    private static final String TESTING_ID = "___TESTING___";
    private static final String AAI_VOLUME_GROUP_ID = "test";
    private static final String CLOUD_SITE_ID = "test";
    private static final String TENANT_ID = "test";
    private static final String VOLUME_GROUP_STACK_ID = "test";
    private static final boolean SKIP_AAI = true;
    private static final String REQUEST_ID = "test";
    private static final String SERVICE_INSTANCE_ID = "test";

    @Test
    public void createVolumeGroupTest() throws VnfAdapterClientException {
        CreateVolumeGroupRequest request = new CreateVolumeGroupRequest();
        request.setCloudSiteId(TESTING_ID);

        CreateVolumeGroupResponse mockResponse = new CreateVolumeGroupResponse();
        mockResponse.setVolumeGroupCreated(true);

        VnfVolumeAdapterClientImpl client = spy(VnfVolumeAdapterClientImpl.class);
        AdapterRestClient mockClient = mock(AdapterRestClient.class);

        doReturn(mockClient).when(client).getAdapterRestClient("");
        when(mockClient.post(request, CreateVolumeGroupResponse.class)).thenReturn(mockResponse);

        CreateVolumeGroupResponse response = client.createVNFVolumes(request);
        assertEquals("Testing CreateVolumeGroup response", mockResponse.getVolumeGroupCreated(),
                response.getVolumeGroupCreated());
    }

    @Test(expected = VnfAdapterClientException.class)
    public void createVolumeGroupTestThrowException() throws VnfAdapterClientException {
        CreateVolumeGroupRequest request = new CreateVolumeGroupRequest();
        request.setCloudSiteId(TESTING_ID);

        CreateVolumeGroupResponse mockResponse = new CreateVolumeGroupResponse();
        mockResponse.setVolumeGroupCreated(true);

        VnfVolumeAdapterClientImpl client = spy(VnfVolumeAdapterClientImpl.class);
        AdapterRestClient mockClient = mock(AdapterRestClient.class);

        doReturn(mockClient).when(client).getAdapterRestClient("");
        when(mockClient.post(request, CreateVolumeGroupResponse.class))
                .thenThrow(new InternalServerErrorException("Error in create volume group"));

        client.createVNFVolumes(request);
    }

    @Test
    public void deleteVolumeGroupTest() throws VnfAdapterClientException {
        DeleteVolumeGroupRequest request = new DeleteVolumeGroupRequest();
        request.setCloudSiteId(TESTING_ID);

        DeleteVolumeGroupResponse mockResponse = new DeleteVolumeGroupResponse();
        mockResponse.setVolumeGroupDeleted(true);

        VnfVolumeAdapterClientImpl client = spy(VnfVolumeAdapterClientImpl.class);
        AdapterRestClient mockClient = mock(AdapterRestClient.class);
        doReturn(mockClient).when(client).getAdapterRestClient("/" + AAI_VOLUME_GROUP_ID);
        when(mockClient.delete(request, DeleteVolumeGroupResponse.class)).thenReturn(mockResponse);
        MockitoAnnotations.openMocks(this);

        DeleteVolumeGroupResponse response = client.deleteVNFVolumes(AAI_VOLUME_GROUP_ID, request);
        assertEquals("Testing DeleteVolumeGroup response", mockResponse.getVolumeGroupDeleted(),
                response.getVolumeGroupDeleted());
    }

    @Test(expected = VnfAdapterClientException.class)
    public void deleteVolumeGroupTestThrowException() throws VnfAdapterClientException {
        DeleteVolumeGroupRequest request = new DeleteVolumeGroupRequest();
        request.setCloudSiteId(TESTING_ID);

        DeleteVolumeGroupResponse mockResponse = new DeleteVolumeGroupResponse();
        mockResponse.setVolumeGroupDeleted(true);

        VnfVolumeAdapterClientImpl client = spy(VnfVolumeAdapterClientImpl.class);
        AdapterRestClient mockClient = mock(AdapterRestClient.class);
        doReturn(mockClient).when(client).getAdapterRestClient("/" + AAI_VOLUME_GROUP_ID);
        when(mockClient.delete(request, DeleteVolumeGroupResponse.class))
                .thenThrow(new InternalServerErrorException("Error in delete volume group"));
        MockitoAnnotations.openMocks(this);

        client.deleteVNFVolumes(AAI_VOLUME_GROUP_ID, request);
    }

    @Test
    public void rollbackVolumeGroupTest() throws VnfAdapterClientException {
        RollbackVolumeGroupRequest request = new RollbackVolumeGroupRequest();

        RollbackVolumeGroupResponse mockResponse = new RollbackVolumeGroupResponse();
        mockResponse.setVolumeGroupRolledBack(true);

        VnfVolumeAdapterClientImpl client = spy(VnfVolumeAdapterClientImpl.class);
        AdapterRestClient mockClient = mock(AdapterRestClient.class);
        doReturn(mockClient).when(client).getAdapterRestClient("/" + AAI_VOLUME_GROUP_ID + "/rollback");
        when(mockClient.delete(request, RollbackVolumeGroupResponse.class)).thenReturn(mockResponse);
        MockitoAnnotations.openMocks(this);

        RollbackVolumeGroupResponse response = client.rollbackVNFVolumes(AAI_VOLUME_GROUP_ID, request);
        assertEquals("Testing RollbackVolumeGroup response", mockResponse.getVolumeGroupRolledBack(),
                response.getVolumeGroupRolledBack());
    }

    @Test(expected = VnfAdapterClientException.class)
    public void rollbackVolumeGroupTestThrowException() throws VnfAdapterClientException {
        RollbackVolumeGroupRequest request = new RollbackVolumeGroupRequest();

        RollbackVolumeGroupResponse mockResponse = new RollbackVolumeGroupResponse();
        mockResponse.setVolumeGroupRolledBack(true);

        VnfVolumeAdapterClientImpl client = spy(VnfVolumeAdapterClientImpl.class);
        AdapterRestClient mockClient = mock(AdapterRestClient.class);
        doReturn(mockClient).when(client).getAdapterRestClient("/" + AAI_VOLUME_GROUP_ID + "/rollback");
        when(mockClient.delete(request, RollbackVolumeGroupResponse.class))
                .thenThrow(new InternalServerErrorException("Error in rollback volume group"));
        MockitoAnnotations.openMocks(this);

        client.rollbackVNFVolumes(AAI_VOLUME_GROUP_ID, request);
    }

    @Test
    public void updateVolumeGroupTest() throws VnfAdapterClientException {
        UpdateVolumeGroupRequest request = new UpdateVolumeGroupRequest();

        UpdateVolumeGroupResponse mockResponse = new UpdateVolumeGroupResponse();
        mockResponse.setVolumeGroupId(AAI_VOLUME_GROUP_ID);

        VnfVolumeAdapterClientImpl client = spy(VnfVolumeAdapterClientImpl.class);
        AdapterRestClient mockClient = mock(AdapterRestClient.class);
        doReturn(mockClient).when(client).getAdapterRestClient("/" + AAI_VOLUME_GROUP_ID);
        when(mockClient.put(request, UpdateVolumeGroupResponse.class)).thenReturn(mockResponse);
        MockitoAnnotations.openMocks(this);

        UpdateVolumeGroupResponse response = client.updateVNFVolumes(AAI_VOLUME_GROUP_ID, request);
        assertEquals("Testing DeleteVfModule response", mockResponse.getVolumeGroupId(), response.getVolumeGroupId());
    }

    @Test(expected = VnfAdapterClientException.class)
    public void updateVolumeGroupTestThrowException() throws VnfAdapterClientException {
        UpdateVolumeGroupRequest request = new UpdateVolumeGroupRequest();

        UpdateVolumeGroupResponse mockResponse = new UpdateVolumeGroupResponse();
        mockResponse.setVolumeGroupId(AAI_VOLUME_GROUP_ID);

        VnfVolumeAdapterClientImpl client = spy(VnfVolumeAdapterClientImpl.class);
        AdapterRestClient mockClient = mock(AdapterRestClient.class);
        doReturn(mockClient).when(client).getAdapterRestClient("/" + AAI_VOLUME_GROUP_ID);
        when(mockClient.put(request, UpdateVolumeGroupResponse.class))
                .thenThrow(new InternalServerErrorException("Error in update volume group"));
        MockitoAnnotations.openMocks(this);

        client.updateVNFVolumes(AAI_VOLUME_GROUP_ID, request);
    }

    public void buildQueryPathTest() {
        String expectedOutput = "/" + AAI_VOLUME_GROUP_ID + "?cloudSiteId=" + CLOUD_SITE_ID + "&tenantId=" + TENANT_ID
                + "&volumeGroupStackId=" + VOLUME_GROUP_STACK_ID + "&skipAAI=" + SKIP_AAI + "&msoRequest.requestId="
                + REQUEST_ID + "&msoRequest.serviceInstanceId=" + SERVICE_INSTANCE_ID;
        VnfVolumeAdapterClientImpl client = new VnfVolumeAdapterClientImpl();
        assertEquals("Test build query path", expectedOutput, client.buildQueryPath(AAI_VOLUME_GROUP_ID, CLOUD_SITE_ID,
                TENANT_ID, VOLUME_GROUP_STACK_ID, SKIP_AAI, REQUEST_ID, SERVICE_INSTANCE_ID));
    }

    protected UriBuilder getUri(String path) {
        return UriBuilder.fromPath(path);
    }
}
