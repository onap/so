/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

import org.onap.so.client.graphinventory.entities.GraphInventoryEdgeLabel;

public enum AAIEdgeLabel implements GraphInventoryEdgeLabel {

	BELONGS_TO("org.onap.relationships.inventory.BelongsTo"),
	USES("org.onap.relationships.inventory.Uses");
	
	
	private final String label;
	private AAIEdgeLabel(String label) {
		this.label = label;
	}
	
	
	@Override
	public String toString() {
		return this.label;
	}
}
