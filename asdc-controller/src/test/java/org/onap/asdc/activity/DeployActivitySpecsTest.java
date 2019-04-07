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
package org.onap.asdc.activity;

import org.junit.Test;
import org.onap.so.asdc.BaseTest;
import org.onap.so.asdc.activity.DeployActivitySpecs;
import org.springframework.beans.factory.annotation.Autowired;


public class DeployActivitySpecsTest extends BaseTest {

    @Autowired
    private DeployActivitySpecs deployActivitySpecs;

    @Test
    public void DeployActivitySpecs_Test() throws Exception {
        // Mock catalog DB results
        deployActivitySpecs.deployActivities();
    }
}
