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

package org.onap.so.client.aai;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class) 
public class AAIUpdatorTest {
	
	@Mock
	protected AAIRestClientI client;
	String vnfName = "testVnf";
	String uuid = "UUID";
	AAIUpdatorImpl updator;
	
	@Before
	public void init(){
		updator = new AAIUpdatorImpl();
		updator.setClient(client);
	}

	@Test
	public void testUpdateVnfToLocked() throws Exception{		
		doNothing().when(client).updateMaintenceFlagVnfId(isA(String.class), isA(Boolean.class), isA(String.class));	
		updator.updateVnfToLocked(vnfName, uuid);
		verify(client, times(1)).updateMaintenceFlagVnfId(vnfName, true, uuid);
	}
	
	@Test
	public void testUpdateVnfToUnLocked() throws Exception {
		doNothing().when(client).updateMaintenceFlagVnfId(isA(String.class), isA(Boolean.class), isA(String.class));	
		updator.updateVnfToUnLocked(vnfName, uuid);
		verify(client, times(1)).updateMaintenceFlagVnfId(vnfName, false, uuid);
	}
}