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

package org.onap.so.bpmn.servicedecomposition.homingobjects;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.onap.so.bpmn.servicedecomposition.ShallowCopy;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;

@JsonRootName("solution")
public class SolutionInfo implements Serializable {

	private static final long serialVersionUID = 6903399488466840832L;

	@JsonProperty("solution-id") //identifies which solution
	private int solutionId;
	@JsonProperty("homed")
	private boolean homed;
	@JsonProperty("is-rehome")
	private boolean isRehome;
	@JsonProperty("targeted-cloud")
	private CloudRegion targetedCloudRegion;


	public boolean isHomed() {
		return homed;
	}

	public void setHomed(boolean homed) {
		this.homed = homed;
	}

	public boolean isRehome() {
		return isRehome;
	}

	public void setRehome(boolean isRehome) {
		this.isRehome = isRehome;
	}

	/**
	 * Identifies which solution when multiple solutions exist for a
	 * given resource or resource pair.
	 * i.e. 1, 2, 3....
	 */
	public int getSolutionId() {
		return solutionId;
	}

	/**
	 * Identifies which solution when multiple solutions exist for a
	 * given resource or resource pair.
	 * i.e. 1, 2, 3....
	 */
	public void setSolutionId(int solutionId) {
		this.solutionId = solutionId;
	}

	/**
	 * Identifies where the resource should be created for
	 * homed equals false scenarios. Will return null if resource
	 * was homed.
	 */
	public CloudRegion getTargetedCloudRegion() {
		return targetedCloudRegion;
	}

	/**
	 * Identifies where the resource should be created for
	 * homed equals false scenarios. Will return null if resource
	 * was homed.
	 */
	public void setTargetedCloudRegion(CloudRegion targetedCloudRegion) {
		this.targetedCloudRegion = targetedCloudRegion;
	}



}
