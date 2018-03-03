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

package org.openecomp.mso.properties;


import java.io.IOException;

import org.openecomp.mso.logger.MsoLogger;

public abstract class AbstractMsoProperties {

	public static final int DEFAULT_RELOAD_TIME_MIN=1;
	
	public static final String RELOAD_TIME_PROPERTY="mso.properties.reload.time.minutes";
	
	protected static MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.GENERAL);

	protected String propertiesFileName;

	protected int automaticRefreshInMinutes=0;

	public String getPropertiesFileName() {
		return propertiesFileName;
	}

	public int getAutomaticRefreshInMinutes() {
		return automaticRefreshInMinutes;
	}
	
	protected synchronized void reloadPropertiesFile() throws IOException {
		this.loadPropertiesFile(this.propertiesFileName);
	}
	
	/**
	 * This method load a properties file from a source path.
	 *
	 * @param propertiesPath The path to the file
	 * @throws IOException In case of issues during the opening
	 */
	
	protected abstract void loadPropertiesFile(String propertiesPath) throws IOException;
	
	@Override
	protected abstract AbstractMsoProperties clone();
	
	@Override
	public abstract int hashCode();
	
	@Override
	public abstract boolean equals(Object obj);
	
	@Override
	public abstract String toString();
	
	
}
