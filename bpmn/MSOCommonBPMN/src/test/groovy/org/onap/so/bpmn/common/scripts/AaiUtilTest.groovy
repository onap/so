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

package org.onap.so.bpmn.common.scripts

import com.github.tomakehurst.wiremock.junit.WireMockRule

import static org.junit.Assert.*;
import static org.mockito.Mockito.*

import org.onap.so.rest.HttpHeader
import org.mockito.MockitoAnnotations
import org.mockito.runners.MockitoJUnitRunner
import org.mockito.internal.debugging.MockitoDebuggerImpl
import org.junit.Before
import org.onap.so.bpmn.common.scripts.AaiUtil;
import org.junit.Rule;
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.Ignore
import org.junit.runner.RunWith
import org.junit.Before;
import org.junit.Test;
import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl
import org.camunda.bpm.engine.repository.ProcessDefinition

@RunWith(MockitoJUnitRunner.class)
@Ignore
class AaiUtilTest extends MsoGroovyTest {


	@Rule
	public WireMockRule wireMockRule = new WireMockRule(8090);

	@Rule
	public ExpectedException thrown = ExpectedException.none


	def aaiPaylod = "<allotted-resource xmlns=\"http://org.openecomp.aai.inventory/v9\">\n" +
			"\t\t\t\t<id>allottedResourceId</id>\n" +
			"\t\t\t\t<description></description>\n" +
			"\t\t\t\t<type>allottedResourceType</type>\n" +
			"\t\t\t\t<role>allottedResourceRole</role>\n" +
			"\t\t\t\t<selflink></selflink>\n" +
			"\t\t\t\t<model-invariant-id></model-invariant-id>\n" +
			"\t\t\t\t<model-version-id></model-version-id>\n" +
			"\t\t\t\t<model-customization-id></model-customization-id>\n" +
			"\t\t\t\t<orchestration-status>PendingCreate</orchestration-status>\n" +
			"\t\t\t\t<operation-status></operation-status>\n" +
			"\t\t\t\t<relationship-list>\n" +
			"\t\t\t\t\t<relationship>\n" +
			"               \t\t\t<related-to>service-instance</related-to>\n" +
			"               \t\t\t<related-link>CSI_resourceLink</related-link>\n" +
			"\t\t\t\t\t</relationship>\n" +
			"\t\t\t\t</relationship-list>\n" +
			"\t\t\t</allotted-resource>";

	@Test
	public void testGetVersionDefault() {
		ExecutionEntity mockExecution = setupMock('CreateAAIVfModule')
		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
		when(mockExecution.getVariable("mso.workflow.global.default.aai.version")).thenReturn('8')

		CreateAAIVfModule myproc = new CreateAAIVfModule()
		AaiUtil aaiUtil = new AaiUtil(myproc)
		def version = aaiUtil.getVersion(mockExecution, 'l3-network', 'CreateAAIVfModule')
		assertEquals('8', version)
	}

	@Test
	public void testGetVersionResourceSpecific() {
		ExecutionEntity mockExecution = setupMock('CreateAAIVfModule')
		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
		when(mockExecution.getVariable("mso.workflow.default.aai.l3-network.version")).thenReturn('7')
		when(mockExecution.getVariable("mso.workflow.global.default.aai.version")).thenReturn('8')
		CreateAAIVfModule myproc = new CreateAAIVfModule()
		AaiUtil aaiUtil = new AaiUtil(myproc)
		def version = aaiUtil.getVersion(mockExecution, 'l3-network', 'CreateAAIVfModule')
		assertEquals('7', version)
	}

	@Test
	public void testGetVersionFlowSpecific() {
		ExecutionEntity mockExecution = setupMock('CreateAAIVfModule')
		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
		when(mockExecution.getVariable("mso.workflow.custom.CreateAAIVfModule.aai.version")).thenReturn('6')
		when(mockExecution.getVariable("mso.workflow.default.aai.l3-network.version")).thenReturn('7')

		when(mockExecution.getVariable("mso.workflow.global.default.aai.version")).thenReturn('8')

		CreateAAIVfModule myproc = new CreateAAIVfModule()
		AaiUtil aaiUtil = new AaiUtil(myproc)
		def version = aaiUtil.getVersion(mockExecution, 'l3-network', 'CreateAAIVfModule')
		assertEquals('6', version)
	}

	@Test
	public void testGetVersionNotDefined() {
		thrown.expect(Exception.class)
		ExecutionEntity mockExecution = setupMock('CreateAAIVfModule')
		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
		CreateAAIVfModule myproc = new CreateAAIVfModule()
		AaiUtil aaiUtil = new AaiUtil(myproc)
		when(mockExecution.getVariable("mso.workflow.global.default.aai.version")).thenReturn("")

		def version = aaiUtil.getVersion(mockExecution, 'l3-network', 'CreateAAIVfModule')

	}

	@Test
	public void testExecuteAAIGetCall() {
		ExecutionEntity mockExecution = setupMock('CreateAAIVfModule')
		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
		when(mockExecution.getVariable("aai.auth")).thenReturn("9B2278E8B8E95F256A560719055F4DF3")
		when(mockExecution.getVariable("mso.msoKey")).thenReturn("aa3871669d893c7fb8abbcda31b88b4f")
		CreateAAIVfModule myproc = new CreateAAIVfModule()
		AaiUtil aaiUtil = new AaiUtil(myproc)
		def uri = aaiUtil.executeAAIGetCall(mockExecution,"http://localhost:8090/aai/v9/business/customers/customer/CUST/service-subscriptions/service-subscription/SVC/service-instances/service-instance/NST/allotted-resources/allotted-resource/allottedResourceId")
	}


	@Test
	public void testExecuteAAIPutCall() {
		ExecutionEntity mockExecution = setupMock('CreateAAIVfModule')
		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
		when(mockExecution.getVariable("aai.auth")).thenReturn("9B2278E8B8E95F256A560719055F4DF3")
		when(mockExecution.getVariable("mso.msoKey")).thenReturn("aa3871669d893c7fb8abbcda31b88b4f")
		CreateAAIVfModule myproc = new CreateAAIVfModule()
		AaiUtil aaiUtil = new AaiUtil(myproc)
		def uri = aaiUtil.executeAAIPutCall(mockExecution,"http://localhost:8090/aai/v9/business/customers/customer/CUST/service-subscriptions/service-subscription/SVC/service-instances/service-instance/NST/allotted-resources/allotted-resource/allottedResourceId",aaiPaylod)
	}

	@Test
	public void testGetNamespaceFromUri_twoArguments() {  // (execution, uri)
		ExecutionEntity mockExecution = setupMock('DeleteVfModuleVolumeInfraV1')
		//
		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
		when(mockExecution.getVariable("mso.workflow.global.default.aai.version")).thenReturn('10')
		when(mockExecution.getVariable("mso.workflow.default.aai.v10.l3-network.uri")).thenReturn('/aai/v10/network/l3-networks/l3-network')
		when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn('http://org.openecomp.aai.inventory/')
		//
		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
		CreateAAIVfModule myproc = new CreateAAIVfModule()
		AaiUtil aaiUtil = new AaiUtil(myproc)
		def ns = aaiUtil.getNamespaceFromUri(mockExecution, '/aai/v10/search/generic-query')
		assertEquals('http://org.openecomp.aai.inventory/v10', ns)
	}
}
