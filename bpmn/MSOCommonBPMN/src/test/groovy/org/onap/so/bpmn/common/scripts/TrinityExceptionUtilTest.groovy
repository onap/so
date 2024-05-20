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

import org.junit.Assert
import org.junit.Ignore
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;



import static org.mockito.Mockito.*

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
//import org.mockito.internal.debugging.MockitoDebuggerImpl
import org.mockito.junit.MockitoJUnitRunner
import org.onap.so.bpmn.common.scripts.TrinityExceptionUtil;
@RunWith(MockitoJUnitRunner.class)
import org.junit.Test

class TrinityExceptionUtilTest {
	
	String aotsFault ="""<n1:Fault xmlns:xml="http://www.w3.org/XML/1998/namespace" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:n1="http://csi.cingular.com/CSI/Namespaces/OneTicketingSystem/InfrastructureCommon/Types/Public/SoapFault.xsd" xmlns:cer="http://csi.cingular.com/CSI/Namespaces/OneTicketingSystem/InfrastructureCommon/Types/Public/ErrorResponse.xsd" xsi:schemaLocation="http://csi.cingular.com/CSI/Namespaces/OneTicketingSystem/InfrastructureCommon/Types/Public/SoapFault.xsd SoapFault-CDM.xsd">
	<n1:faultcode>xml:space</n1:faultcode>
	<n1:faultstring>String</n1:faultstring>
	<n1:faultactor>http://test.com</n1:faultactor>
	<n1:detail>
		<n1:CSIApplicationException>
			<cer:Response>
				<cer:code>400</cer:code>
				<cer:description>bad stuff</cer:description>
			</cer:Response>
			<cer:ServiceProviderEntity>
				<cer:reportingServiceEntity>String</cer:reportingServiceEntity>
				<cer:faultDate>*** ERROR ***</cer:faultDate>
				<cer:faultSequenceNumber>String</cer:faultSequenceNumber>
				<cer:faultLevel>String</cer:faultLevel>
				<cer:faultCode>String</cer:faultCode>
				<cer:faultDescription>String</cer:faultDescription>
				<cer:ServiceProviderRawError>
					<cer:code>String</cer:code>
					<cer:description>String</cer:description>
					<cer:BISError>
						<cer:code>String</cer:code>
						<cer:description>String</cer:description>
						<cer:origination>String</cer:origination>
						<cer:severity>String</cer:severity>
					</cer:BISError>
				</cer:ServiceProviderRawError>
			</cer:ServiceProviderEntity>
		</n1:CSIApplicationException>
		<n1:CSIInternalException>
			<cer:reportingServiceEntity>String</cer:reportingServiceEntity>
			<cer:faultDate>*** ERROR ***</cer:faultDate>
			<cer:faultSequenceNumber>String</cer:faultSequenceNumber>
			<cer:faultLevel>String</cer:faultLevel>
			<cer:faultCode>String</cer:faultCode>
			<cer:faultDescription>String</cer:faultDescription>
			<cer:cingularErrorCode>String</cer:cingularErrorCode>
			<cer:cingularErrorDescription>String</cer:cingularErrorDescription>
			<cer:cingularErrorCategory>String</cer:cingularErrorCategory>
		</n1:CSIInternalException>
	</n1:detail>
</n1:Fault>"""

	
	@Before
	public void init()
	{
		MockitoAnnotations.initMocks(this)
	}
	
