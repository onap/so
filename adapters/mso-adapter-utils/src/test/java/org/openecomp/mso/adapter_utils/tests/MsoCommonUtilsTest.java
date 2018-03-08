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

package org.openecomp.mso.adapter_utils.tests;


import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import org.openecomp.mso.logger.MsoAlarmLogger;
import org.openecomp.mso.openstack.exceptions.MsoAdapterException;
import org.openecomp.mso.openstack.exceptions.MsoException;
import org.openecomp.mso.openstack.exceptions.MsoExceptionCategory;
import org.openecomp.mso.openstack.exceptions.MsoIOException;
import org.openecomp.mso.openstack.exceptions.MsoOpenstackException;
import org.openecomp.mso.openstack.utils.MsoCommonUtils;
import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.properties.MsoPropertiesException;
import org.openecomp.mso.properties.MsoPropertiesFactory;
import com.woorea.openstack.base.client.OpenStackBaseException;
import com.woorea.openstack.base.client.OpenStackConnectException;
import com.woorea.openstack.base.client.OpenStackRequest;
import com.woorea.openstack.base.client.OpenStackResponseException;


/**
 * This class implements test methods of the MsoCommonUtils
 */
public class MsoCommonUtilsTest extends MsoCommonUtils {

    public static MsoPropertiesFactory msoPropertiesFactory = new MsoPropertiesFactory();

    @Test
    public final void testExecuteAndRecordOpenstackRequest() {
        OpenStackRequest openstackRequest = Mockito.mock(OpenStackRequest.class);
        Mockito.when(openstackRequest.endpoint()).thenReturn("localhost");
        Mockito.when(openstackRequest.path()).thenReturn("/test");
        //TODO:Must try a real connection
        assertNull(super.executeAndRecordOpenstackRequest(openstackRequest));

    }

    @Test
    public final void testKeystoneErrorToMsoException() {
        OpenStackBaseException openStackConnectException = new OpenStackConnectException("connect");

        OpenStackBaseException openStackResponseException = new OpenStackResponseException("response", 1);

        MsoException me = super.keystoneErrorToMsoException(openStackConnectException, "ContextError");

        assertTrue(me instanceof MsoIOException);
        assertTrue("connect".equals(me.getMessage()));


        MsoException me2 = super.keystoneErrorToMsoException(openStackResponseException, "ContextError");
        assertTrue(me2 instanceof MsoOpenstackException);
        assertTrue("ContextError".equals(me2.getContext()));
        assertTrue(MsoExceptionCategory.OPENSTACK.equals(me2.getCategory()));

    }

    @Test
    public final void testHeatExceptionToMsoException() {
        OpenStackBaseException openStackConnectException = new OpenStackConnectException("connect");

        OpenStackBaseException openStackResponseException = new OpenStackResponseException("response", 1);

        MsoException me = super.heatExceptionToMsoException(openStackConnectException, "ContextError");

        assertTrue(me instanceof MsoIOException);
        assertTrue("connect".equals(me.getMessage()));


        MsoException me2 = super.heatExceptionToMsoException(openStackResponseException, "ContextError");
        assertTrue(me2 instanceof MsoOpenstackException);
        assertTrue("ContextError".equals(me2.getContext()));
        assertTrue(MsoExceptionCategory.OPENSTACK.equals(me2.getCategory()));
    }

    @Test
    public final void testNeutronExceptionToMsoException() {
        OpenStackBaseException openStackConnectException = new OpenStackConnectException("connect");

        OpenStackBaseException openStackResponseException = new OpenStackResponseException("response", 1);

        MsoException me = super.neutronExceptionToMsoException(openStackConnectException, "ContextError");

        assertTrue(me instanceof MsoIOException);
        assertTrue("connect".equals(me.getMessage()));

        MsoException me2 = super.neutronExceptionToMsoException(openStackResponseException, "ContextError");
        assertTrue(me2 instanceof MsoOpenstackException);
        assertTrue("ContextError".equals(me2.getContext()));
        assertTrue(MsoExceptionCategory.OPENSTACK.equals(me2.getCategory()));
    }

    @Test
    public final void testRuntimeExceptionToMsoException() {
        RuntimeException re = new RuntimeException("runtime");
        MsoException me = super.runtimeExceptionToMsoException(re, "ContextError");

        assertTrue(me instanceof MsoAdapterException);
        assertTrue("ContextError".equals(me.getContext()));
        assertTrue(MsoExceptionCategory.INTERNAL.equals(me.getCategory()));
    }
}
