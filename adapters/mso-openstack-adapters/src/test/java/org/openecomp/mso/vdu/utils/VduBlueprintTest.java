/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 TechMahindra
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

package org.openecomp.mso.vdu.utils;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.mso.vdu.utils.VduBlueprint;

import org.junit.Assert;

public class VduBlueprintTest {

    private VduBlueprint vduBlueprint;

    private Map<String, byte[]> templateFiles;
    private Map<String, byte[]> attachedFiles;

    @Before
    public void setUp() {
        vduBlueprint = new VduBlueprint();
    }

    @Test
    public void testGetVduModelId() {
        vduBlueprint.setVduModelId("vduModelId");
        Assert.assertNotNull(vduBlueprint.getVduModelId());
        Assert.assertEquals(vduBlueprint.getVduModelId(), "vduModelId");
    }

    @Test
    public void testGetMainTemplateName() {
        vduBlueprint.setMainTemplateName("MainTemplateName");
        Assert.assertNotNull(vduBlueprint.getMainTemplateName());
        Assert.assertEquals(vduBlueprint.getMainTemplateName(), "MainTemplateName");
    }

    @Test
    public void testGetTemplateFiles() {
        byte[] templateFileData = "some template file data".getBytes();
        templateFiles = new HashMap<>();
        templateFiles.put("templateKey1", templateFileData);
        vduBlueprint.setTemplateFiles(templateFiles);
        Assert.assertNotNull(vduBlueprint.getTemplateFiles());
        Assert.assertTrue(vduBlueprint.getTemplateFiles().containsKey("templateKey1"));
        Assert.assertTrue(vduBlueprint.getTemplateFiles().containsValue(templateFileData));
    }

    @Test
    public void testGetAttachedFiles() {
        byte[] attachedFileData = "some file data".getBytes();
        attachedFiles = new HashMap<>();
        attachedFiles.put("attachedKey1", attachedFileData);
        vduBlueprint.setAttachedFiles(attachedFiles);
        Assert.assertNotNull(vduBlueprint.getAttachedFiles());
        Assert.assertTrue(vduBlueprint.getAttachedFiles().containsKey("attachedKey1"));
        Assert.assertTrue(vduBlueprint.getAttachedFiles().containsValue(attachedFileData));
    }
}
