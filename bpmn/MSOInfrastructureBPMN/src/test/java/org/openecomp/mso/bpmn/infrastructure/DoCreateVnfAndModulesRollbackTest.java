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

package org.openecomp.mso.bpmn.infrastructure;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockDeleteGenericVnf;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockDeleteVfModuleId;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetGenericVnfById;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetGenericVnfByIdWithDepth;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetGenericVnfByName;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPatchGenericVnf;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPatchVfModuleId;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPutVfModuleId;
import static org.openecomp.mso.bpmn.mock.StubResponseDatabase.mockUpdateRequestDB;
import static org.openecomp.mso.bpmn.mock.StubResponseSDNCAdapter.mockSDNCAdapter;
import static org.openecomp.mso.bpmn.mock.StubResponseVNFAdapter.mockVNFDelete;
import static org.openecomp.mso.bpmn.mock.StubResponseVNFAdapter.mockVfModuleDelete;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.engine.test.Deployment;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.mso.bpmn.common.WorkflowTest;
import org.openecomp.mso.bpmn.core.RollbackData;
import org.openecomp.mso.bpmn.core.WorkflowException;
import org.openecomp.mso.bpmn.mock.FileUtil;

/**
 * Unit test for DoCreateVnfAndModulesRollback.bpmn.
 */
public class DoCreateVnfAndModulesRollbackTest extends WorkflowTest {
    private final CallbackSet callbacks = new CallbackSet();

    private static final String EOL = "\n";


    private final String vnfAdapterDeleteCallback =
            "<deleteVfModuleResponse>" + EOL +
                    "    <vnfId>a27ce5a9-29c4-4c22-a017-6615ac73c721</vnfId>" + EOL +
                    "    <vfModuleId>973ed047-d251-4fb9-bf1a-65b8949e0a73</vfModuleId>" + EOL +
                    "    <vfModuleDeleted>true</vfModuleDeleted>" + EOL +
                    "    <messageId>{{MESSAGE-ID}}</messageId>" + EOL +
                    "</deleteVfModuleResponse>" + EOL;

    private final String vnfAdapterDeleteCallbackFail =
            "<vfModuleException>" + EOL +
                    "    <message>Error processing request to VNF-Async. Not Found.</message>" + EOL +
                    "    <category>INTERNAL</category>" + EOL +
                    "    <rolledBack>false</rolledBack>" + EOL +
                    "    <messageId>{{MESSAGE-ID}}</messageId>" + EOL +
                    "</vfModuleException>" + EOL;

    private final String sdncAdapterDeleteCallback =
            "<output xmlns=\"com:att:sdnctl:l3api\">" + EOL +
                    "  <svc-request-id>{{REQUEST-ID}}</svc-request-id>" + EOL +
                    "  <ack-final-indicator>Y</ack-final-indicator>" + EOL +
                    "</output>" + EOL;

    public DoCreateVnfAndModulesRollbackTest() throws IOException {
        callbacks.put("sdncChangeDelete", sdncAdapterDeleteCallback);
        callbacks.put("sdncDelete", sdncAdapterDeleteCallback);
        callbacks.put("vnfDelete", vnfAdapterDeleteCallback);
        callbacks.put("vnfDeleteFail", vnfAdapterDeleteCallbackFail);
        callbacks.put("deactivate", FileUtil.readResourceFile(
                "__files/VfModularity/SDNCTopologyActivateCallback.xml"));
        callbacks.put("unassign", FileUtil.readResourceFile(
                "__files/VfModularity/SDNCTopologyActivateCallback.xml"));
    }

