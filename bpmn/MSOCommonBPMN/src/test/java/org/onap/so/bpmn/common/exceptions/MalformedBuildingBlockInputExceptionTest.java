/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 IBM.
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
package org.onap.so.bpmn.common.exceptions;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MalformedBuildingBlockInputExceptionTest {

    @Test
    public void testMalformedBuildingBlockInputException() {

        MalformedBuildingBlockInputException mbie1 = new MalformedBuildingBlockInputException();
        assertEquals(null, mbie1.getMessage());

        MalformedBuildingBlockInputException mbie2 = new MalformedBuildingBlockInputException("failed");
        assertEquals("failed", mbie2.getMessage());

        MalformedBuildingBlockInputException mbie3 = new MalformedBuildingBlockInputException("failed",
                new Throwable("throwable"));
        assertEquals("throwable", mbie3.getCause().getMessage());

        MalformedBuildingBlockInputException mbie4 = new MalformedBuildingBlockInputException(
                new Throwable("throwable"));
        assertEquals("throwable", mbie4.getCause().getMessage());

        MalformedBuildingBlockInputException mbie5 = new MalformedBuildingBlockInputException("TestMsg",
                new Throwable("throwable"), true, false);
        assertEquals("TestMsg", mbie5.getMessage());

    }
}
