/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.openecomp.mso.apihandlerinfra;

import static org.junit.Assert.*;

import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.mso.HealthCheckUtils;
import org.openecomp.mso.MsoStatusUtil;
import org.openecomp.mso.properties.MsoPropertiesFactory;

public class GlobalHealthcheckHandlerTest {

	public static MsoPropertiesFactory msoPropertiesFactory = new MsoPropertiesFactory();
	private static final String CHECK_HTML = "<!DOCTYPE html><html><head><meta charset=\"ISO-8859-1\"><title>Health Check</title></head><body>Application ready</body></html>";
	public static final Response HEALTH_CHECK_RESPONSE = Response.status(HttpStatus.SC_OK).entity(CHECK_HTML).build();

	@Test
	public final void testGlobalHealthcheck() {
		try {
			MsoStatusUtil statusUtil = Mockito.mock(MsoStatusUtil.class);
			HealthCheckUtils utils = Mockito.mock(HealthCheckUtils.class);
			Mockito.when(utils.verifyGlobalHealthCheck(true, null)).thenReturn(true);
			Mockito.when(statusUtil.getSiteStatus(Mockito.anyString())).thenReturn(true);
			GlobalHealthcheckHandler gh = Mockito.mock(GlobalHealthcheckHandler.class);
			Mockito.when(gh.globalHealthcheck(Mockito.anyBoolean())).thenReturn(HEALTH_CHECK_RESPONSE);
			Response resp = gh.globalHealthcheck(true);
			assertEquals(resp.getStatus(), HttpStatus.SC_OK);
		} catch (Exception e) {

			e.printStackTrace();
		}
	}
}
