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

package org.openecomp.mso.vdu.utils;

import java.util.Map;

/*
 * This Java bean class describes the template model of a VDU as distributed
 * by SDC to SO.  It is composed of one or more templates, one of which must be
 * the main template, 
 * 
 * The structure of this class corresponds to the format in which the templates
 * and associated artifacts are represented in the SO Catalog.
 * 
 * The map keys will be the "path" that is used to reference these artifacts within
 * the other templates.  This may be relevant to how different VDU plugins package
 * the files for delivery to the sub-orchestrator.
 * 
 * In the future, it is possible that pre-packaged blueprints (e.g. complete TOSCA CSARs)
 * could be stored in the catalog (and added to this structure).
 * 
 * This bean is passed as an input to instantiateVdu and updateVdu.
 */

public class VduBlueprint {
	String vduModelId;
	String mainTemplateName;
	Map<String,byte[]> templateFiles;
	Map<String,byte[]> attachedFiles;

	public String getVduModelId() {
		return vduModelId;
	}

	public void setVduModelId(String vduModelId) {
		this.vduModelId = vduModelId;
	}

	public String getMainTemplateName() {
		return mainTemplateName;
	}

	public void setMainTemplateName(String mainTemplateName) {
		this.mainTemplateName = mainTemplateName;
	}

	public Map<String, byte[]> getTemplateFiles() {
		return templateFiles;
	}

	public void setTemplateFiles(Map<String, byte[]> templateFiles) {
		this.templateFiles = templateFiles;
	}

	public Map<String, byte[]> getAttachedFiles() {
		return attachedFiles;
	}

	public void setAttachedFiles(Map<String, byte[]> attachedFiles) {
		this.attachedFiles = attachedFiles;
	}

	@Override
    public String toString() {
        return "VduInfo {" +
                "id='" + vduModelId + '\'' +
                "mainTemplateName='" + mainTemplateName + '\'' +
                '}';
    }

}

