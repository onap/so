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

package org.onap.so.db.catalog.beans;

import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class VnfcCustomizationTest {

    private static final String DESCRIPTION = "testDescription";
    private static final String MODEL_CUSTOMIZATION_UUID = "testModelCustomizationUUID";
    private static final String MODEL_INSTANCE_NAME = "testModelInstanceName";
    private static final String MODEL_INVARIANT_UUID = "testModelInvariantUUID";
    private static final String MODEL_NAME = "testModelName";
    private static final String MODEL_UUID = "testModelUUID";
    private static final String MODEL_VERSION = "testModelVersion";
    private static final String TOSCA_NODE_TYPE = "testToscaNodeType";

    @Test
    public final void testVnfcCustomization() {
        VnfcCustomization vnfcCustomization = new VnfcCustomization();
        vnfcCustomization.setCvnfcCustomization(setupCvnfcCustomizationList());
        vnfcCustomization.setDescription(DESCRIPTION);
        vnfcCustomization.setModelCustomizationUUID(MODEL_CUSTOMIZATION_UUID);
        vnfcCustomization.setModelInstanceName(MODEL_INSTANCE_NAME);
        vnfcCustomization.setModelInvariantUUID(MODEL_INVARIANT_UUID);
        vnfcCustomization.setModelName(MODEL_NAME);
        vnfcCustomization.setModelUUID(MODEL_UUID);
        vnfcCustomization.setModelVersion(MODEL_VERSION);
        vnfcCustomization.setToscaNodeType(TOSCA_NODE_TYPE);

        assertTrue(vnfcCustomization.getDescription().equals(DESCRIPTION));
        assertTrue(vnfcCustomization.getModelCustomizationUUID().equals(MODEL_CUSTOMIZATION_UUID));
        assertTrue(vnfcCustomization.getModelInstanceName().equals(MODEL_INSTANCE_NAME));
        assertTrue(vnfcCustomization.getModelInvariantUUID().equals(MODEL_INVARIANT_UUID));
        assertTrue(vnfcCustomization.getModelName().equals(MODEL_NAME));
        assertTrue(vnfcCustomization.getModelUUID().equals(MODEL_UUID));
        assertTrue(vnfcCustomization.getModelVersion().equals(MODEL_VERSION));
        assertTrue(vnfcCustomization.getToscaNodeType().equals(TOSCA_NODE_TYPE));
        assertTrue(vnfcCustomization.getCvnfcCustomization().get(0).getModelCustomizationUUID()
                .equals(MODEL_CUSTOMIZATION_UUID));
    }

    private List<CvnfcCustomization> setupCvnfcCustomizationList() {
        CvnfcCustomization testCvnfcCustomization = new CvnfcCustomization();
        testCvnfcCustomization.setModelCustomizationUUID(MODEL_CUSTOMIZATION_UUID);
        testCvnfcCustomization.setDescription(DESCRIPTION);
        testCvnfcCustomization.setModelVersion(MODEL_VERSION);
        testCvnfcCustomization.setModelInstanceName(MODEL_INSTANCE_NAME);
        testCvnfcCustomization.setToscaNodeType(TOSCA_NODE_TYPE);
        List<CvnfcCustomization> testCvnfcCustomizationList = new ArrayList();
        testCvnfcCustomizationList.add(testCvnfcCustomization);
        return testCvnfcCustomizationList;
    }

}
