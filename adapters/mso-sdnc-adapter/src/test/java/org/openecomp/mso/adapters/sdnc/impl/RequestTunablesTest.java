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

package org.openecomp.mso.adapters.sdnc.impl;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.properties.MsoPropertiesException;
import org.openecomp.mso.properties.MsoPropertiesFactory;

public class RequestTunablesTest {

	private static MsoPropertiesFactory msoPropertiesFactory = new MsoPropertiesFactory();
	
	public static final String SDNC_PROP = MsoJavaProperties.class.getClassLoader().getResource("mso.sdnc.properties").toString().substring(5);
	
	@Before
	public final void initBeforeEachTest() throws MsoPropertiesException {
			msoPropertiesFactory.removeAllMsoProperties();
			msoPropertiesFactory.initializeMsoProperties("MSO_PROP_SDNC_ADAPTER", SDNC_PROP);
	}

	@AfterClass
	public static final void kill () throws MsoPropertiesException {

		    msoPropertiesFactory.removeMsoProperties("MSO_PROP_SDNC_ADAPTER");
	}
	
    /**
     * Test method for
     * {@link org.openecomp.mso.adapters.sdnc.impl.RequestTunables#RequestTunables(java.lang.String, java.lang.String, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public final void testRequestTunables () {
        RequestTunables rt = new RequestTunables (null, null, "op", null,msoPropertiesFactory);
        assertEquals(0, rt.getReqId ().length ());
        rt = new RequestTunables ("reqId", "msoAction", null, "query",msoPropertiesFactory);
        rt.setTunables ();
        System.out.println(rt.toString ());
      //  assert (rt.getReqMethod ().equals ("toto"));
        assertNotNull(rt.getTimeout ());
        assertEquals("query", rt.getAction ());
        assertEquals("msoAction", rt.getMsoAction ());
        assertEquals("sdnc-request-header", rt.getHeaderName ());
        assertEquals(0, rt.getOperation ().length ());
        assertEquals("N", rt.getAsyncInd ());
        assertEquals("reqId", rt.getReqId ());
    }
    
    @Test
    public final void testRequestTunablesSet() {
    	RequestTunables rt = new RequestTunables("reqId", "gammainternet", "service-configuration-operation", "changeactivate", msoPropertiesFactory);
    	 rt.setTunables ();
    	 assertNotNull(rt.getTimeout ());
         assertEquals("changeactivate", rt.getAction ());
         assertEquals("gammainternet", rt.getMsoAction ());
         assertEquals("sdnc-request-header", rt.getHeaderName ());
         assertEquals("service-configuration-operation", rt.getOperation ());
         assertEquals("N", rt.getAsyncInd ());
         assertEquals("reqId", rt.getReqId ());
    }

}
