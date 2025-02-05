/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.servicedecomposition.tasks.exceptions;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class MultipleObjectsFoundExceptionTest {
    private static final String MESSAGE = "message";
    private static final Throwable CAUSE = new Throwable();
    private MultipleObjectsFoundException MultipleObjectsFoundException;

    @Test
    public void defaultConstructorTest() {
        MultipleObjectsFoundException = new MultipleObjectsFoundException();
        assertEquals(null, MultipleObjectsFoundException.getMessage());
        assertEquals(null, MultipleObjectsFoundException.getCause());
    }

    @Test
    public void messageConstructorTest() {
        MultipleObjectsFoundException = new MultipleObjectsFoundException(MESSAGE);
        assertEquals(MESSAGE, MultipleObjectsFoundException.getMessage());
        assertEquals(null, MultipleObjectsFoundException.getCause());
    }

    @Test
    public void causeConstructorTest() {
        MultipleObjectsFoundException = new MultipleObjectsFoundException(CAUSE);
        assertEquals(CAUSE.toString(), MultipleObjectsFoundException.getMessage());
        assertEquals(CAUSE, MultipleObjectsFoundException.getCause());
    }

    @Test
    public void messageAndCauseConstructorTest() {
        MultipleObjectsFoundException = new MultipleObjectsFoundException(MESSAGE, CAUSE);
        assertEquals(MESSAGE, MultipleObjectsFoundException.getMessage());
        assertEquals(CAUSE, MultipleObjectsFoundException.getCause());
    }

    @Test
    public void messageAndCauseAndFlagsConstructorTest() {
        MultipleObjectsFoundException = new MultipleObjectsFoundException(MESSAGE, CAUSE, true, true);
        assertEquals(MESSAGE, MultipleObjectsFoundException.getMessage());
        assertEquals(CAUSE, MultipleObjectsFoundException.getCause());
    }
}
