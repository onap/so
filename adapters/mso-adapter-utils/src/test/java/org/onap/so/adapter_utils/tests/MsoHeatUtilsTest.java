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

package org.onap.so.adapter_utils.tests;

import java.util.HashMap;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.TestApplication;
import org.onap.so.openstack.exceptions.MsoCloudSiteNotFound;
import org.onap.so.openstack.exceptions.MsoException;
import org.onap.so.openstack.exceptions.MsoIOException;
import org.onap.so.openstack.exceptions.MsoStackAlreadyExists;
import org.onap.so.openstack.exceptions.MsoTenantNotFound;
import org.onap.so.openstack.utils.MsoCommonUtils;
import org.onap.so.openstack.utils.MsoHeatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.woorea.openstack.heat.model.CreateStackParam;

/**
 * This class implements test methods of the MsoHeatUtils
 *
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
@Ignore
public class MsoHeatUtilsTest extends MsoCommonUtils {
	@Autowired
	private  MsoHeatUtils msoHeatUtils;

	@Test
	public final void testCreateStackBadCloudConfig()
			throws MsoStackAlreadyExists, MsoTenantNotFound, MsoException, MsoCloudSiteNotFound {
		try {
			msoHeatUtils.createStack("DOESNOTEXIST", "test", "stackName", "test", new HashMap<String, Object>(),
					Boolean.TRUE, 10);
		} catch (MsoCloudSiteNotFound e) {

		} catch (java.lang.NullPointerException npe) {

		}

	}

	@Test
	public final void testCreateStackFailedConnectionHeatClient()
			throws MsoStackAlreadyExists, MsoTenantNotFound, MsoException, MsoCloudSiteNotFound {
		try {
			msoHeatUtils.createStack("MT", "test", "stackName", "test", new HashMap<String, Object>(), Boolean.TRUE,
					10);
		} catch (MsoIOException e) {

		}

	}

	@Test
	public final void testCreateStackFailedConnection()
			throws MsoStackAlreadyExists, MsoTenantNotFound, MsoException, MsoCloudSiteNotFound {
		try {
			msoHeatUtils.createStack("MT", "test", "stackName", "test", new HashMap<String, Object>(), Boolean.TRUE,
					10);
		} catch (MsoIOException e) {

		}

	}

	@Test
	public final void createStackSuccessWithEnvironment() throws MsoException {
		try {
			msoHeatUtils.createStack("MT", "test", "stackName", "test", new HashMap<String, Object>(), Boolean.TRUE, 10,
					"environment");
		} catch (MsoIOException e) {

		}

	}

	@Test
	public final void createStackSuccessWithFiles() throws MsoException {
		try {
			msoHeatUtils.createStack("MT", "test", "stackName", "test", new HashMap<String, Object>(), Boolean.TRUE, 10,
					"environment", new HashMap<String, Object>());
		} catch (MsoIOException e) {

		}

	}

	@Test
	public final void createStackSuccessWithHeatFiles() throws MsoException {
		try {
			msoHeatUtils.createStack("MT", "test", "stackName", "test", new HashMap<String, Object>(), Boolean.TRUE, 10,
					"environment", new HashMap<String, Object>(), new HashMap<String, Object>());
		} catch (MsoIOException e) {

		}
	}

	@Test
	public final void requestToStringBuilderTest() {
		CreateStackParam param = new CreateStackParam();
		param.setDisableRollback(false);
		param.setEnvironment("environment");
		param.setFiles(new HashMap<String, Object>());
		param.setParameters(new HashMap<>());
		param.setStackName("stackName");
		param.setTemplate("template");
		param.setTemplateUrl("http://templateUrl");
		param.setTimeoutMinutes(1);

		msoHeatUtils.requestToStringBuilder(param);
	}
}
