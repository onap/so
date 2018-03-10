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

package org.openecomp.mso.bpmn.common.scripts

import org.apache.commons.lang3.*

public class VfModule implements Serializable {
	
	/**
	 * Class representing a VF Module Node. Fields of this class include indicators
	 * as to whether the VF Module is the only VF Module in its containing Generic VNF
	 * and whether the VF Module is the base VF Module in its containing Generic VNF.
	 */

	private Node node
	private Boolean onlyVfModule
	private Boolean baseVfModule

	/**
	 * Constructor.
	 *
	 * @param node Node representing the VF Module xml.
	 * @param onlyVfModule Is this VF Module the only VF Module in its containing Generic VNF?
	 */
	public VfModule(Node node, boolean onlyVfModule) {
		this.node = node
		this.onlyVfModule = onlyVfModule
		this.baseVfModule = getElementText('is-base-vf-module').equals('true')
	}

	/**
	 * Get the Node representing the VF Module xml.
	 *
	 * @return the Node representing the VF Module xml.
	 */
	public Node getNode() {
		return node
	}

	public String getElementText(String childNodeName) {
		def Node childNode = (new MsoUtils()).getChildNode(node, childNodeName)
		if (childNode == null) {
			return ''
		} else {
			return childNode.text()
		}
	}

	/**
	 * Is this VF Module the only VF Module in its containing Generic VNF?
	 *
	 * @return true if this VF Module is the only VF Module in its containing Generic VNF;
	 * false otherwise.
	 */
	public boolean isOnlyVfModule() {
		return onlyVfModule
	}

	/**
	 * Is this VF Module the base VF Module in its containing Generic VNF?
	 *
	 * @return true if this VF Module is the base VF Module in its containing Generic VNF;
	 * false otherwise.
	 */
	public boolean isBaseVfModule() {
		return baseVfModule
	}
}