    @Test
    @Ignore
    @Deployment(resources = {
            "subprocess/DoCreateVnfAndModulesRollback.bpmn",
            "subprocess/DoCreateVfModuleRollback.bpmn",
            "subprocess/PrepareUpdateAAIVfModule.bpmn",
            "subprocess/UpdateAAIVfModule.bpmn",
            "subprocess/UpdateAAIGenericVnf.bpmn",
            "subprocess/DeleteAAIVfModule.bpmn",
            "subprocess/SDNCAdapterV1.bpmn",
            "subprocess/VnfAdapterRestV1.bpmn",
            "subprocess/GenericGetVnf.bpmn",
            "subprocess/GenericDeleteVnf.bpmn",
            "subprocess/DoDeleteVnf.bpmn"
    })
    public void TestDoCreateVnfAndModulesRollbackSuccess_BaseOnly() {
        // delete the Base Module and Generic Vnf
        // vnf-id=a27ce5a9-29c4-4c22-a017-6615ac73c721, vf-module-id=973ed047-d251-4fb9-bf1a-65b8949e0a73
        String request =
                "<vnf-request xmlns=\"http://org.openecomp/mso/infra/vnf-request/v1\">" + EOL +
                        "  <request-info>" + EOL +
                        "    <action>DELETE_VF_MODULE</action>" + EOL +
                        "    <source>PORTAL</source>" + EOL +
                        "  </request-info>" + EOL +
                        "  <vnf-inputs>" + EOL +
                        "    <vnf-id>a27ce5a9-29c4-4c22-a017-6615ac73c721</vnf-id>" + EOL +
                        "    <vnf-name>STMTN5MMSC21</vnf-name>" + EOL +
                        "    <vnf-type>asc_heat-int</vnf-type>" + EOL +
                        "    <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a73</vf-module-id>" + EOL +
                        "    <vf-module-name>STMTN5MMSC21-MMSC::module-0-0</vf-module-name>" + EOL +
                        "    <service-id>00000000-0000-0000-0000-000000000000</service-id>" + EOL +
                        "    <service-type>SDN-ETHERNET-INTERNET</service-type>" + EOL +
                        "    <tenant-id>fba1bd1e195a404cacb9ce17a9b2b421</tenant-id>" + EOL +
                        "    <orchestration-status>pending-delete</orchestration-status>" + EOL +
                        "    <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>" + EOL +
                        "  </vnf-inputs>" + EOL +
                        "  <vnf-params xmlns:tns=\"http://org.openecomp/mso/infra/vnf-request/v1\"/>" + EOL +
                        "</vnf-request>" + EOL;
        logStart();
        mockSDNCAdapter("/SDNCAdapter", "SvcAction>changedelete", 200, "DeleteGenericVNFV1/sdncAdapterResponse.xml");
        mockSDNCAdapter("/SDNCAdapter", "SvcAction>delete", 200, "DeleteGenericVNFV1/sdncAdapterResponse.xml");
        mockVNFDelete(".*", "/.*", 202);
        mockVfModuleDelete("78987");
        MockPatchVfModuleId("a27ce5a9-29c4-4c22-a017-6615ac73c721", "973ed047-d251-4fb9-bf1a-65b8949e0a73");
        MockPatchGenericVnf("a27ce5a9-29c4-4c22-a017-6615ac73c721");
        MockGetGenericVnfByName("STMTN5MMSC21", "DoCreateVfModule_getVnfResponse.xml");
        MockGetGenericVnfById("/a27ce5a9-29c4-4c22-a017-6615ac73c721.*", "DoCreateVfModule_getVnfResponse.xml", 200);
        MockPutVfModuleId("a27ce5a9-29c4-4c22-a017-6615ac73c721", "973ed047-d251-4fb9-bf1a-65b8949e0a73");
        MockDeleteGenericVnf("a27ce5a9-29c4-4c22-a017-6615ac73c721", "0000021");
        MockDeleteVfModuleId("a27ce5a9-29c4-4c22-a017-6615ac73c721", "973ed047-d251-4fb9-bf1a-65b8949e0a73", "0000073", 200);
        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        RollbackData rollbackData = new RollbackData();

        rollbackData.put("VFMODULE_BASE", "source", "PORTAL");
        rollbackData.put("VFMODULE_BASE", "vnfid", "a27ce5a9-29c4-4c22-a017-6615ac73c721");
        rollbackData.put("VFMODULE_BASE", "vnfname", "STMTN5MMSC21");
        rollbackData.put("VFMODULE_BASE", "vnftype", "asc_heat-int");
        rollbackData.put("VFMODULE_BASE", "vfmoduleid", "973ed047-d251-4fb9-bf1a-65b8949e0a73");
        rollbackData.put("VFMODULE_BASE", "vfmodulename", "STMTN5MMSC21-MMSC::module-0-0");
        rollbackData.put("VFMODULE_BASE", "tenantid", "fba1bd1e195a404cacb9ce17a9b2b421");
        rollbackData.put("VFMODULE_BASE", "aiccloudregion", "RDM2WAGPLCP");
        rollbackData.put("VFMODULE_BASE", "heatstackid", "thisisaheatstack");
        rollbackData.put("VFMODULE_BASE", "contrailNetworkPolicyFqdn0", "MSOTest:DefaultPolicyFQDN1");
        rollbackData.put("VFMODULE_BASE", "contrailNetworkPolicyFqdn1", "MSOTest:DefaultPolicyFQDN2");
        rollbackData.put("VFMODULE_BASE", "oamManagementV6Address", "2000:abc:bce:1111");
        rollbackData.put("VFMODULE_BASE", "oamManagementV4Address", "127.0.0.1");

        rollbackData.put("VFMODULE_BASE", "rollbackPrepareUpdateVfModule", "true");
        rollbackData.put("VFMODULE_BASE", "rollbackVnfAdapterCreate", "true");
        rollbackData.put("VFMODULE_BASE", "rollbackUpdateAAIVfModule", "true");
        rollbackData.put("VFMODULE_BASE", "rollbackSDNCRequestActivate", "true");
        rollbackData.put("VFMODULE_BASE", "rollbackCreateAAIVfModule", "true");
        rollbackData.put("VFMODULE_BASE", "rollbackCreateNetworkPoliciesAAI", "true");
        rollbackData.put("VFMODULE_BASE", "rollbackUpdateVnfAAI", "true");

        rollbackData.put("VNF", "vnfId", "testVnfId123");

        rollbackData.put("VNFANDMODULES", "numOfCreatedAddOnModules", "0");

        variables.put("isDebugLogEnabled", "true");
        variables.put("mso-request-id", "a27ce5a9-29c4-4c22-a017-6615ac73c721");
        variables.put("mso-service-instance-id", "a27ce5a9-29c4-4c22-a017-6615ac73c721");
        rollbackData.put("VFMODULE_BASE", "msorequestid", "a27ce5a9-29c4-4c22-a017-6615ac73c721");
        rollbackData.put("VFMODULE_BASE", "serviceinstanceid", "a27ce5a9-29c4-4c22-a017-6615ac73c721");

        variables.put("rollbackData", rollbackData);
        invokeSubProcess("DoCreateVnfAndModulesRollback", businessKey, variables);

        // "changedelete" operation not required for deleting a Vf Module
//		injectSDNCCallbacks(callbacks, "sdncChangeDelete");
        injectVNFRestCallbacks(callbacks, "vnfDelete");
        //waitForRunningProcessCount("DoCreateVnfAndModulesRollback", 0, 120000);
        injectSDNCCallbacks(callbacks, "sdncDelete");

        waitForProcessEnd(businessKey, 10000);
        WorkflowException wfe = (WorkflowException) getVariableFromHistory(businessKey, "WorkflowException");
        checkVariable(businessKey, "WorkflowException", null);
        if (wfe != null) {
            System.out.println("TestCreateVfModuleSuccess: ErrorCode=" + wfe.getErrorCode() +
                    ", ErrorMessage=" + wfe.getErrorMessage());
        }
        logEnd();
    }

