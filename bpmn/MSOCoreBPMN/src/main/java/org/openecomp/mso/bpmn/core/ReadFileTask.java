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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.openecomp.mso.logger.MsoLogger;

/**
 * Conditionally reads the contents of a resource file as a string and stores it
 * in an execution variable.  The file is read only if the value of the input
 * variable is null.
 * <p>
 * Required fields:<br/><br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;file: the resource file path<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;inputVariable: the input variable name<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;outputVariable: the output variable name<br/>
 */
public class ReadFileTask extends BaseTask {
	
	private Expression file;
	private Expression inputVariable;
	private Expression outputVariable;
	
	private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL);

	public void execute(DelegateExecution execution) throws Exception {
		if (msoLogger.isDebugEnabled()) {
			msoLogger.debug("Started Executing " + getTaskName());
		}

		String theInputVariable =
			getStringField(inputVariable, execution, "inputVariable");
		String theOutputVariable =
			getOutputField(outputVariable, execution, "outputVariable");
		String theFile =getStringField(file, execution, "file");

		if (msoLogger.isDebugEnabled()) {
			msoLogger.debug("inputVariable = " + theInputVariable
				+ " outputVariable = " + theOutputVariable
				+ "file = " + theFile);
		}

        if (shouldFail(execution)) {
            throw new ProcessEngineException(getTaskName() + " Failed");
        }

		Object value = execution.getVariable(theInputVariable);

		if (value == null) {
			try(InputStream xmlStream = getClass().getResourceAsStream(theFile)) {

				if (xmlStream == null) {
					throw new IOException("Resource not found: " + theFile);
				}

				BufferedReader reader = new BufferedReader(new InputStreamReader(xmlStream));
				value = reader.lines().collect(Collectors.joining());
			}
		}
		execution.setVariable(theInputVariable, value);
		execution.setVariable(theOutputVariable, value);
		System.out.println("ServiceInput - " + execution.getVariable("gServiceInput"));
		if (msoLogger.isDebugEnabled()) {
			msoLogger.debug("Done Executing " + getTaskName());
		}
	}
}
