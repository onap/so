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


import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.mso.cloud.Application;
import org.openecomp.mso.openstack.utils.MsoCommonUtils;
import org.openecomp.mso.openstack.utils.MsoHeatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import static org.junit.Assert.assertEquals;

/**
 * This class implements test methods of the MsoHeatUtils
 *
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class MsoHeatUtilsRefactorTest extends MsoCommonUtils {

	@Autowired
	private  MsoHeatUtils msoHeatUtils;
	
	@Test
	public final void testGetKeystoneUrl() {
		try {
			String keyUrl = msoHeatUtils.getCloudSiteKeystoneUrl("DAN");
			assertEquals("http://135.138.170.21:5000/v2.0",keyUrl);
		} catch (Exception e) {
			
		}
	}


}
