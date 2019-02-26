/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.openstack.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MsoHeatEnvironmentResource {

    private static final Logger logger = LoggerFactory.getLogger(MsoHeatEnvironmentResource.class);
    
	private String name;
	private String value;
	
	public MsoHeatEnvironmentResource(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}
	public MsoHeatEnvironmentResource(String name) {
		// Allow to initialize with a null value
		this(name, null);
	}
	public MsoHeatEnvironmentResource() {
		this(null, null);
	}
	
	public String getName() {
		return this.name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getValue() {
		return this.value;
	}
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "\"" +
			this.name +
			"\": " +
			this.value;
	}

	@Override
	public boolean equals(Object o) {	
		if (!(o instanceof MsoHeatEnvironmentResource)) {
			return false;
		}
		if (this == o) {
			return true;
		}
		MsoHeatEnvironmentResource her = (MsoHeatEnvironmentResource) o;	
		// If the name of the parameter is the same, then they're equal
		if (her.getName().equals(this.getName())) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int result = 0;
		try {
			result = this.name.hashCode();
		} catch (Exception e) {
        logger.debug("Exception:", e);
    }
		return result;
	}


}
