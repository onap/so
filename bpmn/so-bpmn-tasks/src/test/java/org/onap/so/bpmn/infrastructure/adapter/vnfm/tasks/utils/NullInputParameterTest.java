/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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
package org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.utils;

import static org.junit.Assert.assertTrue;
import java.util.Collections;
import org.junit.Test;

/**
 * @author Waqas Ikram (waqas.ikram@ericsson.com)
 *
 */
public class NullInputParameterTest {

    @Test(expected = UnsupportedOperationException.class)
    public void test_addExtVirtualLinks_throwException() {
        NullInputParameter.NULL_INSTANCE.addExtVirtualLinks(Collections.emptyList());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test_setAdditionalParams_throwException() {
        NullInputParameter.NULL_INSTANCE.setAdditionalParams(Collections.emptyMap());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test_setExtVirtualLinks_throwException() {
        NullInputParameter.NULL_INSTANCE.setExtVirtualLinks(Collections.emptyList());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test_putAdditionalParams_throwException() {
        NullInputParameter.NULL_INSTANCE.putAdditionalParams(Collections.emptyMap());
    }

    @Test
    public void test_getAdditionalParams_ReturnEmptyCollection() {
        assertTrue(NullInputParameter.NULL_INSTANCE.getAdditionalParams().isEmpty());
    }

    @Test
    public void test_getExtVirtualLinks_ReturnEmptyCollection() {
        assertTrue(NullInputParameter.NULL_INSTANCE.getExtVirtualLinks().isEmpty());
    }

}
