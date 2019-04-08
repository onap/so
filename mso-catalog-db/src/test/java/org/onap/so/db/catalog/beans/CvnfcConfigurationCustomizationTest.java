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
import org.junit.Test;

public class CvnfcConfigurationCustomizationTest {

    private static final String CONFIGURATION_FUNCTION = "testconfigurationFunction";
    private static final String CONFIGURATION_ROLE = "testconfigurationRole";
    private static final String CONFIGURATION_TYPE = "testconfigurationType";
    private static final Integer ID = new Integer(1);
    private static final String MODEL_CUSTOMIZATION_UUID = "testModelCustomizationUUID";
    private static final String MODEL_INSTANCE_NAME = "testModelInstanceName";
    private static final String MODEL_UUID = "testModelUUID";
    private static final String POLICY_NAME = "testPolicyName";

    @Test
    public final void testVnfVfmoduleCvnfcConfigurationCustomization() {
        CvnfcConfigurationCustomization vnfVfmoduleCvnfcConfigurationCustomization =
                new CvnfcConfigurationCustomization();
        vnfVfmoduleCvnfcConfigurationCustomization.setConfigurationFunction(CONFIGURATION_FUNCTION);
        vnfVfmoduleCvnfcConfigurationCustomization.setConfigurationResource(setupConfigurationResource());
        vnfVfmoduleCvnfcConfigurationCustomization.setConfigurationRole(CONFIGURATION_ROLE);
        vnfVfmoduleCvnfcConfigurationCustomization.setConfigurationType(CONFIGURATION_TYPE);
        CvnfcCustomization cvnfcCustomization = new CvnfcCustomization();
        cvnfcCustomization.setModelCustomizationUUID(MODEL_CUSTOMIZATION_UUID);
        vnfVfmoduleCvnfcConfigurationCustomization.setCvnfcCustomization(cvnfcCustomization);
        vnfVfmoduleCvnfcConfigurationCustomization.setId(ID);
        vnfVfmoduleCvnfcConfigurationCustomization.setModelCustomizationUUID(MODEL_CUSTOMIZATION_UUID);
        vnfVfmoduleCvnfcConfigurationCustomization.setModelInstanceName(MODEL_INSTANCE_NAME);
        vnfVfmoduleCvnfcConfigurationCustomization.setPolicyName(POLICY_NAME);

        assertTrue(vnfVfmoduleCvnfcConfigurationCustomization.getId().equals(new Integer(1)));
        assertTrue(
                vnfVfmoduleCvnfcConfigurationCustomization.getConfigurationFunction().equals(CONFIGURATION_FUNCTION));
        assertTrue(vnfVfmoduleCvnfcConfigurationCustomization.getConfigurationRole().equals(CONFIGURATION_ROLE));
        assertTrue(vnfVfmoduleCvnfcConfigurationCustomization.getConfigurationType().equals(CONFIGURATION_TYPE));
        assertTrue(vnfVfmoduleCvnfcConfigurationCustomization.getModelCustomizationUUID()
                .equals(MODEL_CUSTOMIZATION_UUID));
        assertTrue(vnfVfmoduleCvnfcConfigurationCustomization.getModelInstanceName().equals(MODEL_INSTANCE_NAME));
        assertTrue(vnfVfmoduleCvnfcConfigurationCustomization.getPolicyName().equals(POLICY_NAME));
        assertTrue(vnfVfmoduleCvnfcConfigurationCustomization.getCvnfcCustomization().getModelCustomizationUUID()
                .equals(MODEL_CUSTOMIZATION_UUID));
    }


    private ConfigurationResource setupConfigurationResource() {
        ConfigurationResource configurationResource = new ConfigurationResource();
        configurationResource.setModelUUID(MODEL_UUID);
        return configurationResource;
    }
}
