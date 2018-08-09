/*
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

package org.onap.so.bpmn.vcpe.scripts;

import java.util.HashMap;
import java.util.Map;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

class MapSetter implements Answer<Void> {
	final Map<String,Object> map;
	
	public MapSetter() {
		map = new HashMap<>();
	}
	
	public MapSetter(Map<String,Object> map) {
		this.map = map;
	}

	public Map<String, Object> getMap() {
		return map;
	}

	@Override
	public Void answer(InvocationOnMock invocation) throws Throwable {
		map.put(invocation.getArgumentAt(0, String.class), invocation.getArgumentAt(1, Object.class));
		return null;
	}

}
