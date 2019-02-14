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

package org.onap.so.client.graphinventory.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.onap.so.client.aai.entities.QueryStep;

import com.google.common.base.Joiner;


public class DSLNodeKey implements QueryStep {

	private boolean not = false;
	private final StringBuilder query = new StringBuilder();
	private final String keyName;
	private final List<String> values;
	public DSLNodeKey(String keyName, String... value) {

		this.keyName = keyName;
		this.values = Arrays.asList(value);
	}
	
	public DSLNodeKey not() {
		
		this.not = true;
		return this;
	}
	
	@Override
	public String build() {
		
		if (not) {
			query.append(" !");
		}
		query.append("('").append(keyName).append("', ");
		List<String> temp = new ArrayList<>();
		for (String item : values) {
			if (item.equals("null")) {
				temp.add(String.format("' %s '", item));
			} else if (item.equals("")){
				temp.add("' '");
			} else {
				temp.add(String.format("'%s'", item));
			}
		}
		query.append(Joiner.on(", ").join(temp)).append(")");
		
		return query.toString();
	}
}
