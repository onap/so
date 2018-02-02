/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.openecomp.mso.openstack.utils;

import java.util.Optional;
import java.util.Set;
import org.openecomp.mso.logger.MsoLogger;

public class MsoHeatEnvironmentEntry {

    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);
    private Set<MsoHeatEnvironmentParameter> parameters;
	private StringBuilder rawEntry;
	private boolean valid = true;
	private String errorString;
	private StringBuilder resourceRegistryEntryRaw;

	public MsoHeatEnvironmentEntry(StringBuilder sb) {
		rawEntry = sb;
		processRawEntry();
	}
	
	private void processRawEntry() {
		try {
			if (rawEntry == null || rawEntry.toString().isEmpty()) {
				return;
			}
			byte[] b = this.rawEntry.toString().getBytes();
			parameters = new MsoYamlEditorWithEnvt(b).getParameterListFromEnvt();
			resourceRegistryEntryRaw = Optional.ofNullable(getResourceRegistryRawEntry())
					.orElse(new StringBuilder(""));
		} catch (Exception e) {
		    LOGGER.debug("Exception:", e);
			this.valid = false;
			this.errorString = e.getMessage();
		}
	}
	
	public boolean isValid() {
		return this.valid;
	}
	public String getErrorString() {
		return this.errorString;
	}

	private boolean hasParameters() {
		if (this.parameters != null && this.parameters.size() > 0) {
			return true;
		}
		return false;
	}
	
	public boolean containsParameter(String paramName) {
		boolean contains = false;
		if (this.parameters == null || this.parameters.size() < 1) {
			return false;
		}
		if (this.parameters.contains(new MsoHeatEnvironmentParameter(paramName))) {
			contains = true;
		}
		return contains;
	}

	public StringBuilder toFullString() {
		StringBuilder sb = new StringBuilder();
		
		if (this.hasParameters()) {
			sb.append("parameters:\n");
			for (MsoHeatEnvironmentParameter hep : this.parameters) {
				sb.append("   " + hep.getName() + ":  " + hep.getValue() + "\n");
			}
			sb.append("\n");
		}
		sb.append("\n");
		sb.append(this.resourceRegistryEntryRaw);
		return sb;
	}

	public StringBuilder getRawEntry() {
		return this.rawEntry;
	}
	
	private StringBuilder getResourceRegistryRawEntry() {
		StringBuilder sb = new StringBuilder();
		int indexOf = this.rawEntry.indexOf("resource_registry:");
		if (indexOf < 0) { // no resource_registry:
			return null;
		}
		sb.append(this.rawEntry.substring(indexOf));
		return sb;
	}

}
