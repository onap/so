/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 IBM.
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

package org.onap.so.db.request.data.controller;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class InstanceNameDuplicateCheckRequestTest {
    
    private InstanceNameDuplicateCheckRequest instanceNameDuplicateCheckRequest;
    
    @Before
    public void setUp(){
        instanceNameDuplicateCheckRequest= new InstanceNameDuplicateCheckRequest(new HashMap<String, String>(), "testInstanceName", "testRequestScope");
    }
    
    @Test
    public void getSetInstanceIdMap()
    {
        Map<String, String> map=new HashMap<String, String>();
        instanceNameDuplicateCheckRequest.setInstanceIdMap((HashMap)map);
        assertEquals((HashMap)map, instanceNameDuplicateCheckRequest.getInstanceIdMap());
    }
    
    @Test
    public void getSetInstanceName()
    {
        instanceNameDuplicateCheckRequest.setInstanceName("testInstanceName");
        assertEquals("testInstanceName", instanceNameDuplicateCheckRequest.getInstanceName());
    }
    
    @Test
    public void getSetRequestScope()
    {
        Map<String, String> map=new HashMap<String, String>();
        instanceNameDuplicateCheckRequest.setRequestScope("testRequestScope");
        assertEquals("testRequestScope", instanceNameDuplicateCheckRequest.getRequestScope());
    }

}
