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

package org.openecomp.mso.openstack.utils;

import org.junit.Test;


public class MsoHeatEnvironmentResourceTest {

	
	@Test
	public void test() {
		Object op=true;
	//	MsoHeatEnvironmentResource mer=mock(MsoHeatEnvironmentResource.class);
	//	MsoHeatEnvironmentResource mrea=new MsoHeatEnvironmentResource();
		MsoHeatEnvironmentResource mre=new MsoHeatEnvironmentResource("name");
		MsoHeatEnvironmentResource mae=new MsoHeatEnvironmentResource("name", "value");
		mre.setName("name");
		mae.setValue("value");
		assert(mre.getName().equals("name"));
		assert(mae.getValue().equals("value"));
		assert(mre.toString()!=null);
		//assertFalse(mer.equals(op));
		mae.equals(op);
		mae.hashCode();
		//when(mer.hashCode()).thenReturn(result);
	}

}