    @Test
    @Deployment(resources = {
            "subprocess/DoCreateVnfAndModulesRollback.bpmn",
            "subprocess/PrepareUpdateAAIVfModule.bpmn",
            "subprocess/UpdateAAIVfModule.bpmn",
            "subprocess/UpdateAAIGenericVnf.bpmn",
            "subprocess/DeleteAAIVfModule.bpmn",
            "subprocess/SDNCAdapterV1.bpmn",
            "subprocess/GenericGetVnf.bpmn",
            "subprocess/GenericDeleteVnf.bpmn",
            "subprocess/DoDeleteVnf.bpmn"
    })
    public void TestDoCreateVnfAndModulesRollbackSuccess_vnfOnly() {
        // delete the Base Module and Generic Vnf
        // vnf-id=a27ce5a9-29c4-4c22-a017-6615ac73c721, vf-module-id=973ed047-d251-4fb9-bf1a-65b8949e0a73
        String request =
                "<vnf-request xmlns=\"http://org.openecomp/mso/infra/vnf-request/v1\">" + EOL +
                        "  <request-info>" + EOL +
                        "    <action>DELETE_VF_MODULE</action>" + EOL +
                        "    <source>PORTAL</source>" + EOL +
                        "  </request-info>" + EOL +
                        "  <vnf-inputs>" + EOL +
                        "    <vnf-id>a27ce5a9-29c4-4c22-a017-6615ac73c721</vnf-id>" + EOL +
                        "    <vnf-name>STMTN5MMSC21</vnf-name>" + EOL +
                        "    <vnf-type>asc_heat-int</vnf-type>" + EOL +
                        "    <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a73</vf-module-id>" + EOL +
                        "    <vf-module-name>STMTN5MMSC21-MMSC::module-0-0</vf-module-name>" + EOL +
                        "    <service-id>00000000-0000-0000-0000-000000000000</service-id>" + EOL +
                        "    <service-type>SDN-ETHERNET-INTERNET</service-type>" + EOL +
                        "    <tenant-id>fba1bd1e195a404cacb9ce17a9b2b421</tenant-id>" + EOL +
                        "    <orchestration-status>pending-delete</orchestration-status>" + EOL +
                        "    <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>" + EOL +
                        "  </vnf-inputs>" + EOL +
                        "  <vnf-params xmlns:tns=\"http://org.openecomp/mso/infra/vnf-request/v1\"/>" + EOL +
                        "</vnf-request>" + EOL;
        logStart();
        MockGetGenericVnfById("testVnfId123.*", "GenericFlows/getGenericVnfByNameResponse.xml");
        MockDeleteGenericVnf("testVnfId123", "testReVer123");
        MockDoDeleteVfModule_SDNCSuccess();
        mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        RollbackData rollbackData = new RollbackData();

        rollbackData.put("VNF", "vnfId", "testVnfId123");
        rollbackData.put("VNF", "rollbackVnfCreate", "true");
        rollbackData.put("VNF", "rollbackSDNCAssign", "true");
        rollbackData.put("VNF", "rollbackSDNCActivate", "true");
        rollbackData.put("VNFANDMODULES", "numOfCreatedAddOnModules", "0");


        variables.put("isDebugLogEnabled", "true");
        variables.put("mso-request-id", "a27ce5a9-29c4-4c22-a017-6615ac73c721");
        variables.put("mso-service-instance-id", "a27ce5a9-29c4-4c22-a017-6615ac73c721");


        variables.put("rollbackData", rollbackData);
        variables.put("sdncVersion", "1707");
        invokeSubProcess("DoCreateVnfAndModulesRollback", businessKey, variables);

        // "changedelete" operation not required for deleting a Vf Module
//		injectSDNCCallbacks(callbacks, "sdncChangeDelete");

        //waitForRunningProcessCount("DoCreateVnfAndModulesRollback", 0, 120000);
        //	injectSDNCCallbacks(callbacks, "sdncDelete");

        waitForProcessEnd(businessKey, 10000);
        WorkflowException wfe = (WorkflowException) getVariableFromHistory(businessKey, "WorkflowException");
        checkVariable(businessKey, "WorkflowException", null);
        if (wfe != null) {
            System.out.println("TestCreateVfModuleSuccess: ErrorCode=" + wfe.getErrorCode() +
                    ", ErrorMessage=" + wfe.getErrorMessage());
        }
        logEnd();
    }

