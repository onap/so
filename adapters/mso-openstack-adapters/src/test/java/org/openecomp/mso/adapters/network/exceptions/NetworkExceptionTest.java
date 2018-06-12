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

package org.openecomp.mso.adapters.network.exceptions;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openecomp.mso.openstack.exceptions.MsoException;
import org.openecomp.mso.openstack.exceptions.MsoExceptionCategory;

public class NetworkExceptionTest{
	
	NetworkException ne=new NetworkException("msg");
	NetworkExceptionBean neb =new NetworkExceptionBean();
	MsoException msoException= new MsoException("msoException") {};
    MsoExceptionCategory msc=MsoExceptionCategory.INTERNAL;
	NetworkException ne1=new NetworkException(msoException);
	NetworkException ne2=new NetworkException(ne);
	NetworkException ne3=new NetworkException("msg", ne);
	NetworkException ne4=new NetworkException("msg", msoException);
	NetworkException ne5=new NetworkException("msg", msc);
	NetworkException ne6=new NetworkException("msg", msc, ne);
	
	@Test
	public void test() {
		ne.setFaultInfo(neb);
		assertEquals(ne.getFaultInfo(), neb);
	}

}
