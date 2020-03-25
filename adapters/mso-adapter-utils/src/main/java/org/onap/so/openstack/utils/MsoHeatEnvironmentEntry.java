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


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.onap.so.db.catalog.beans.HeatTemplateParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MsoHeatEnvironmentEntry {

    private static final Logger logger = LoggerFactory.getLogger(MsoHeatEnvironmentEntry.class);

    private Set<MsoHeatEnvironmentParameter> parameters = null;
    private Set<MsoHeatEnvironmentResource> resources = null;
    private StringBuilder rawEntry = null;
    private boolean valid = true;
    private String errorString = null;
    private StringBuilder resourceRegistryEntryRaw = null;

    public MsoHeatEnvironmentEntry() {
        super();
    }

    public MsoHeatEnvironmentEntry(StringBuilder sb) {
        this();
        this.rawEntry = sb;
        this.processRawEntry();
    }

    private void processRawEntry() {
        try {
            if (this.rawEntry == null || "".equals(this.rawEntry.toString()))
                return;
            byte[] b = this.rawEntry.toString().getBytes();
            MsoYamlEditorWithEnvt yaml = new MsoYamlEditorWithEnvt(b);
            this.parameters = yaml.getParameterListFromEnvt();
            StringBuilder sb = this.getResourceRegistryRawEntry();
            if (yaml != null) {
                this.parameters = yaml.getParameterListFromEnvt();
                // this.resources = yaml.getResourceListFromEnvt();
                sb = this.getResourceRegistryRawEntry();
            }
            if (sb == null) {
                this.resourceRegistryEntryRaw = new StringBuilder("");
            } else {
                this.resourceRegistryEntryRaw = sb;
            }
        } catch (Exception e) {
            logger.debug("Exception:", e);
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

    public Set<MsoHeatEnvironmentParameter> getParameters() {
        return this.parameters;
    }

    public Set<MsoHeatEnvironmentResource> getResources() {
        return this.resources;
    }

    public void setParameters(Set<MsoHeatEnvironmentParameter> paramSet) {
        if (paramSet == null) {
            this.parameters = null;
        } else {
            this.parameters = paramSet;
        }
    }

    public void setResources(Set<MsoHeatEnvironmentResource> resourceSet) {
        if (resourceSet == null) {
            this.resources = null;
        } else {
            this.resources = resourceSet;
        }
    }

    public void addParameter(MsoHeatEnvironmentParameter hep) {
        if (this.parameters == null) {
            this.parameters = new HashSet<>();
        }
        this.parameters.add(hep);
    }

    public void addResource(MsoHeatEnvironmentResource her) {
        if (this.resources == null) {
            this.resources = new HashSet<>();
        }
        this.resources.add(her);
    }

    public int getNumberOfParameters() {
        return this.parameters.size();
    }

    public int getNumberOfResources() {
        return this.resources.size();
    }

    public boolean hasResources() {
        if (this.resources != null && !this.resources.isEmpty()) {
            return true;
        }
        return false;
    }

    public boolean hasParameters() {
        if (this.parameters != null && !this.parameters.isEmpty()) {
            return true;
        }
        return false;
    }

    public boolean containsParameter(String paramName) {
        boolean contains = false;
        if (this.parameters == null || this.parameters.isEmpty()) {
            return false;
        }
        if (this.parameters.contains(new MsoHeatEnvironmentParameter(paramName))) {
            contains = true;
        }
        return contains;
    }

    public boolean containsParameter(String paramName, String paramAlias) {
        if (this.containsParameter(paramName)) {
            return true;
        }
        if (this.containsParameter(paramAlias)) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "MsoHeatEnvironmentEntry{" + "parameters=" + parameters + ", resourceRegistryEntryRaw='"
                + resourceRegistryEntryRaw + '\'' + '}';
    }

    public StringBuilder toFullStringExcludeNonParams(Set<HeatTemplateParam> params) {
        // Basically give back the envt - but exclude the params that aren't in the HeatTemplate

        StringBuilder sb = new StringBuilder();
        ArrayList<String> paramNameList = new ArrayList<>(params.size());
        for (HeatTemplateParam htp : params) {
            paramNameList.add(htp.getParamName());
        }

        if (this.hasParameters()) {
            sb.append("parameters:\n");
            for (MsoHeatEnvironmentParameter hep : this.parameters) {
                String paramName = hep.getName();
                if (paramNameList.contains(paramName)) {
                    // This parameter *is* in the Heat Template - so include it:
                    sb.append("  " + hep.getName() + ": " + hep.getValue() + "\n");
                    // New - 1607 - if any of the params mapped badly - JUST RETURN THE ORIGINAL ENVT!
                    if (hep.getValue().startsWith("_BAD")) {
                        return this.rawEntry;
                    }
                }
            }
            sb.append("\n");
        }
        // if (this.hasResources()) {
        // sb.append("resource_registry:\n");
        // for (MsoHeatEnvironmentResource her : this.resources) {
        // sb.append(" \"" + her.getName() + "\": " + her.getValue() + "\n");
        // }
        // }
        sb.append("\n");
        sb.append(this.resourceRegistryEntryRaw);
        return sb;
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
        // if (this.hasResources()) {
        // sb.append("resource_registry:\n");
        // for (MsoHeatEnvironmentResource her : this.resources) {
        // sb.append(" \"" + her.getName() + "\": " + her.getValue() + "\n");
        // }
        // }
        sb.append("\n");
        sb.append(this.resourceRegistryEntryRaw);
        return sb;
    }

    public StringBuilder getRawEntry() {
        return this.rawEntry;
    }

    private StringBuilder getResourceRegistryRawEntry() {

        if (this.rawEntry == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        int indexOf = this.rawEntry.indexOf("resource_registry:");
        if (indexOf < 0) { // no resource_registry:
            return null;
        }
        sb.append(this.rawEntry.substring(indexOf));
        return sb;
    }
}
