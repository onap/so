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

package org.onap.so.bpmn.servicedecomposition.entities.exceptions;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class InvalidBuildingBlockInputExceptionTest {
    private static final String MESSAGE = "message";
    private static final Throwable CAUSE = new Throwable();
    private InvalidBuildingBlockInputException invalidBuildingBlockInputException;

    @Test
    public void defaultConstructorTest() {
        invalidBuildingBlockInputException = new InvalidBuildingBlockInputException();
        assertEquals(null, invalidBuildingBlockInputException.getMessage());
        assertEquals(null, invalidBuildingBlockInputException.getCause());
    }

    @Test
    public void messageConstructorTest() {
        invalidBuildingBlockInputException = new InvalidBuildingBlockInputException(MESSAGE);
        assertEquals(MESSAGE, invalidBuildingBlockInputException.getMessage());
        assertEquals(null, invalidBuildingBlockInputException.getCause());
    }

    @Test
    public void causeConstructorTest() {
        invalidBuildingBlockInputException = new InvalidBuildingBlockInputException(CAUSE);
        assertEquals(CAUSE.toString(), invalidBuildingBlockInputException.getMessage()); // CAUSE.toString because of
                                                                                         // the implementation of
                                                                                         // Exception(Throwable cause)
        assertEquals(CAUSE, invalidBuildingBlockInputException.getCause());
    }

    @Test
    public void messageAndCauseConstructorTest() {
        invalidBuildingBlockInputException = new InvalidBuildingBlockInputException(MESSAGE, CAUSE);
        assertEquals(MESSAGE, invalidBuildingBlockInputException.getMessage());
        assertEquals(CAUSE, invalidBuildingBlockInputException.getCause());
    }

    @Test
    public void messageAndCauseAndFlagsConstructorTest() {
        invalidBuildingBlockInputException = new InvalidBuildingBlockInputException(MESSAGE, CAUSE, true, true);
        assertEquals(MESSAGE, invalidBuildingBlockInputException.getMessage());
        assertEquals(CAUSE, invalidBuildingBlockInputException.getCause());
    }

}
