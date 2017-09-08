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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Iterator;

import javax.xml.transform.stream.StreamSource;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
//import java.util.logging.Logger;
import org.camunda.bpm.engine.delegate.Expression;

import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;

/**
 * Executes an XQuery script.
 * <p>
 * Required fields:<br/><br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;scriptFile: the XQuery script file path<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;outputVariable: the output variable name<br/>
 * <p>
 * Optional fields:<br/><br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;xmlInputVariables: CSV list of variables containing
 * 		XML data to be injected into the script<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;atomicInputVariables: CSV list of variables containing
 * 		atomic data to be injected into the script<br/>
 */
public class XQueryScriptTask extends BaseTask {
	
	private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL);

	private Expression scriptFile;
	private Expression xmlInputVariables;
	private Expression atomicInputVariables;
	private Expression outputVariable;

	public void execute(DelegateExecution execution) throws Exception {
		if (msoLogger.isDebugEnabled()) {
			msoLogger.debug("Started Executing " + getTaskName());
		}

		String theScriptFile =
			getStringField(scriptFile, execution, "scriptFile");
		String theXmlInputVariables =
			getOptionalStringField(xmlInputVariables, execution, "xmlInputVariables");
		String theAtomicInputVariables =
			getOptionalStringField(atomicInputVariables, execution, "atomicInputVariables");
		String theOutputVariable =
			getStringField(outputVariable, execution, "outputVariable");

		if (msoLogger.isDebugEnabled()) {
			System.out.println("scriptFile = " + theScriptFile
				+ " xmlInputVariables = " + theXmlInputVariables
				+ " atomicInputVariables = " + theAtomicInputVariables
				+ "outputVariable = " + theOutputVariable);
		}

		String[] xmlInputVariableArray = (theXmlInputVariables == null)
			? new String[0] : theXmlInputVariables.split(",[ ]*");

		String[] atomicInputVariableArray = (theAtomicInputVariables == null)
			? new String[0] : theAtomicInputVariables.split(",[ ]*");

		Boolean shouldFail = (Boolean) execution.getVariable("shouldFail");

		if (shouldFail != null && shouldFail) {
			throw new ProcessEngineException(getClass().getSimpleName() + " Failed");
		}

		// The script could be compiled once and reused, but we are reading it
		// and compiling it every time.
		Configuration configuration = new Configuration();
		Processor processor = new Processor(configuration);
		XQueryCompiler compiler = processor.newXQueryCompiler();
		XQueryExecutable executable = compile(compiler, theScriptFile);

		// The evaluator must not be shared by multiple threads.  Here is where
		// the initial context may be set, as well as values of external variables.
		XQueryEvaluator evaluator = executable.load();

		// Convert XML string variable content to document-node objects and inject
		// these into the evaluator.  Note: the script must accept the document-node
		// type.  Most MSO scripts today expect element() input, not document-node
		// input.  TODO: figure out how to pass the variable data as element() types.

		for (String xmlInputVariable : xmlInputVariableArray) {
			if (msoLogger.isDebugEnabled()) {
				msoLogger.debug("Injecting XML variable '" + xmlInputVariable + "'");
				msoLogger.debug("printing the variable content>>'" + execution.getVariable(xmlInputVariable) +"'");
			}

			String xml = (String) execution.getVariable(xmlInputVariable);
			DocumentBuilder documentBuilder = processor.newDocumentBuilder();
			StreamSource source = new StreamSource(new ByteArrayInputStream(xml.getBytes("UTF-8")));
			XdmNode xdmNode = documentBuilder.build(source);

			// Inject the document-node object into the XQueryEvaluator.
			// TODO: transform it to an element()
			QName variable = new QName(xmlInputVariable);
			evaluator.setExternalVariable(variable, xdmNode);
		}

		// Inject atomic variables into the evaluator.

		for (String atomicInputVariable : atomicInputVariableArray) {
			
			if (msoLogger.isDebugEnabled()) {
				System.out.println("Injecting object variable '"
					+ atomicInputVariable + "'");
			}

			QName variable = new QName(atomicInputVariable);
			Object value = execution.getVariable(atomicInputVariable);

			if (value == null) {
				// The variable value is null, so we have no way to know what
				// type it is.  I don't know how to deal with this, so for
				// now, just skip it.
				
				msoLogger.warn (MessageEnum.BPMN_VARIABLE_NULL, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.DataError, atomicInputVariable);
				
				continue;
			}

			// There might be a better way to do this...
			if (value instanceof BigDecimal) {
				evaluator.setExternalVariable(variable,
					new XdmAtomicValue((BigDecimal) value));
			} else if (value instanceof Boolean) {
				evaluator.setExternalVariable(variable,
					new XdmAtomicValue((Boolean) value));
			} else if (value instanceof Double) {
				evaluator.setExternalVariable(variable,
					new XdmAtomicValue((Double) value));
			} else if (value instanceof Float) {
				evaluator.setExternalVariable(variable,
					new XdmAtomicValue((Float) value));
			} else if (value instanceof Long) {
				evaluator.setExternalVariable(variable,
					new XdmAtomicValue((Long) value));
			} else if (value instanceof String) {
				evaluator.setExternalVariable(variable,
					new XdmAtomicValue((String) value));
			} else if (value instanceof URI) {
				evaluator.setExternalVariable(variable,
					new XdmAtomicValue((URI) value));
			} else {
				throw new BadInjectedFieldException(
					"atomicInputVariables", getTaskName(),
					"'" + atomicInputVariable + "' type is not supported: "
						+ value.getClass());
			}
		}

		// Evaluate the query and collect the output.
		StringBuilder output = new StringBuilder();
		Iterator<XdmItem> xdmItems = evaluator.iterator();
		while (xdmItems.hasNext()) {
			XdmItem item = xdmItems.next();
			
			if (msoLogger.isDebugEnabled()) {
				msoLogger.debug("XQuery result item = " + item);
			}

			output.append(item.toString());
		}

		// Set the output variable.
		execution.setVariable(theOutputVariable, output.toString());

		if (msoLogger.isDebugEnabled()) {
			msoLogger.debug("Done Executing " + getTaskName());
		}
	}
	
	/**
	 * Compiles an XQuery script contained in a resource (file).
	 * @param compiler the XQueryCompiler
	 * @param resource the resource path
	 * @return an XQueryExecutable
	 * @throws Exception on error
	 */
	private XQueryExecutable compile(XQueryCompiler compiler, String resource)
			throws Exception {
		InputStream xqStream = null;
		try {
			xqStream = getClass().getResourceAsStream(resource);

			if (xqStream == null) {
				throw new IOException("Resource not found: " + resource);
			}

			XQueryExecutable executable = compiler.compile(xqStream);
			xqStream.close();
			xqStream = null;
			return executable;
		} finally {
			if (xqStream != null) {
				try {
					xqStream.close();
				} catch (Exception e) {
					// Do nothing
				}
			}
		}
	}
}