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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.BeforeClass;
import org.junit.Test;

import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.properties.MsoPropertiesException;
import org.openecomp.mso.properties.MsoPropertiesFactory;

public class RequestTunablesTest {

    public static MsoPropertiesFactory msoPropertiesFactory = new MsoPropertiesFactory();

    /**
     * This method is called before any test occurs.
     * It creates a fake tree from scratch
     *
     * @throws MsoPropertiesException
     */
    @BeforeClass
    public static final void prepare() throws MsoPropertiesException {
        ClassLoader classLoader = RequestTunablesTest.class.getClassLoader();
        String path = classLoader.getResource("mso.properties").toString().substring(5);

        msoPropertiesFactory.initializeMsoProperties(RequestTunables.MSO_PROP_SDNC_ADAPTER, path);

    }

    /**
     * Test method for
     * {@link org.openecomp.mso.adapters.sdnc.impl.RequestTunables#RequestTunables(java.lang.String, java.lang.String, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public final void testRequestTunables() {
        RequestTunables rt = new RequestTunables(null, null, "op", null, msoPropertiesFactory);
        assert (rt.getReqId().length() == 0);
        rt = new RequestTunables("reqId", "msoAction", null, "query", msoPropertiesFactory);
        rt.setTunables();
        System.out.println(rt.toString());
        //  assert (rt.getReqMethod ().equals ("toto"));
        assert (rt.getTimeout() != null);
        assert (rt.getAction().equals("query"));
        assert (rt.getMsoAction().equals("msoAction"));
        assert (rt.getHeaderName().equals("sdnc-request-header"));
        assert (rt.getOperation().length() == 0);
        assert (rt.getAsyncInd().equals("N"));
        assert (rt.getReqId().equals("reqId"));
    }

}
