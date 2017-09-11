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

package org.openecomp.mso.bpmn.core;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;

import org.openecomp.mso.logger.MsoAlarmLogger;
import org.openecomp.mso.logger.MsoLogger;

/**
 * Logs a text message.  The text may contain variable references.
 * For example:<br/><br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;name=$name, address=$address
 * <p>
 * Required fields:<br/><br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;text: The text to log<br/>
 * Optional fields:<br/><br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;level: The log level (TRACE, DEBUG, INFO, WARN, ERROR)<br/>
 */
public class LogTask extends BaseTask {
	

	private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL);
	private static MsoAlarmLogger alarmLogger = new MsoAlarmLogger();

	private Expression text;
	private Expression level;
	
	public void execute(DelegateExecution execution) throws Exception {
		String theText = getStringField(text, execution, "text");
				

		
			StringBuilder out = new StringBuilder();
			StringBuilder var = new StringBuilder();
			boolean inVar = false;

			int pos = 0;
			int len = theText.length();

			while (pos < len) {
				char c = theText.charAt(pos++);

				if (inVar && !Character.isLetterOrDigit(c) && c != '_') {
					if (var.length() > 0) {
						Object value = execution.getVariable(var.toString());

						if (value != null) {
							out.append(value.toString());
						}

						var.setLength(0);
					}

					inVar = false;
				}

				if (c == '$') {
					inVar = true;
				} else {
					if (inVar) {
						var.append(c);
					} else {
						out.append(c);
					}
				}
			}

			if (inVar && var.length() > 0) {
				Object value = execution.getVariable(var.toString());
				if (value != null) {
					out.append(value.toString());
				}
			}

			
		
	}
}
