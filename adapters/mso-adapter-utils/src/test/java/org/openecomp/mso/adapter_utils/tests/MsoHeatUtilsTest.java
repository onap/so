/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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

import java.util.HashMap;

import org.junit.BeforeClass;
import org.junit.Test;

import org.openecomp.mso.cloud.CloudConfigFactory;
import org.openecomp.mso.openstack.exceptions.MsoAdapterException;
import org.openecomp.mso.openstack.exceptions.MsoCloudSiteNotFound;
import org.openecomp.mso.openstack.exceptions.MsoException;
import org.openecomp.mso.openstack.exceptions.MsoIOException;
import org.openecomp.mso.openstack.exceptions.MsoStackAlreadyExists;
import org.openecomp.mso.openstack.exceptions.MsoTenantNotFound;
import org.openecomp.mso.openstack.utils.MsoCommonUtils;
import org.openecomp.mso.openstack.utils.MsoHeatUtils;
import org.openecomp.mso.properties.MsoPropertiesFactory;



/**
 * This class implements test methods of the MsoHeatUtils
 *
 *
 */
public class MsoHeatUtilsTest extends MsoCommonUtils {
	public static MsoPropertiesFactory msoPropertiesFactory = new MsoPropertiesFactory();
	public static CloudConfigFactory cloudConfigFactory = new CloudConfigFactory();
	public static MsoHeatUtils msoHeatUtils;

	@BeforeClass
	public static final void loadClasses() {
		ClassLoader classLoader = CloudConfigTest.class.getClassLoader();
		String config = classLoader.getResource("cloud_config.json").toString().substring(5);
		cloudConfigFactory.initializeCloudConfig(config, 1);
		msoHeatUtils = new MsoHeatUtils("NO_PROP",msoPropertiesFactory,cloudConfigFactory);
	}

	@Test
	public final void testCreateStackBadCloudConfig () throws MsoStackAlreadyExists, MsoTenantNotFound, MsoException, MsoCloudSiteNotFound {
		try {
			msoHeatUtils.createStack ("DOESNOTEXIST", "test", "stackName", "test",  new HashMap<String,Object> (), Boolean.TRUE, 10);
		} catch (MsoCloudSiteNotFound e) {

		}

	}

	@Test
    public final void testCreateStackFailedConnectionHeatClient () throws MsoStackAlreadyExists, MsoTenantNotFound, MsoException, MsoCloudSiteNotFound {
		try {
			msoHeatUtils.createStack ("MT", "test", "stackName", "test",  new HashMap<String,Object> (), Boolean.TRUE, 10);
		} catch (MsoIOException e) {

		}

	}

	@Test
    public final void testCreateStackFailedConnection () throws MsoStackAlreadyExists, MsoTenantNotFound, MsoException, MsoCloudSiteNotFound {
		try {
			msoHeatUtils.createStack ("MT", "test", "stackName", "test",  new HashMap<String,Object> (), Boolean.TRUE, 10);
		} catch (MsoIOException e) {

		}

	}



}
