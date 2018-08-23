/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
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
package org.onap.so.montoring.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


/**
 * @author waqas.ikram@ericsson.com
 */
public class AuthenticationDetailsTest {

    private static final String EMPTY_STRING = "";
    private static final String PASS_VALUE = "Joker";
    private static final String USER_NAME_VALUE = "JusticeLeague";

    @Test
    public void test_CamundaAuthenticationDetails_GetUsername_GetPassword() {
        final AuthenticationDetails objUnderTest = new AuthenticationDetails(USER_NAME_VALUE, PASS_VALUE);
        assertEquals(USER_NAME_VALUE, objUnderTest.getUsername());
        assertEquals(PASS_VALUE, objUnderTest.getPassword());
        assertTrue(objUnderTest.isValid());
    }

    @Test
    public void test_CamundaAuthenticationDetails_GetUsername_GetPassword_InvalidUsername() {
        AuthenticationDetails objUnderTest = new AuthenticationDetails(null, PASS_VALUE);
        assertFalse(objUnderTest.isValid());

        objUnderTest = new AuthenticationDetails(EMPTY_STRING, PASS_VALUE);
        assertFalse(objUnderTest.isValid());
    }

    @Test
    public void test_CamundaAuthenticationDetails_GetUsername_GetPassword_InvalidPassword() {
        AuthenticationDetails objUnderTest = new AuthenticationDetails(USER_NAME_VALUE, null);
        assertFalse(objUnderTest.isValid());

        objUnderTest = new AuthenticationDetails(USER_NAME_VALUE, EMPTY_STRING);
        assertFalse(objUnderTest.isValid());
    }

}
