/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.asdc.client;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.so.asdc.BaseTest;
import org.onap.so.asdc.client.exceptions.ASDCControllerException;
import org.springframework.beans.factory.annotation.Autowired;

public class ASDCControllerTest extends BaseTest {
    @Autowired
    private ASDCController asdcController;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void initASDCExceptionTest() throws Exception {
        expectedException.expect(ASDCControllerException.class);

        asdcController.changeControllerStatus(ASDCControllerStatus.IDLE);

        try {
            asdcController.initASDC();
        } finally {
            asdcController.closeASDC();
        }
    }
}
