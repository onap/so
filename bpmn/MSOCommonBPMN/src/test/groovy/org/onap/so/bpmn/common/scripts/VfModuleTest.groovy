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

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue

@RunWith(MockitoJUnitRunner.class)
class VfModuleTest {

    private static final String VF_MODULE_XML = """
        <vf-module>
            <vf-module-id>abc123</vf-module-id>
            <vf-module-name>testModule</vf-module-name>
            <is-base-vf-module>true</is-base-vf-module>
        </vf-module>
    """

    private static final String VF_MODULE_NOT_BASE_XML = """
        <vf-module>
            <vf-module-id>def456</vf-module-id>
            <vf-module-name>nonBaseModule</vf-module-name>
            <is-base-vf-module>false</is-base-vf-module>
        </vf-module>
    """

    private Node parseXml(String xml) {
        return new XmlParser().parseText(xml)
    }

    @Test
    void testConstructor_baseModule() {
        Node node = parseXml(VF_MODULE_XML)
        VfModule vfModule = new VfModule(node, true)
        assertNotNull(vfModule)
        assertTrue(vfModule.isBaseVfModule())
        assertTrue(vfModule.isOnlyVfModule())
    }

    @Test
    void testConstructor_nonBaseModule() {
        Node node = parseXml(VF_MODULE_NOT_BASE_XML)
        VfModule vfModule = new VfModule(node, false)
        assertNotNull(vfModule)
        assertFalse(vfModule.isBaseVfModule())
        assertFalse(vfModule.isOnlyVfModule())
    }

    @Test
    void testGetNode() {
        Node node = parseXml(VF_MODULE_XML)
        VfModule vfModule = new VfModule(node, true)
        assertEquals(node, vfModule.getNode())
    }

    @Test
    void testGetElementText_existingChild() {
        Node node = parseXml(VF_MODULE_XML)
        VfModule vfModule = new VfModule(node, true)
        assertEquals("abc123", vfModule.getElementText("vf-module-id"))
    }

    @Test
    void testGetElementText_existingChildName() {
        Node node = parseXml(VF_MODULE_XML)
        VfModule vfModule = new VfModule(node, true)
        assertEquals("testModule", vfModule.getElementText("vf-module-name"))
    }

    @Test
    void testGetElementText_nonExistingChild() {
        Node node = parseXml(VF_MODULE_XML)
        VfModule vfModule = new VfModule(node, true)
        assertEquals("", vfModule.getElementText("non-existing-element"))
    }

    @Test
    void testIsOnlyVfModule_true() {
        Node node = parseXml(VF_MODULE_XML)
        VfModule vfModule = new VfModule(node, true)
        assertTrue(vfModule.isOnlyVfModule())
    }

    @Test
    void testIsOnlyVfModule_false() {
        Node node = parseXml(VF_MODULE_XML)
        VfModule vfModule = new VfModule(node, false)
        assertFalse(vfModule.isOnlyVfModule())
    }
}
