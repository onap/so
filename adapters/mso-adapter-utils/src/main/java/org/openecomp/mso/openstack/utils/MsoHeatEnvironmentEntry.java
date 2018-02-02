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

import java.util.Set;
import org.openecomp.mso.logger.MsoLogger;

public class MsoHeatEnvironmentEntry {

	private static final MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA);
	private Set<MsoHeatEnvironmentParameter> parameters;
	private String rawEntry;
	private boolean valid = true;
	private String errorString;
	private String resourceRegistryEntryRaw;

	private MsoHeatEnvironmentEntry(String rawEntry) {
		this.rawEntry = rawEntry;
	}

	private MsoHeatEnvironmentEntry(Set<MsoHeatEnvironmentParameter> parameters, String rawEntry, boolean valid,
			String errorString, String resourceRegistryEntryRaw) {
		this.parameters = parameters;
		this.rawEntry = rawEntry;
		this.valid = valid;
		this.errorString = errorString;
		this.resourceRegistryEntryRaw = resourceRegistryEntryRaw;
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
		if (this.parameters == null || this.parameters.size() < 1) {
			return false;
		}
		if (this.parameters.contains(new MsoHeatEnvironmentParameter(paramName))) {
			return true;
		}
		return false;
	}

	public String toFullString() {
		StringBuilder sb = new StringBuilder();
		if (hasParameters()) {
			sb.append("parameters:\n");
			for (MsoHeatEnvironmentParameter hep : parameters) {
				sb.append("   ").append(hep.getName()).append(":  ").append(hep.getValue()).append("\n");
			}
			sb.append("\n");
		}
		sb.append("\n");
		sb.append(this.resourceRegistryEntryRaw);
		return sb.toString();
	}

	public String getRawEntry() {
		return rawEntry;
	}
	
	private static String getResourceRegistryRawEntry(String rawEntry) {
		int indexOf = rawEntry.indexOf("resource_registry:");
		if (indexOf < 0) {
			return "";
		}
		return rawEntry.substring(indexOf);
	}

	public static MsoHeatEnvironmentEntry create(String rawEntry) {
		if (rawEntry == null || rawEntry.isEmpty()) {
			return new MsoHeatEnvironmentEntry(rawEntry);
		}
		try {
			Set<MsoHeatEnvironmentParameter> parameters = new MsoYamlEditorWithEnvt(rawEntry.getBytes())
					.getParameterListFromEnvt();
			return new MsoHeatEnvironmentEntry(parameters, rawEntry, true, null,
					getResourceRegistryRawEntry(rawEntry));
		} catch (Exception e) {
			LOGGER.debug(String.format("An exception occurred during processing the following raw entry: %s", rawEntry),
					e);
			return new MsoHeatEnvironmentEntry(null, rawEntry, false, e.getMessage(), null);
		}
	}

}
