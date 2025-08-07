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

public class CvnfcCustomizationTest {

    private static final Integer ID = new Integer(1);
    private static final String DESCRIPTION = "testDescription";
    private static final String MODEL_CUSTOMIZATION_UUID = "testModelCustomizationUUID";
    private static final String MODEL_INSTANCE_NAME = "testModelInstanceName";
    private static final String MODEL_INVARIANT_UUID = "testModelInvariantUUID";
    private static final String MODEL_NAME = "testModelName";
    private static final String MODEL_UUID = "testModelUUID";
    private static final String MODEL_VERSION = "testModelVersion";
    private static final String TOSCA_NODE_TYPE = "testToscaNodeType";
    private static final String NFC_FUNCTION = "testNfcFunction";
    private static final String NFC_NAMING_CODE = "testNfcNamingCode";

    @Test
    public final void testCvnfcCustomization() {
        CvnfcCustomization cvnfcCustomization = new CvnfcCustomization();
        cvnfcCustomization.setDescription(DESCRIPTION);
        cvnfcCustomization.setId(ID);
        cvnfcCustomization.setModelCustomizationUUID(MODEL_CUSTOMIZATION_UUID);
        cvnfcCustomization.setModelInstanceName(MODEL_INSTANCE_NAME);
        cvnfcCustomization.setModelInvariantUUID(MODEL_INVARIANT_UUID);
        cvnfcCustomization.setModelName(MODEL_NAME);
        cvnfcCustomization.setModelUUID(MODEL_UUID);
        cvnfcCustomization.setModelVersion(MODEL_VERSION);
        cvnfcCustomization.setNfcFunction(NFC_FUNCTION);
        cvnfcCustomization.setNfcNamingCode(NFC_NAMING_CODE);
        cvnfcCustomization.setToscaNodeType(TOSCA_NODE_TYPE);
        cvnfcCustomization.setVfModuleCustomization(setupVfModuleCustomization());
        List<CvnfcConfigurationCustomization> cvnfcConfigurationCustomizationSet = new ArrayList();
        cvnfcConfigurationCustomizationSet.add(setupCvnfcConfigurationCustomization());
        cvnfcCustomization.setCvnfcConfigurationCustomization(cvnfcConfigurationCustomizationSet);

        assertTrue(cvnfcCustomization.getId().equals(new Integer(1)));
        assertTrue(cvnfcCustomization.getDescription().equals(DESCRIPTION));
        assertTrue(cvnfcCustomization.getModelCustomizationUUID().equals(MODEL_CUSTOMIZATION_UUID));
        assertTrue(cvnfcCustomization.getModelInstanceName().equals(MODEL_INSTANCE_NAME));
        assertTrue(cvnfcCustomization.getModelInvariantUUID().equals(MODEL_INVARIANT_UUID));
        assertTrue(cvnfcCustomization.getModelName().equals(MODEL_NAME));
        assertTrue(cvnfcCustomization.getModelUUID().equals(MODEL_UUID));
        assertTrue(cvnfcCustomization.getModelVersion().equals(MODEL_VERSION));
        assertTrue(cvnfcCustomization.getNfcFunction().equals(NFC_FUNCTION));
        assertTrue(cvnfcCustomization.getNfcNamingCode().equals(NFC_NAMING_CODE));
        assertTrue(cvnfcCustomization.getToscaNodeType().equals(TOSCA_NODE_TYPE));
        assertTrue(cvnfcCustomization.getVfModuleCustomization().getModelCustomizationUUID()
                .equals(MODEL_CUSTOMIZATION_UUID));
    }

    private VfModuleCustomization setupVfModuleCustomization() {
        VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
        vfModuleCustomization.setModelCustomizationUUID(MODEL_CUSTOMIZATION_UUID);
        return vfModuleCustomization;
    }


    private CvnfcConfigurationCustomization setupCvnfcConfigurationCustomization() {
        CvnfcConfigurationCustomization cvnfcConfigurationCustomization = new CvnfcConfigurationCustomization();
        cvnfcConfigurationCustomization.setModelCustomizationUUID(MODEL_CUSTOMIZATION_UUID);
        return cvnfcConfigurationCustomization;
    }
}
