package org.openecomp.mso.bpmn.common;

import static org.junit.Assert.assertNotNull;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetGenericVnfById;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;

/**
 * Unit test for GenerateVfModuleName.bpmn.
 */
public class GenerateVfModuleNameTest extends WorkflowTest {
	private static final String EOL = "\n";
	
	@Test	
	@Deployment(resources = {
			"subprocess/GenerateVfModuleName.bpmn"
		})
	public void  TestGenerateVfModuleNameSuvvess() {
		// 
		String request =
			"<vnf-request xmlns=\"http://ecomp.att.com/mso/infra/vnf-request/v1\">" + EOL +
			"  <request-info>" + EOL +
			"    <action>CREATE_VF_MODULE</action>" + EOL +
			"    <source>PORTAL</source>" + EOL +
			"  </request-info>" + EOL +
			"  <vnf-inputs>" + EOL +
			"    <vnf-name>STMTN5MMSC22</vnf-name>" + EOL +
			"    <vf-module-name>STMTN5MMSC22-MMSC::module-0-0</vf-module-name>" + EOL +
			"    <vf-module-model-name>MMSC::module-0</vf-module-model-name>" + EOL +
			"    <persona-model-id>973ed047-d251-4fb9-bf1a-65b8949e0a73</persona-model-id>" + EOL +
			"    <persona-model-version>1.0</persona-model-version>" + EOL +
			"    <service-id>00000000-0000-0000-0000-000000000000</service-id>" + EOL +
			"    <aic-node-clli>MDTWNJ21</aic-node-clli>" + EOL +
			"    <tenant-id>fba1bd1e195a404cacb9ce17a9b2b421</tenant-id>" + EOL +
			"  </vnf-inputs>" + EOL +
			"  <vnf-params xmlns:tns=\"http://ecomp.att.com/mso/infra/vnf-request/v1\"/>" + EOL +
			"</vnf-request>" + EOL;

		MockGetGenericVnfById(".*", "GenericFlows/getGenericVnfByNameResponse.xml");
		RuntimeService runtimeService = processEngineRule.getRuntimeService();				
		Map<String, Object> variables = new HashMap<String, Object>();		
		variables.put("isDebugLogEnabled","true");		
		variables.put("vnfName", "STMTN5MMSC20");
		variables.put("vfModuleLabel", "ModuleLabel1");
		variables.put("personaModelId", "extrovert");
		variables.put("vnfId", "12345678-f41f-4822-9323-b75962763d74");
		
		runtimeService.startProcessInstanceByKey("GenerateVfModuleName", variables);
		String response = BPMNUtil.getVariable(processEngineRule, "GenerateVfModuleName", "vfModuleName");
		
		assertNotNull(response);
	}
}