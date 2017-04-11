package org.openecomp.mso.bpmn.common;

import javax.xml.ws.Endpoint;

import org.camunda.bpm.engine.ProcessEngineServices;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openecomp.mso.bpmn.common.workflow.service.SDNCAdapterCallbackServiceImpl;

/**
 * A JUnit rule that starts the SDNC Adapter Callback Service before
 * every test, and tears it down after every test.  Example:
 * <pre>
 *     @Rule
 *     public final SDNCAdapterCallbackRule sdncAdapterCallbackRule =
 *         new SDNCAdapterCallbackRule(processEngineRule);
 * </pre>
 */
public class SDNCAdapterCallbackRule implements TestRule {
	public static final String DEFAULT_ENDPOINT_URL =
		"http://localhost:28080/mso/SDNCAdapterCallbackService";

	private final ProcessEngineServices processEngineServices;
	private final String endpointUrl;

	public SDNCAdapterCallbackRule(ProcessEngineServices processEngineServices) {
		this(processEngineServices, DEFAULT_ENDPOINT_URL);
	}

	public SDNCAdapterCallbackRule(ProcessEngineServices processEngineServices,
			String endpointUrl) {
		this.processEngineServices = processEngineServices;
		this.endpointUrl = endpointUrl;
	}

	@Override
	public Statement apply(final Statement baseStmt, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				Endpoint endpoint = null;

				try {
					SDNCAdapterCallbackServiceImpl sdncCallbackService = new SDNCAdapterCallbackServiceImpl();
					sdncCallbackService.setProcessEngineServices4junit(processEngineServices);

					System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
					System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");

					System.out.println("Publishing Endpoint - " + endpointUrl);
					endpoint = Endpoint.publish(endpointUrl, sdncCallbackService);

					baseStmt.evaluate();
				} finally {
					if (endpoint != null) {
						System.out.println("Stopping Endpoint - " + endpointUrl);
						endpoint.stop();
					}
				}
			}
		};
	}
}