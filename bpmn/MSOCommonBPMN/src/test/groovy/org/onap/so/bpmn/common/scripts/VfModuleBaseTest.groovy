/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG
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

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertTrue

/**
 * VfModuleBase is abstract, so we use a concrete subclass for testing.
 */
@RunWith(MockitoJUnitRunner.class)
class VfModuleBaseTest {

    // Concrete subclass to test the abstract VfModuleBase
    static class TestableVfModuleBase extends VfModuleBase {
        @Override
        void preProcessRequest(DelegateExecution execution) {
            // no-op for testing
        }
    }

    TestableVfModuleBase vfModuleBase

    @Before
    void init() {
        vfModuleBase = new TestableVfModuleBase()
    }

    @Test
    void testFindVfModule_found() {
        String genericVnf = """
            <generic-vnf>
                <vnf-id>vnf-123</vnf-id>
                <vf-modules>
                    <vf-module>
                        <vf-module-id>mod-001</vf-module-id>
                        <vf-module-name>module1</vf-module-name>
                        <is-base-vf-module>true</is-base-vf-module>
                    </vf-module>
                    <vf-module>
                        <vf-module-id>mod-002</vf-module-id>
                        <vf-module-name>module2</vf-module-name>
                        <is-base-vf-module>false</is-base-vf-module>
                    </vf-module>
                </vf-modules>
            </generic-vnf>
        """
        VfModule result = vfModuleBase.findVfModule(genericVnf, "mod-001")
        assertNotNull(result)
        assertTrue(result.isBaseVfModule())
        assertEquals("mod-001", result.getElementText("vf-module-id"))
    }

    @Test
    void testFindVfModule_notFound() {
        String genericVnf = """
            <generic-vnf>
                <vnf-id>vnf-123</vnf-id>
                <vf-modules>
                    <vf-module>
                        <vf-module-id>mod-001</vf-module-id>
                        <vf-module-name>module1</vf-module-name>
                        <is-base-vf-module>true</is-base-vf-module>
                    </vf-module>
                </vf-modules>
            </generic-vnf>
        """
        VfModule result = vfModuleBase.findVfModule(genericVnf, "nonexistent")
        assertNull(result)
    }

    @Test
    void testFindVfModule_noVfModules() {
        String genericVnf = """
            <generic-vnf>
                <vnf-id>vnf-123</vnf-id>
            </generic-vnf>
        """
        VfModule result = vfModuleBase.findVfModule(genericVnf, "mod-001")
        assertNull(result)
    }

    @Test
    void testFindVfModule_singleModule_isOnly() {
        String genericVnf = """
            <generic-vnf>
                <vnf-id>vnf-123</vnf-id>
                <vf-modules>
                    <vf-module>
                        <vf-module-id>mod-001</vf-module-id>
                        <vf-module-name>module1</vf-module-name>
                        <is-base-vf-module>true</is-base-vf-module>
                    </vf-module>
                </vf-modules>
            </generic-vnf>
        """
        VfModule result = vfModuleBase.findVfModule(genericVnf, "mod-001")
        assertNotNull(result)
        assertTrue(result.isOnlyVfModule())
    }

    @Test
    void testTransformNetworkParamsToVnfNetworks_nullInput() {
        String result = vfModuleBase.transformNetworkParamsToVnfNetworks(null)
        assertEquals('', result)
    }

    @Test
    void testTransformNetworkParamsToVnfNetworks_emptyInput() {
        String result = vfModuleBase.transformNetworkParamsToVnfNetworks('')
        assertEquals('', result)
    }

    @Test
    void testTransformNetworkParamsToVnfNetworks_withNetworkParam() {
        String paramsXml = """
            <vnf-params>
                <param name="oam_network">oam-net</param>
                <param name="some_other">value</param>
            </vnf-params>
        """
        String result = vfModuleBase.transformNetworkParamsToVnfNetworks(paramsXml)
        assertTrue(result.contains('<network-role>oam</network-role>'))
        assertTrue(result.contains('<network-name>oam-net</network-name>'))
        // non-network params should not appear
        assertTrue(!result.contains('some_other'))
    }

    @Test
    void testTransformNetworkParamsToVnfNetworks_multipleNetworks() {
        String paramsXml = """
            <vnf-params>
                <param name="oam_network">oam-net</param>
                <param name="management_network">mgmt-net</param>
            </vnf-params>
        """
        String result = vfModuleBase.transformNetworkParamsToVnfNetworks(paramsXml)
        assertTrue(result.contains('<network-role>oam</network-role>'))
        assertTrue(result.contains('<network-role>management</network-role>'))
    }

    @Test
    void testTransformParamsToEntries_nullInput() {
        String result = vfModuleBase.transformParamsToEntries(null)
        assertEquals('', result)
    }

    @Test
    void testTransformParamsToEntries_emptyInput() {
        String result = vfModuleBase.transformParamsToEntries('')
        assertEquals('', result)
    }

    @Test
    void testTransformParamsToEntries_withParams() {
        String paramsXml = """
            <vnf-params>
                <param name="key1">value1</param>
                <param name="key2">value2</param>
            </vnf-params>
        """
        String result = vfModuleBase.transformParamsToEntries(paramsXml)
        assertTrue(result.contains('<key>key1</key>'))
        assertTrue(result.contains('<value>value1</value>'))
        assertTrue(result.contains('<key>key2</key>'))
        assertTrue(result.contains('<value>value2</value>'))
    }

    @Test
    void testTransformVolumeParamsToEntries_nullInput() {
        String result = vfModuleBase.transformVolumeParamsToEntries(null)
        assertEquals('', result)
    }

    @Test
    void testTransformVolumeParamsToEntries_filtersReservedKeys() {
        String paramsXml = """
            <volume-params>
                <param name="vnf_id">id-123</param>
                <param name="vnf_name">name-123</param>
                <param name="vf_module_id">mod-123</param>
                <param name="vf_module_name">modname-123</param>
                <param name="custom_key">custom_value</param>
            </volume-params>
        """
        String result = vfModuleBase.transformVolumeParamsToEntries(paramsXml)
        // Reserved keys should be filtered out
        assertTrue(!result.contains('vnf_id'))
        assertTrue(!result.contains('vnf_name'))
        assertTrue(!result.contains('vf_module_id'))
        assertTrue(!result.contains('vf_module_name'))
        // Custom key should be present
        assertTrue(result.contains('<key>custom_key</key>'))
        assertTrue(result.contains('<value>custom_value</value>'))
    }
}
