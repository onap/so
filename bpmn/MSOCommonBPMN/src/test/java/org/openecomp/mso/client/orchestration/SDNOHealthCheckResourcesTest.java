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

package org.openecomp.mso.client.orchestration;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openecomp.mso.BaseTest;
import org.openecomp.mso.BuildingBlockTestDataSetup;
import org.openecomp.mso.bpmn.common.InjectionHelper;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.RequestContext;
import org.openecomp.mso.client.sdno.SDNOValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
@RunWith(MockitoJUnitRunner.class)
public class SDNOHealthCheckResourcesTest  extends BuildingBlockTestDataSetup{
	@InjectMocks
	private SDNOHealthCheckResources sdnoHealthCheckResources;
	
	private GenericVnf genericVnf;
	private RequestContext requestContext;
	
	@Mock
	protected SDNOValidator MOCK_sdnoValidator;
	
	@Mock
	protected InjectionHelper MOCK_injectionHelper;
	
	@Before
	public void before() {
		genericVnf = buildGenericVnf();
		
		requestContext = buildRequestContext();
		doReturn(MOCK_sdnoValidator).when(MOCK_injectionHelper).getSdnoValidator();
	}
	
	@Test
	public void healthCheckTest() throws Exception {
		doReturn(true).when(MOCK_sdnoValidator).healthDiagnostic(isA(String.class), isA(UUID.class), isA(String.class));
		assertTrue(sdnoHealthCheckResources.healthCheck(genericVnf, requestContext));
	}	
}
