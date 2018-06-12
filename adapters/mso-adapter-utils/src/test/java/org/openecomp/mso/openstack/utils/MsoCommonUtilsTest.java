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

package org.openecomp.mso.openstack.utils;


import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openecomp.mso.cloud.Application;
import org.openecomp.mso.openstack.exceptions.MsoAdapterException;
import org.openecomp.mso.openstack.exceptions.MsoException;
import org.openecomp.mso.openstack.exceptions.MsoExceptionCategory;
import org.openecomp.mso.openstack.exceptions.MsoIOException;
import org.openecomp.mso.openstack.exceptions.MsoOpenstackException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.woorea.openstack.base.client.OpenStackBaseException;
import com.woorea.openstack.base.client.OpenStackConnectException;
import com.woorea.openstack.base.client.OpenStackRequest;
import com.woorea.openstack.base.client.OpenStackResponseException;


/**
 * This class implements test methods of the MsoCommonUtils
 *
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class MsoCommonUtilsTest {

	
	@Autowired()
	@Qualifier("CommonUtils")
	private MsoCommonUtils commonUtils;
	@Test
    public final void testExecuteAndRecordOpenstackRequest () {
		OpenStackRequest openstackRequest = Mockito.mock(OpenStackRequest.class);
		Mockito.when(openstackRequest.endpoint()).thenReturn("localhost");
		Mockito.when(openstackRequest.path()).thenReturn("/test");
		//TODO:Must try a real connection
		assertNull(commonUtils.executeAndRecordOpenstackRequest (openstackRequest));

	}

	@Test
    public final void testKeystoneErrorToMsoException () {
		OpenStackBaseException openStackConnectException = new OpenStackConnectException("connect");

		OpenStackBaseException openStackResponseException = new OpenStackResponseException("response",1);

		MsoException me = commonUtils.keystoneErrorToMsoException (openStackConnectException,"ContextError");

		assertTrue(me instanceof MsoIOException);
		assertTrue("connect".equals(me.getMessage()));


		MsoException me2 = commonUtils.keystoneErrorToMsoException (openStackResponseException,"ContextError");
		assertTrue(me2 instanceof MsoOpenstackException);
		assertTrue("ContextError".equals(me2.getContext()));
		assertTrue(MsoExceptionCategory.OPENSTACK.equals(me2.getCategory()));

	}

	@Test
	public final void testHeatExceptionToMsoException () {
		OpenStackBaseException openStackConnectException = new OpenStackConnectException("connect");

		OpenStackBaseException openStackResponseException = new OpenStackResponseException("response",1);

		MsoException me = commonUtils.heatExceptionToMsoException (openStackConnectException,"ContextError");

		assertTrue(me instanceof MsoIOException);
		assertTrue("connect".equals(me.getMessage()));


		MsoException me2 = commonUtils.heatExceptionToMsoException (openStackResponseException,"ContextError");
		assertTrue(me2 instanceof MsoOpenstackException);
		assertTrue("ContextError".equals(me2.getContext()));
		assertTrue(MsoExceptionCategory.OPENSTACK.equals(me2.getCategory()));
	}

	@Test
	public final void testNeutronExceptionToMsoException () {
		OpenStackBaseException openStackConnectException = new OpenStackConnectException("connect");

		OpenStackBaseException openStackResponseException = new OpenStackResponseException("response",1);

		MsoException me = commonUtils.neutronExceptionToMsoException (openStackConnectException,"ContextError");

		assertTrue(me instanceof MsoIOException);
		assertTrue("connect".equals(me.getMessage()));

		MsoException me2 = commonUtils.neutronExceptionToMsoException (openStackResponseException,"ContextError");
		assertTrue(me2 instanceof MsoOpenstackException);
		assertTrue("ContextError".equals(me2.getContext()));
		assertTrue(MsoExceptionCategory.OPENSTACK.equals(me2.getCategory()));
	}

	@Test
	public final void testRuntimeExceptionToMsoException () {
	    RuntimeException re = new RuntimeException ("runtime");
	    MsoException me = commonUtils.runtimeExceptionToMsoException (re, "ContextError");

	    assertTrue (me instanceof MsoAdapterException);
	    assertTrue("ContextError".equals(me.getContext()));
        assertTrue(MsoExceptionCategory.INTERNAL.equals(me.getCategory()));
	}
}
