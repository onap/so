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

package org.openecomp.mso.db.catalog.test;


import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.UUID;

import org.junit.Test;

import org.openecomp.mso.db.catalog.beans.VnfResourceCustomization;

/**
 */

public class VnfResourceCustomizationTest {
	
    @Test
    public final void vnfResourceCustomizationTest () {
    	VnfResourceCustomization vrc = new VnfResourceCustomization();
    	vrc.setModelCustomizationUuid("004fccad-a9d1-4b34-b50b-ccb9800a178b");
    	vrc.setModelInstanceName("testName");
    	vrc.setMultiStageDesign("sampleDesign");
    	
    	assertTrue(vrc.getModelCustomizationUuid().equals("004fccad-a9d1-4b34-b50b-ccb9800a178b"));
    	assertTrue(vrc.getModelInstanceName().equals("testName"));
    	assertTrue(vrc.getMultiStageDesign().equals("sampleDesign"));
    }

}
