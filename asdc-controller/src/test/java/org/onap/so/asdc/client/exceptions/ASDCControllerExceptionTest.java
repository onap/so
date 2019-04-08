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

package org.onap.so.asdc.client.exceptions;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import org.junit.Test;

public class ASDCControllerExceptionTest {
    private String exceptionMessage = "test message for exception";
    private String throwableMessage = "separate throwable that caused asdcDownloadException";

    @Test
    public void asdcParametersExceptionTest() {
        ASDCControllerException asdcDownloadException = new ASDCControllerException(exceptionMessage);

        Exception expectedException = new Exception(exceptionMessage);

        assertThat(asdcDownloadException, sameBeanAs(expectedException));
    }

    @Test
    public void asdcParametersExceptionThrowableTest() {
        Throwable throwableCause = new Throwable(throwableMessage);
        ASDCControllerException asdcDownloadException = new ASDCControllerException(exceptionMessage, throwableCause);

        Exception expectedException = new Exception(exceptionMessage, new Throwable(throwableMessage));

        assertThat(asdcDownloadException, sameBeanAs(expectedException));
    }
}
