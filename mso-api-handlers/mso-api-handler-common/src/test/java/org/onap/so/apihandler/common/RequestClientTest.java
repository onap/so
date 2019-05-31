/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (c) 2019 Samsung. All rights reserved.
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
package org.onap.so.apihandler.common;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RequestClientTest {

    private static final String ENCRYPTION_KEY = "aa3871669d893c7fb8abbcda31b88b4f";

    private RequestClient requestClient;

    @Before
    public void init() {
        requestClient = Mockito.mock(RequestClient.class, Mockito.CALLS_REAL_METHODS);
    }

    @Test
    public void getEncryptedPropValueWithSuccess() {

        String encryptedValue = requestClient.getEncryptedPropValue(
                "E8E19DD16CC90D2E458E8FF9A884CC0452F8F3EB8E321F96038DE38D5C1B0B02DFAE00B88E2CF6E2A4101AB2C011FC161212EE",
                "defaultValue", ENCRYPTION_KEY);

        Assert.assertEquals("apihBpmn:camunda-R1512!", encryptedValue);
    }

    @Test
    public void getDefaultEncryptedPropValue() {

        String encryptedValue =
                requestClient.getEncryptedPropValue("012345678901234567890123456789", "defaultValue", ENCRYPTION_KEY);

        Assert.assertEquals("defaultValue", encryptedValue);
    }

}
