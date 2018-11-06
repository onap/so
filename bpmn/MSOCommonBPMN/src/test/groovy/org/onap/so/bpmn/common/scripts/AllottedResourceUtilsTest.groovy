package org.onap.so.bpmn.common.scripts

import static org.junit.Assert.assertEquals
import static org.mockito.Matchers.eq
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake
import org.junit.Test
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.springframework.core.env.Environment

class AllottedResourceUtilsTest {

	
	@Test
	public void createARUrlTest() {
		AllottedResourceUtils utils = new AllottedResourceUtils(mock(AbstractServiceTaskProcessor.class))
		DelegateExecution execution = new DelegateExecutionFake()
		String allottedResourceId = "my-id"
		UrnPropertiesReader reader = new UrnPropertiesReader()
		Environment env = mock(Environment.class);
		
		when(env.getProperty(eq("mso.workflow.global.default.aai.version"))).thenReturn("14")
		when(env.getProperty(eq("aai.endpoint"))).thenReturn("http://localhost:8080")

		
		reader.setEnvironment(env)
		
		
		AAIResourceUri uri = mock(AAIResourceUri.class)
		when(uri.build()).thenReturn(new URI("/business/customers/customer/1/service-subscriptions/service-subscription/2/service-instances/service-instance/3"))
		String actual = utils.createARUrl(execution, uri, allottedResourceId)
		
		assertEquals("http://localhost:8080/aai/v14/business/customers/customer/1/service-subscriptions/service-subscription/2/service-instances/service-instance/3/allotted-resources/allotted-resource/my-id", actual)
	}
}
