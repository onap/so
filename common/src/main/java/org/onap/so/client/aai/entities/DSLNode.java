/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.aai.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.onap.so.client.graphinventory.GraphInventoryObjectName;

public class DSLNode implements QueryStep {

	private final String nodeName;
	private final List<DSLNodeKey> nodeKeys;
	private final StringBuilder query = new StringBuilder();
	private boolean output = false;
	
	public DSLNode() {
		this.nodeName = "";
		this.nodeKeys = new ArrayList<>();
		
	}
	public DSLNode(GraphInventoryObjectName name) {
		this.nodeName = name.typeName();
		this.nodeKeys = new ArrayList<>();
		query.append(nodeName);
	}
	public DSLNode(GraphInventoryObjectName name, DSLNodeKey... key) {
		this.nodeName = name.typeName();
		this.nodeKeys = Arrays.asList(key);
		query.append(nodeName);
	}
	
	public DSLNode output() {
		this.output = true;
		
		return this;
	}

	public DSLNode and(DSLNodeKey... key) {
		this.nodeKeys.addAll(Arrays.asList(key));
		
		return this;
	}
	
	@Override
	public String build() {
		if (output) {
			query.append("*");
		}
		for (DSLNodeKey key : nodeKeys) {
			query.append(key.build());
		}
		
		return query.toString();
	}
}
