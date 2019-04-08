/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (c) 2019 Samsung. All rights reserved.
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

package org.onap.so.adapters.vfc.rest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.adapters.vfc.constant.CommonConstant;
import org.onap.so.adapters.vfc.model.NSResourceInputParameter;
import org.onap.so.adapters.vfc.model.NsOperationKey;
import org.onap.so.adapters.vfc.model.NsParameters;
import org.onap.so.adapters.vfc.model.NsScaleParameters;
import org.onap.so.adapters.vfc.model.RestfulResponse;
import org.onap.so.adapters.vfc.util.RestfulUtil;
import org.onap.so.db.request.beans.ResourceOperationStatus;
import org.onap.so.db.request.data.repository.ResourceOperationStatusRepository;
import org.springframework.http.HttpStatus;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VfcManagerTest {

    @Mock
    private ResourceOperationStatusRepository resourceOperationStatusRepository;

    @Mock
    private RestfulUtil restfulUtil;

    @InjectMocks
    @Spy
    private VfcManager vfcManager;

    @Test
    public void createNs() throws Exception {

        NSResourceInputParameter segInput = new NSResourceInputParameter();
        segInput.setNsOperationKey(new NsOperationKey());

        RestfulResponse restfulResponse = new RestfulResponse();
        restfulResponse.setStatus(HttpStatus.OK.value());
        restfulResponse.setResponseContent("{\"" + CommonConstant.NS_INSTANCE_ID + "\": \"someNsInstanceId\"}");
        when(restfulUtil.send(any(), any(), any())).thenReturn(restfulResponse);

        RestfulResponse response = vfcManager.createNs(segInput);
        assertEquals(HttpStatus.OK.value(), restfulResponse.getStatus());
    }

    @Test
    public void deleteNs() throws Exception {

        RestfulResponse restfulResponse = new RestfulResponse();
        restfulResponse.setStatus(HttpStatus.OK.value());
        restfulResponse.setResponseContent("{\"" + CommonConstant.NS_INSTANCE_ID + "\": \"someNsInstanceId\"}");
        when(restfulUtil.send(any(), any(), any())).thenReturn(restfulResponse);

        NsOperationKey nsOperationKey = new NsOperationKey();

        RestfulResponse response = vfcManager.deleteNs(nsOperationKey, "someNsInstanceId");
        assertEquals(HttpStatus.OK.value(), restfulResponse.getStatus());
    }

    @Test
    public void instantiateNs() throws Exception {

        RestfulResponse restfulResponse = new RestfulResponse();
        restfulResponse.setStatus(HttpStatus.OK.value());
        restfulResponse.setResponseContent("{\"" + CommonConstant.JOB_ID + "\": \"someJobId\"}");
        when(restfulUtil.send(any(), any(), any())).thenReturn(restfulResponse);

        NsParameters nsParameters = new NsParameters();
        NSResourceInputParameter segInput = new NSResourceInputParameter();
        segInput.setNsParameters(nsParameters);
        segInput.setNsOperationKey(new NsOperationKey());

        RestfulResponse response = vfcManager.instantiateNs("someNsInstanceId", segInput);
        assertEquals(HttpStatus.OK.value(), restfulResponse.getStatus());
    }

    @Test
    public void terminateNs() throws Exception {

        NsOperationKey nsOperationKey = new NsOperationKey();

        RestfulResponse restfulResponse = new RestfulResponse();
        restfulResponse.setStatus(HttpStatus.OK.value());
        restfulResponse.setResponseContent("{\"" + CommonConstant.JOB_ID + "\": \"someJobId\"}");
        when(restfulUtil.send(any(), any(), any())).thenReturn(restfulResponse);

        RestfulResponse response = vfcManager.terminateNs(nsOperationKey, "someNsInstanceId");
        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }

    @Test
    public void getNsProgress() throws Exception {

        NsOperationKey nsOperationKey = new NsOperationKey();
        ResourceOperationStatus resourceOperationStatus = new ResourceOperationStatus();

        RestfulResponse restfulResponse = new RestfulResponse();
        restfulResponse.setStatus(HttpStatus.OK.value());
        restfulResponse.setResponseContent(
                "{\"" + CommonConstant.JOB_ID + "\": \"someJobId\", " + "\"responseDescriptor\" : {}}");
        when(restfulUtil.send(any(), any(), any())).thenReturn(restfulResponse);

        when(resourceOperationStatusRepository.findOne(any()))
                .thenReturn(java.util.Optional.ofNullable(resourceOperationStatus));

        RestfulResponse response = vfcManager.getNsProgress(nsOperationKey, "someJobId");
        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }

    @Test
    public void scaleNs() throws Exception {

        ResourceOperationStatus resourceOperationStatus = new ResourceOperationStatus();
        NSResourceInputParameter segInput = new NSResourceInputParameter();
        NsScaleParameters nsScaleParameters = new NsScaleParameters();
        segInput.setNsScaleParameters(nsScaleParameters);

        RestfulResponse restfulResponse = new RestfulResponse();
        restfulResponse.setStatus(HttpStatus.OK.value());
        segInput.setNsOperationKey(new NsOperationKey());
        restfulResponse.setResponseContent("{\"" + CommonConstant.JOB_ID + "\": \"someJobId\"}");
        when(restfulUtil.send(any(), any(), any())).thenReturn(restfulResponse);

        when(resourceOperationStatusRepository.findOne(any()))
                .thenReturn(java.util.Optional.ofNullable(resourceOperationStatus));

        RestfulResponse response = vfcManager.scaleNs("someNsInstanceId", segInput);
        assertEquals(HttpStatus.OK.value(), restfulResponse.getStatus());
    }

}
