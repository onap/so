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

package com.gigaspaces.aria.rest.client;

import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class ExecutionDetailsTest {
	
private Input inputs;
private ExecutionDetails ed;

@Test
	public void test() {	
        List<Input> actual = Arrays.asList(inputs);
		ed= new ExecutionDetails("",30,30,false,actual);
		ed=new ExecutionDetails("");
		ed.setExecutor("");
		ed.setInputs(actual);
		ed.setTaskMaxAttempts(30);
		ed.setTaskRetryInterval(30);
		ed.setRetry_failed_tasks(false);
		assert(ed.getExecutor()).equals("");
		assertFalse(ed.isRetry_failed_tasks());
		assert(ed.getInputs().equals(actual));
		assertEquals(30,ed.getTaskMaxAttempts());
    	assertEquals(30,ed.getTaskRetryInterval());
	}
}
