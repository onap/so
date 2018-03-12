/*
* ============LICENSE_START=======================================================
* ONAP : SO
* ================================================================================
* Copyright 2018 TechMahindra
*=================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* ============LICENSE_END=========================================================
*/

package org.openecomp.mso.adapters.sdncrest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.mso.adapters.sdncrest.SDNCRequestCommon;
import org.openecomp.mso.adapters.sdncrest.ServiceInformation;
import org.openecomp.mso.adapters.sdncrest.RequestInformation;

public class SDNCServiceRequestTest {

	@Mock
	SDNCRequestCommon src;
	
	@Mock
	ServiceInformation si;
	
	@Mock
	RequestInformation ri;
	
	@InjectMocks
	SDNCServiceRequest ssr;
	
	@Before
	public void init(){
	    MockitoAnnotations.initMocks(this);
	 }
	
	@Test
	public void test() {
		ssr= new SDNCServiceRequest("url", "timeout",
				"sdncRequestId", "sdncService", "sdncOperation",
				ri,
				si, "sdncServiceDataType",
				"sndcServiceData");
		
	ssr.setSDNCService("sdncService");
	ssr.setSDNCServiceData("sndcServiceData");
	ssr.setSDNCServiceDataType("sdncServiceDataType");
	ssr.setBPTimeout("timeout");
	ssr.setBPNotificationUrl("url");
	ssr.setRequestInformation(ri);
	ssr.setServiceInformation(si);
	ssr.setSDNCOperation("sdncOperation");
	ssr.setSDNCRequestId("sdncRequestId");
	assert(ssr.getSDNCService().equals("sdncService"));
	assert(ssr.getSDNCServiceData().equals("sndcServiceData"));
	assert(ssr.getSDNCServiceDataType().equals("sdncServiceDataType"));
	assert(ssr.getBPTimeout().equals("timeout"));
	assert(ssr.getBPNotificationUrl().equals("url"));
	assert(ssr.getRequestInformation().equals(ri));
	assert(ssr.getServiceInformation().equals(si));
	assert(ssr.getSDNCOperation().equals("sdncOperation"));
	assert(ssr.getSDNCRequestId().equals("sdncRequestId"));	
	}

}
