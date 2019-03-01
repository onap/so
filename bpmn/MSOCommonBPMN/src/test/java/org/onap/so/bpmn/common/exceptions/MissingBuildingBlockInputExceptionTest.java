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
package org.onap.so.bpmn.common.exceptions;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MissingBuildingBlockInputExceptionTest {

    @Test
    public void testMissingBuildingBlockInputException() {

        MissingBuildingBlockInputException mbie1 = new MissingBuildingBlockInputException();
        assertEquals(null, mbie1.getMessage());

        MissingBuildingBlockInputException mbie2 = new MissingBuildingBlockInputException("failed");
        assertEquals("failed", mbie2.getMessage());

        MissingBuildingBlockInputException mbie3 = new MissingBuildingBlockInputException("failed",
                new Throwable("throwable"));
        assertEquals("throwable", mbie3.getCause().getMessage());

        MissingBuildingBlockInputException mbie4 = new MissingBuildingBlockInputException(new Throwable("throwable"));
        assertEquals("throwable", mbie4.getCause().getMessage());

        MissingBuildingBlockInputException mbie5 = new MissingBuildingBlockInputException("TestMsg",
                new Throwable("throwable"), true, false);
        assertEquals("TestMsg", mbie5.getMessage());

    }

}
