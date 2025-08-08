/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2022 - Samsung Electronics. All rights reserved.
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

package org.onap.so.openstack.utils;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import java.io.File;
import java.io.IOException;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.so.BaseTest;
import org.onap.so.openstack.exceptions.MsoAdapterException;
import org.onap.so.openstack.exceptions.MsoException;
import org.onap.so.openstack.exceptions.MsoExceptionCategory;
import org.onap.so.openstack.exceptions.MsoIOException;
import org.onap.so.openstack.exceptions.MsoOpenstackException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.woorea.openstack.base.client.OpenStackBaseException;
import com.woorea.openstack.base.client.OpenStackConnectException;
import com.woorea.openstack.base.client.OpenStackRequest;
import com.woorea.openstack.base.client.OpenStackResponse;
import com.woorea.openstack.base.client.OpenStackResponseException;
import com.woorea.openstack.heat.model.Explanation;
import com.woorea.openstack.keystone.model.Error;
import com.woorea.openstack.quantum.model.NeutronError;

/**
 * This class implements test methods of the MsoCommonUtils
 */
public class MsoCommonUtilsTest extends BaseTest {
    @Autowired
    @Qualifier("CommonUtils")
    private MsoCommonUtils commonUtils;

    @Mock
    private OpenStackRequest openstackRequest;

    @Test
    public final void testExecuteAndRecordOpenstackRequest() {
        Mockito.when(openstackRequest.endpoint()).thenReturn("localhost");
        Mockito.when(openstackRequest.path()).thenReturn("/test");
        // TODO:Must try a real connection
        assertNull(commonUtils.executeAndRecordOpenstackRequest(openstackRequest));
        assertNull(commonUtils.executeAndRecordOpenstackRequest(openstackRequest, true));
    }

    @Test
    public void testexecuteAndRecordOpenstackRequestResponseException() {
        expectedException.expect(OpenStackResponseException.class);

        doThrow(OpenStackResponseException.class).when(openstackRequest).execute();

        commonUtils.executeAndRecordOpenstackRequest(openstackRequest);
        commonUtils.executeAndRecordOpenstackRequest(openstackRequest, true);
    }

    @Test
    public void testexecuteAndRecordOpenstackRequestConnectException() {
        expectedException.expect(OpenStackConnectException.class);

        doThrow(OpenStackConnectException.class).when(openstackRequest).execute();

        commonUtils.executeAndRecordOpenstackRequest(openstackRequest, true);
    }

    @Test
    public final void testKeystoneErrorToMsoException() throws IOException {
        OpenStackBaseException openStackConnectException = new OpenStackConnectException("connect");

        OpenStackBaseException openStackResponseException = new OpenStackResponseException("response", 1);

        MsoException me = commonUtils.keystoneErrorToMsoException(openStackConnectException, "ContextError");

        assertTrue(me instanceof MsoIOException);
        assertEquals("connect", me.getMessage());


        MsoException me2 = commonUtils.keystoneErrorToMsoException(openStackResponseException, "ContextError");
        assertTrue(me2 instanceof MsoOpenstackException);
        assertEquals("ContextError", me2.getContext());
        assertEquals(MsoExceptionCategory.OPENSTACK, me2.getCategory());


        OpenStackResponse openStackResponse = Mockito.mock(OpenStackResponse.class);
        Error error = mapper.readValue(new File(RESOURCE_PATH + "Error.json"), Error.class);

        doReturn(error).when(openStackResponse).getErrorEntity(Error.class);

        openStackResponseException = new OpenStackResponseException("response", 501, openStackResponse);

        MsoException me3 = commonUtils.keystoneErrorToMsoException(openStackResponseException, "ContextError");

        assertTrue(me3 instanceof MsoOpenstackException);
        assertEquals("1 title: message", me3.toString());
    }

    @Test
    public final void testHeatExceptionToMsoException() throws IOException {
        OpenStackBaseException openStackConnectException = new OpenStackConnectException("connect");

        OpenStackBaseException openStackResponseException = new OpenStackResponseException("response", 1);

        MsoException me = commonUtils.heatExceptionToMsoException(openStackConnectException, "ContextError");

        assertTrue(me instanceof MsoIOException);
        assertEquals("connect", me.getMessage());


        MsoException me2 = commonUtils.heatExceptionToMsoException(openStackResponseException, "ContextError");
        assertTrue(me2 instanceof MsoOpenstackException);
        assertEquals("ContextError", me2.getContext());
        assertEquals(MsoExceptionCategory.OPENSTACK, me2.getCategory());


        OpenStackResponse openStackResponse = Mockito.mock(OpenStackResponse.class);
        Explanation explanation = mapper.readValue(new File(RESOURCE_PATH + "Explanation.json"), Explanation.class);

        doReturn(explanation).when(openStackResponse).getErrorEntity(Explanation.class);

        openStackResponseException = new OpenStackResponseException("response", 501, openStackResponse);

        MsoException me3 = commonUtils.heatExceptionToMsoException(openStackResponseException, "ContextError");

        assertTrue(me3 instanceof MsoOpenstackException);
        assertEquals("1 title: explanation, error.type=null, error.message=null", me3.toString());
    }

    @Test
    public final void testNeutronExceptionToMsoException()
            throws IOException {
        OpenStackBaseException openStackConnectException = new OpenStackConnectException("connect");

        OpenStackBaseException openStackResponseException = new OpenStackResponseException("response", 1);

        MsoException me = commonUtils.neutronExceptionToMsoException(openStackConnectException, "ContextError");

        assertTrue(me instanceof MsoIOException);
        assertEquals("connect", me.getMessage());

        MsoException me2 = commonUtils.neutronExceptionToMsoException(openStackResponseException, "ContextError");
        assertTrue(me2 instanceof MsoOpenstackException);
        assertEquals("ContextError", me2.getContext());
        assertEquals(MsoExceptionCategory.OPENSTACK, me2.getCategory());


        OpenStackResponse openStackResponse = Mockito.mock(OpenStackResponse.class);
        NeutronError explanation = mapper.readValue(new File(RESOURCE_PATH + "NeutronError.json"), NeutronError.class);

        doReturn(explanation).when(openStackResponse).getErrorEntity(NeutronError.class);

        openStackResponseException = new OpenStackResponseException("response", 501, openStackResponse);

        MsoException me3 = commonUtils.neutronExceptionToMsoException(openStackResponseException, "ContextError");

        assertTrue(me3 instanceof MsoOpenstackException);
        assertEquals("501 type: message", me3.toString());
    }

    @Test
    public final void testRuntimeExceptionToMsoException() {
        RuntimeException re = new RuntimeException("runtime");
        MsoException me = commonUtils.runtimeExceptionToMsoException(re, "ContextError");

        assertTrue(me instanceof MsoAdapterException);
        assertEquals("ContextError", me.getContext());
        assertEquals(MsoExceptionCategory.INTERNAL, me.getCategory());
    }

    @Test
    public void testIoExceptionToMsoException() {
        IOException exception = new IOException("IOExceptionTestMessage");

        MsoException msoException = commonUtils.ioExceptionToMsoException(exception, "ContextError");

        assertTrue(msoException instanceof MsoAdapterException);
        assertEquals("ContextError", msoException.getContext());
        assertEquals(MsoExceptionCategory.INTERNAL, msoException.getCategory());
    }
}

