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

package org.onap.so.openstack.mappers;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.onap.so.openstack.beans.HeatStatus;
import org.onap.so.openstack.beans.StackInfo;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.woorea.openstack.heat.model.Stack;

public class StackInfoMapperTest {

    private static final String PATH = "src/test/resources/";

    @Test
    public void nullStack() {
        StackInfoMapper mapper = new StackInfoMapper(null);
        assertEquals(HeatStatus.NOTFOUND, mapper.map().getStatus());
    }

    @Test
    public void checkHeatStatusMap() {
        StackInfoMapper mapper = new StackInfoMapper(null);
        assertEquals(HeatStatus.BUILDING, mapper.mapStatus("CREATE_IN_PROGRESS"));
        assertEquals(HeatStatus.CREATED, mapper.mapStatus("CREATE_COMPLETE"));
        assertEquals(HeatStatus.FAILED, mapper.mapStatus("CREATE_FAILED"));
        assertEquals(HeatStatus.DELETING, mapper.mapStatus("DELETE_IN_PROGRESS"));
        assertEquals(HeatStatus.NOTFOUND, mapper.mapStatus("DELETE_COMPLETE"));
        assertEquals(HeatStatus.FAILED, mapper.mapStatus("DELETE_FAILED"));
        assertEquals(HeatStatus.UPDATING, mapper.mapStatus("UPDATE_IN_PROGRESS"));
        assertEquals(HeatStatus.FAILED, mapper.mapStatus("UPDATE_FAILED"));
        assertEquals(HeatStatus.UPDATED, mapper.mapStatus("UPDATE_COMPLETE"));
        assertEquals(HeatStatus.INIT, mapper.mapStatus(null));
        assertEquals(HeatStatus.UNKNOWN, mapper.mapStatus("status-not-there"));
    }

    @Test
    public void checkOutputToMap() throws JsonMappingException, IOException {
        ObjectMapper jacksonMapper = new ObjectMapper();
        Stack sample = jacksonMapper.readValue(this.getJson("stack-example.json"), Stack.class);
        StackInfoMapper mapper = new StackInfoMapper(sample);
        StackInfo result = mapper.map();
        Map<String, Object> map = result.getOutputs();
        assertEquals(true, map.containsKey("key2"));
        assertEquals("value1", map.get("key1"));
    }

    @Test
    public void mapRemainingFields() {
        Stack stack = new Stack();
        stack.setStackName("name");
        stack.setId("id");
        stack.setStackStatusReason("message");
        stack.setParameters(new HashMap<String, Object>());
        StackInfoMapper mapper = new StackInfoMapper(stack);
        StackInfo info = mapper.map();
        assertEquals("name", info.getName());
        assertEquals("name/id", info.getCanonicalName());
        assertEquals("message", info.getStatusMessage());
        assertEquals(stack.getParameters(), info.getParameters());
    }

    private String getJson(String filename) throws IOException {
        return new String(Files.readAllBytes(Paths.get(PATH + filename)));
    }
}
