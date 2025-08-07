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

package org.onap.so.adapters.appc.orchestrator.client;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.appc.client.lcm.exceptions.AppcClientException;
import org.onap.appc.client.lcm.model.Status;
import org.onap.so.adapters.appc.orchestrator.client.ApplicationControllerSupport;
import org.onap.so.adapters.appc.orchestrator.client.StatusCategory;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class ApplicationControllerSupportTest {
    private ApplicationControllerSupport applicationControllerSupport = new ApplicationControllerSupport();

    public static Object[][] statusesAndCategories() {
        return new Object[][] {{100, StatusCategory.NORMAL}, {200, StatusCategory.ERROR}, {300, StatusCategory.ERROR},
                {400, StatusCategory.NORMAL}, {401, StatusCategory.ERROR}, {500, StatusCategory.NORMAL},
                {501, StatusCategory.ERROR}, {502, StatusCategory.WARNING}, {800, StatusCategory.WARNING},};
    }

    public static Object[][] statusesAndFinalities() {
        return new Object[][] {{100, false}, {200, true}, {300, true}, {400, true}, {500, false}, {800, true},};
    }

    @Test
    @Parameters(method = "statusesAndCategories")
    public void shouldReturnCategoryForCode(int code, StatusCategory category) throws Exception {
        // when
        StatusCategory detectedCategory = applicationControllerSupport.getCategoryOf(createStatus(code));
        // then
        assertThat(detectedCategory).isEqualTo(category);
    }

    @Test
    @Parameters(method = "statusesAndFinalities")
    public void shouldReturnFinalityForCode(int code, boolean expectedFinality) throws Exception {
        // when
        boolean finality = applicationControllerSupport.getFinalityOf(createStatus(code));
        // then
        assertThat(finality).isEqualTo(expectedFinality);
    }

    @Test
    public void buildStatusFromAppcException_Test() {
        String errorMessage = "errormessage";
        AppcClientException exception = new AppcClientException(errorMessage);
        Status status = applicationControllerSupport.buildStatusFromAppcException(exception);
        assertThat(status.getCode()).isEqualTo(200);
        assertThat(status.getMessage()).isEqualTo("Exception on APPC request: " + errorMessage);
    }

    private Status createStatus(int code) {
        Status status = new Status();
        status.setCode(code);
        return status;
    }
}