    @Test
    @Ignore
    @Deployment(resources = {
            "subprocess/DoCreateVnfAndModulesRollback.bpmn",
            "subprocess/DoCreateVfModuleRollback.bpmn",
            "subprocess/PrepareUpdateAAIVfModule.bpmn",
            "subprocess/UpdateAAIVfModule.bpmn",
            "subprocess/UpdateAAIGenericVnf.bpmn",
            "subprocess/DeleteAAIVfModule.bpmn",
            "subprocess/SDNCAdapterV1.bpmn",
            "subprocess/VnfAdapterRestV1.bpmn",
            "subprocess/GenericGetVnf.bpmn",
            "subprocess/GenericDeleteVnf.bpmn",
            "subprocess/DoDeleteVnf.bpmn"
    })
    public void TestDoCreateVnfAndModulesRollbackSuccess_AddOn() {
        // delete the Base Module and Generic Vnf
        // vnf-id=a27ce5a9-29c4-4c22-a017-6615ac73c721, vf-module-id=973ed047-d251-4fb9-bf1a-65b8949e0a73
        String request =
                "<vnf-request xmlns=\"http://org.openecomp/mso/infra/vnf-request/v1\">" + EOL +
                        "  <request-info>" + EOL +
                        "    <action>DELETE_VF_MODULE</action>" + EOL +
                        "    <source>PORTAL</source>" + EOL +
                        "  </request-info>" + EOL +
                        "  <vnf-inputs>" + EOL +
                        "    <vnf-id>a27ce5a9-29c4-4c22-a017-6615ac73c721</vnf-id>" + EOL +
                        "    <vnf-name>STMTN5MMSC21</vnf-name>" + EOL +
                        "    <vnf-type>asc_heat-int</vnf-type>" + EOL +
                        "    <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a73</vf-module-id>" + EOL +
                        "    <vf-module-name>STMTN5MMSC21-MMSC::module-0-0</vf-module-name>" + EOL +
                        "    <service-id>00000000-0000-0000-0000-000000000000</service-id>" + EOL +
                        "    <service-type>SDN-ETHERNET-INTERNET</service-type>" + EOL +
                        "    <tenant-id>fba1bd1e195a404cacb9ce17a9b2b421</tenant-id>" + EOL +
                        "    <orchestration-status>pending-delete</orchestration-status>" + EOL +
                        "    <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>" + EOL +
                        "  </vnf-inputs>" + EOL +
                        "  <vnf-params xmlns:tns=\"http://org.openecomp/mso/infra/vnf-request/v1\"/>" + EOL +
                        "</vnf-request>" + EOL;
        logStart();
        mockSDNCAdapter("/SDNCAdapter", "SvcAction>changedelete", 200, "DeleteGenericVNFV1/sdncAdapterResponse.xml");
        mockSDNCAdapter("/SDNCAdapter", "SvcAction>delete", 200, "DeleteGenericVNFV1/sdncAdapterResponse.xml");
        mockVNFDelete(".*", "/.*", 202);
        mockVfModuleDelete("78987");
        MockGetGenericVnfByName("STMTN5MMSC21", "DoCreateVfModule_getVnfResponse.xml");
        MockGetGenericVnfById("/a27ce5a9-29c4-4c22-a017-6615ac73c721", "DoCreateVfModule_getVnfResponse.xml", 200);
        MockGetGenericVnfByIdWithDepth("a27ce5a9-29c4-4c22-a017-6615ac73c721", 1, "DoCreateVfModuleRollback/GenericVnf.xml");
        MockPatchVfModuleId("a27ce5a9-29c4-4c22-a017-6615ac73c721", "973ed047-d251-4fb9-bf1a-65b8949e0a73");
        MockPatchGenericVnf("a27ce5a9-29c4-4c22-a017-6615ac73c721");
        MockPutVfModuleId("a27ce5a9-29c4-4c22-a017-6615ac73c721", "973ed047-d251-4fb9-bf1a-65b8949e0a73");
        MockDeleteGenericVnf("a27ce5a9-29c4-4c22-a017-6615ac73c721", "0000021");
        MockDeleteVfModuleId("", "", "", 200);
        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        RollbackData rollbackData = new RollbackData();

        rollbackData.put("VFMODULE_BASE", "source", "PORTAL");
        rollbackData.put("VFMODULE_BASE", "vnfid", "a27ce5a9-29c4-4c22-a017-6615ac73c721");
        rollbackData.put("VFMODULE_BASE", "vnfname", "STMTN5MMSC21");
        rollbackData.put("VFMODULE_BASE", "vnftype", "asc_heat-int");
        rollbackData.put("VFMODULE_BASE", "vfmoduleid", "973ed047-d251-4fb9-bf1a-65b8949e0a73");
        rollbackData.put("VFMODULE_BASE", "vfmodulename", "STMTN5MMSC21-MMSC::module-0-0");
        rollbackData.put("VFMODULE_BASE", "tenantid", "fba1bd1e195a404cacb9ce17a9b2b421");
        rollbackData.put("VFMODULE_BASE", "aiccloudregion", "RDM2WAGPLCP");
        rollbackData.put("VFMODULE_BASE", "heatstackid", "thisisaheatstack");
        rollbackData.put("VFMODULE_BASE", "contrailNetworkPolicyFqdn0", "MSOTest:DefaultPolicyFQDN1");
        rollbackData.put("VFMODULE_BASE", "contrailNetworkPolicyFqdn1", "MSOTest:DefaultPolicyFQDN2");
        rollbackData.put("VFMODULE_BASE", "oamManagementV6Address", "2000:abc:bce:1111");
        rollbackData.put("VFMODULE_BASE", "oamManagementV4Address", "127.0.0.1");
        rollbackData.put("VFMODULE_BASE", "rollbackPrepareUpdateVfModule", "true");
        rollbackData.put("VFMODULE_BASE", "rollbackVnfAdapterCreate", "true");
        rollbackData.put("VFMODULE_BASE", "rollbackUpdateAAIVfModule", "true");
        rollbackData.put("VFMODULE_BASE", "rollbackSDNCRequestActivate", "true");
        rollbackData.put("VFMODULE_BASE", "rollbackCreateAAIVfModule", "true");
        rollbackData.put("VFMODULE_BASE", "rollbackCreateNetworkPoliciesAAI", "true");
        rollbackData.put("VFMODULE_BASE", "rollbackUpdateVnfAAI", "true");
        rollbackData.put("VFMODULE_BASE", "msorequestid", "a27ce5a9-29c4-4c22-a017-6615ac73c721");
        rollbackData.put("VFMODULE_BASE", "serviceinstanceid", "a27ce5a9-29c4-4c22-a017-6615ac73c721");


        rollbackData.put("VFMODULE_ADDON_1", "source", "PORTAL");
        rollbackData.put("VFMODULE_ADDON_1", "vnfid", "a27ce5a9-29c4-4c22-a017-6615ac73c721");
        rollbackData.put("VFMODULE_ADDON_1", "vnfname", "STMTN5MMSC21");
        rollbackData.put("VFMODULE_ADDON_1", "vnftype", "asc_heat-int");
        rollbackData.put("VFMODULE_ADDON_1", "vfmoduleid", "973ed047-d251-4fb9-bf1a-65b8949e0a73");
        rollbackData.put("VFMODULE_ADDON_1", "vfmodulename", "STMTN5MMSC21-MMSC::module-0-0");
        rollbackData.put("VFMODULE_ADDON_1", "tenantid", "fba1bd1e195a404cacb9ce17a9b2b421");
        rollbackData.put("VFMODULE_ADDON_1", "aiccloudregion", "RDM2WAGPLCP");
        rollbackData.put("VFMODULE_ADDON_1", "heatstackid", "thisisaheatstack");
        rollbackData.put("VFMODULE_ADDON_1", "contrailNetworkPolicyFqdn0", "MSOTest:DefaultPolicyFQDN1");
        rollbackData.put("VFMODULE_ADDON_1", "contrailNetworkPolicyFqdn1", "MSOTest:DefaultPolicyFQDN2");
        rollbackData.put("VFMODULE_ADDON_1", "oamManagementV6Address", "2000:abc:bce:1111");
        rollbackData.put("VFMODULE_ADDON_1", "oamManagementV4Address", "127.0.0.1");
        rollbackData.put("VFMODULE_ADDON_1", "rollbackPrepareUpdateVfModule", "true");
        rollbackData.put("VFMODULE_ADDON_1", "rollbackVnfAdapterCreate", "true");
        rollbackData.put("VFMODULE_ADDON_1", "rollbackUpdateAAIVfModule", "true");
        rollbackData.put("VFMODULE_ADDON_1", "rollbackSDNCRequestActivate", "true");
        rollbackData.put("VFMODULE_ADDON_1", "rollbackCreateAAIVfModule", "true");
        rollbackData.put("VFMODULE_ADDON_1", "rollbackCreateNetworkPoliciesAAI", "true");
        rollbackData.put("VFMODULE_ADDON_1", "rollbackUpdateVnfAAI", "true");
        rollbackData.put("VFMODULE_ADDON_1", "msorequestid", "a27ce5a9-29c4-4c22-a017-6615ac73c721");
        rollbackData.put("VFMODULE_ADDON_1", "serviceinstanceid", "a27ce5a9-29c4-4c22-a017-6615ac73c721");

        rollbackData.put("VNF", "vnfId", "testVnfId123");

        rollbackData.put("VNFANDMODULES", "numOfCreatedAddOnModules", "1");

        variables.put("isDebugLogEnabled", "true");
        variables.put("mso-request-id", "a27ce5a9-29c4-4c22-a017-6615ac73c721");
        variables.put("mso-service-instance-id", "a27ce5a9-29c4-4c22-a017-6615ac73c721");


        variables.put("rollbackData", rollbackData);
        invokeSubProcess("DoCreateVnfAndModulesRollback", businessKey, variables);

        // "changedelete" operation not required for deleting a Vf Module
        //	injectSDNCCallbacks(callbacks, "sdncChangeDelete");
        injectVNFRestCallbacks(callbacks, "vnfDelete");
        injectVNFRestCallbacks(callbacks, "vnfDelete");
        //waitForRunningProcessCount("DoCreateVnfAndModulesRollback", 0, 120000);
        injectSDNCCallbacks(callbacks, "sdncDelete");

        waitForProcessEnd(businessKey, 10000);
        WorkflowException wfe = (WorkflowException) getVariableFromHistory(businessKey, "WorkflowException");
        checkVariable(businessKey, "WorkflowException", null);
        if (wfe != null) {
            System.out.println("TestCreateVfModuleSuccess: ErrorCode=" + wfe.getErrorCode() +
                    ", ErrorMessage=" + wfe.getErrorMessage());
        }
        logEnd();
    }

    public static void MockDoDeleteVfModule_SDNCSuccess() {
        stubFor(post(urlEqualTo("/SDNCAdapter"))
                .withRequestBody(containing("SvcAction>deactivate"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBodyFile("DeleteGenericVNFV1/sdncAdapterResponse.xml")));
        stubFor(post(urlEqualTo("/SDNCAdapter"))
                .withRequestBody(containing("SvcAction>unassign"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBodyFile("DeleteGenericVNFV1/sdncAdapterResponse.xml")));
    }


}