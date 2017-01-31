/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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

package org.openecomp.mso.bpmn.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;

import org.openecomp.mso.logger.MsoLogger;

/**
 * Reads the contents of a resource file as a string and stores it in an
 * execution variable.
 * <p>
 * Required fields:<br/><br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;file: the resource file path<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;outputVariable: the output variable name<br/>
 */
public class ReadConfigTask extends BaseTask {
	
	private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL);
	private static Properties properties = null;

	private Expression propertiesFile;

	public void execute(DelegateExecution execution) throws Exception {
		if (msoLogger.isDebugEnabled()) {
			msoLogger.debug("Started Executing " + getTaskName());
		}

		String thePropertiesFile =
			getStringField(propertiesFile, execution, "propertiesFile");

		if (msoLogger.isDebugEnabled()) {
			msoLogger.debug("propertiesFile = " + thePropertiesFile);
		}

		Boolean shouldFail = (Boolean) execution.getVariable("shouldFail");

		if (shouldFail != null && shouldFail) {
			throw new ProcessEngineException(getClass().getSimpleName() + " Failed");
		}

		synchronized (ReadConfigTask.class) {
			if (properties == null) {
				properties = new Properties();

				InputStream stream = null;

				try {
					stream = getClass().getResourceAsStream(thePropertiesFile);

					if (stream == null) {
						throw new IOException("Resource not found: " + thePropertiesFile);
					}

					properties.load(stream);

					stream.close();
					stream = null;

				} finally {
					if (stream != null) {
						try {
							stream.close();
						} catch (Exception e) {
							// Do nothing
						}
					}
				}
			}
		}

		for (Object objectKey : properties.keySet()) {
			String key = (String) objectKey;
			String value = properties.getProperty(key);

			if (msoLogger.isDebugEnabled()) {
				msoLogger.debug("Setting variable '" + key + "' to '" + value + "'");
			}

			execution.setVariable(key, value);
		}

		if (msoLogger.isDebugEnabled()) {
			msoLogger.debug("Done Executing " + getTaskName());
		}
	}
}