	@Test
	@Ignore
	public void testMapAAIExceptionTCommonException() {
		
		
		
		String restFault = """
				<Fault>
<requestError>
<serviceException>
<messageId>SVC3002</messageId>
<text>Error writing output performing %1 on %2 (msg=%3) (ec=%4)</text>
<variables>
<variable>PUTcustomer</variable>
<variable>SubName01</variable>
<variable>Unexpected error reading/updating database:Adding this property for key [service-instance-id] and value [USSTU2CFCNC0101UJZZ01] violates a uniqueness constraint [service-instance-id]</variable>
<variable>ERR.5.4.5105</variable>
</variables>
</serviceException>
</requestError>
</Fault>
			"""	
		String errorString = """<requestError>
<serviceException>
<messageId>SVC3002</messageId>
<text>Error writing output performing %1 on %2 (msg=%3) (ec=%4)</text>
<variables>
<variable>PUTcustomer</variable>
<variable>SubName01</variable>
<variable>Unexpected error reading/updating database:Adding this property for key [service-instance-id] and value [USSTU2CFCNC0101UJZZ01] violates a uniqueness constraint [service-instance-id]</variable>
<variable>ERR.5.4.5105</variable>
</variables>
</serviceException>
</requestError>""" as String


			
		ExecutionEntity mockExecution = mock(ExecutionEntity.class)

		TrinityExceptionUtil util = new TrinityExceptionUtil()
		Assert.assertEquals(errorString, util.mapAAIExceptionTCommonException(restFault, mockExecution))
	}
	
	
	
	
	@Test
	public void testBuildException() {				
		ExecutionEntity mockExecution = mock(ExecutionEntity.class)
		when(mockExecution.getVariable("prefix")).thenReturn("test_")
		when(mockExecution.getVariable("test_ResponseCode")).thenReturn("400")
		ArrayList msgVars = new ArrayList()
		msgVars.add("var1")
		msgVars.add("var2")
		when(mockExecution.getVariable("test_errVariables")).thenReturn(msgVars)

		
		TrinityExceptionUtil util = new TrinityExceptionUtil()
		String msg = "Bad request"
		String errorString = """<tns:requestError xmlns:tns="http://org.onap/so/request/types/v1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://org.onap/so/request/types/v1 MsoServiceInstanceTypesV1.xsd">
	<tns:serviceException>
		<tns:messageId>SVC2000</tns:messageId>
		<tns:text>The following service error occurred: %1. Error code is %2.</tns:text>
			<tns:variables>var1</tns:variables>
			<tns:variables>var2</tns:variables>
	</tns:serviceException>
</tns:requestError>"""
		Assert.assertEquals(errorString, util.buildException(msg, mockExecution))
	}
	
	@Test
	public void testMapAOTSExecptionToCommonException() {
		ExecutionEntity mockExecution = mock(ExecutionEntity.class)
		when(mockExecution.getVariable("prefix")).thenReturn("test_")
		when(mockExecution.getVariable("test_ResponseCode")).thenReturn("400")
		
		TrinityExceptionUtil util = new TrinityExceptionUtil()
		String errorString = """<tns:requestError xmlns:tns="http://org.onap/so/request/types/v1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://org.onap/so/request/types/v1 MsoServiceInstanceTypesV1.xsd">
	<tns:serviceException>
		<tns:messageId>SVC2000</tns:messageId>
		<tns:text>The following service error occurred: %1. Error code is %2.</tns:text>
			<tns:variables>Received error from AOTS: bad stuff</tns:variables>
			<tns:variables>400</tns:variables>
	</tns:serviceException>
</tns:requestError>"""
		Assert.assertEquals(errorString, util.mapAOTSExecptionToCommonException(aotsFault, mockExecution))
	}
	
	
	@Test
	public void testParseError() {
	ExecutionEntity mockExecution = mock(ExecutionEntity.class)
	String errorString = "The following service error occurred: %1. Error code is %2."
	ArrayList msgVars = new ArrayList()
	msgVars.add("var1")
	msgVars.add("var2")
	when(mockExecution.getVariable("prefix")).thenReturn("test_")
	when(mockExecution.getVariable("test_errTxt")).thenReturn(errorString)
	when(mockExecution.getVariable("test_errVariables")).thenReturn(msgVars)
		
	TrinityExceptionUtil util = new TrinityExceptionUtil()

	Assert.assertEquals("The following service error occurred: var1. Error code is var2.", util.parseError(mockExecution))
}

	
	
}
