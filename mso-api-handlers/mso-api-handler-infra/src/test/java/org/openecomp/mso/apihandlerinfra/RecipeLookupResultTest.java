/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.openecomp.mso.apihandlerinfra;

import org.junit.After;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class RecipeLookupResultTest {

    RecipeLookupResult instance;

    public RecipeLookupResultTest() {
    }

    @Before
    public void setUp() {
        instance = mock(RecipeLookupResult.class);
    }

    @After
    public void tearDown() {
        instance = null;
    }

    /**
     * Test of getOrchestrationURI method
     */
    @Test
    public void testGetOrchestrationURI() {
        String expResult = "orchestrationURI";
        when(instance.getOrchestrationURI()).thenReturn(expResult);
        String result = instance.getOrchestrationURI();
        assertEquals(expResult, result);
    }


    /**
     * Test of setOrchestrationURI method.
     */
    @Test
    public void testSetOrchestrationURI() {
        String orchestrationUri = "orchestrationURI";
        instance.setOrchestrationURI(orchestrationUri);
        verify(instance).setOrchestrationURI(orchestrationUri);
    }

    /**
     * Test of getRecipeTimeout method
     */
    @Test
    public void testGetRecipeTimeout() {
        int expResult = 10;
        when(instance.getRecipeTimeout()).thenReturn(expResult);
        int result = instance.getRecipeTimeout();
        assertEquals(expResult, result);
    }


    /**
     * Test of setRecipeTimeout method.
     */
    @Test
    public void testSetRecipeTimeout() {
        int recipeTimeOut = 10;
        instance.setRecipeTimeout(recipeTimeOut);
        verify(instance).setRecipeTimeout(10);
    }


}
   