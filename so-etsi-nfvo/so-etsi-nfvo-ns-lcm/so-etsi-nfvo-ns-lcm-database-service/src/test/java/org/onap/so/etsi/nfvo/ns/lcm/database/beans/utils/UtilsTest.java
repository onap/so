/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.etsi.nfvo.ns.lcm.database.beans.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public class UtilsTest {

    @Test
    public void testTwoEmptyLists_equal() {
        assertTrue(Utils.isEquals(Collections.emptyList(), Collections.emptyList()));
    }

    @Test
    public void testEmptyListAndNonEmpty_notEqual() {
        assertFalse(Utils.isEquals(Collections.emptyList(), Arrays.asList("A")));
    }

    @Test
    public void testTwoNullLists_equal() {
        assertTrue(Utils.isEquals(null, null));
    }

    @Test
    public void testNullListAndEmptyList_notEqual() {
        assertFalse(Utils.isEquals(null, Collections.emptyList()));
    }

    @Test
    public void testTwoNotEmptyListsContainSameObjects_equal() {
        assertTrue(Utils.isEquals(Arrays.asList("A"), Arrays.asList("A")));
    }

    @Test
    public void testTwoNotEmptyListsContainsDifferentObjects_equal() {
        assertFalse(Utils.isEquals(Arrays.asList("A"), Arrays.asList(1)));
    }


}
