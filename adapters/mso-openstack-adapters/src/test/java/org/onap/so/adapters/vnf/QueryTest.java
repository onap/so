/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.adapters.vnf;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Map;

import javax.xml.ws.Holder;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.adapters.vnf.exceptions.VnfException;
import org.onap.so.openstack.beans.HeatStatus;
import org.onap.so.openstack.beans.StackInfo;
import org.onap.so.openstack.beans.VnfStatus;
import org.onap.so.openstack.exceptions.MsoException;
import org.onap.so.openstack.exceptions.MsoOpenstackException;
import org.onap.so.openstack.utils.MsoHeatUtils;

@RunWith(MockitoJUnitRunner.class)
public class QueryTest {
	
	@Mock
	private MsoHeatUtils heat;
	@InjectMocks
	private MsoVnfAdapterImpl vnfAdapter = new MsoVnfAdapterImpl();
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	@Test
	public void testQueryCreatedVnf() throws VnfException, MsoException {
		StackInfo info = new StackInfo("stackName", HeatStatus.CREATED);
		when(heat.queryStack(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(info);
		String cloudId = "MT";
		String cloudOwner = "CloudOwner";
		String tenantId = "MSO_Test";
		String vnfName = "VNF_TEST1";
		Holder<Boolean> vnfExists = new Holder<>();
		Holder<String> vnfId = new Holder<>();
		Holder<VnfStatus> status = new Holder<>();
		Holder<Map<String, String>> outputs = new Holder<>();

		vnfAdapter.queryVnf(cloudId, cloudOwner, tenantId, vnfName, null, vnfExists, vnfId, status, outputs);

		assertTrue(vnfExists.value);
	}

	@Test
	public void testQueryNotFoundVnf() throws VnfException, MsoException {
		StackInfo info = new StackInfo("stackName", HeatStatus.NOTFOUND);
		when(heat.queryStack(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(info);
		String cloudId = "MT";
		String cloudOwner = "CloudOwner";
		String tenantId = "MSO_Test";
		String vnfName = "VNF_TEST1";
		Holder<Boolean> vnfExists = new Holder<>();
		Holder<String> vnfId = new Holder<>();
		Holder<VnfStatus> status = new Holder<>();
		Holder<Map<String, String>> outputs = new Holder<>();

		vnfAdapter.queryVnf(cloudId, cloudOwner, tenantId, vnfName, null, vnfExists, vnfId, status, outputs);

		assertFalse(vnfExists.value);
	}

	@Test()
	// @Ignore // 1802 merge
	public void testQueryVnfWithException() throws VnfException, MsoException {
		String cloudId = "MT";
		String cloudOwner = "CloudOwner";
		String tenantId = "MSO_Test";
		String vnfName = "VNF_TEST1";
		Holder<Boolean> vnfExists = new Holder<>();
		Holder<String> vnfId = new Holder<>();
		Holder<VnfStatus> status = new Holder<>();
		Holder<Map<String, String>> outputs = new Holder<>();
		thrown.expect(VnfException.class);
		thrown.expectCause(hasProperty("context", is("QueryVNF")));
		when(heat.queryStack(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenThrow(new MsoOpenstackException(1, "test messsage", "test detail"));
		vnfAdapter.queryVnf(cloudId, cloudOwner, tenantId, vnfName, null, vnfExists, vnfId, status, outputs);
	}
}
