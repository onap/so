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

package org.openecomp.mso.apihandlerinfra.taskbeans;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.openecomp.mso.apihandlerinfra.tasksbeans.Variables;
import org.openecomp.mso.apihandlerinfra.tasksbeans.Value;

public class VariablesTest {

    Variables _variables;
    protected Value _source;
    protected Value _responseValue;
    protected Value _requestorId;

    @Before
    public void setUp() {
        _variables = mock(Variables.class);
        _source = mock(Value.class);
        _responseValue = mock(Value.class);
        _requestorId = mock(Value.class);

        when(_variables.getSource()).thenReturn(_source);
        when(_variables.getRequestorId()).thenReturn(_requestorId);
        when(_variables.getResponseValue()).thenReturn(_responseValue);

    }

    @After
    public void tearDown() {
        _variables = null;
        _source = null;
        _responseValue = null;
        _requestorId = null;
    }

    @Test
    public void testGetSource() {
        _variables.setSource(_source);
        assertTrue(_variables.getSource().equals(_source));
    }

    @Test
    public void testSetSource() {
        _variables.setSource(_source);
        verify(_variables).setSource(_source);
    }

    @Test
    public void testGetResponseValue() {
        _variables.setResponseValue(_responseValue);
        assertTrue(_variables.getResponseValue().equals(_responseValue));
    }

    @Test
    public void testSetResponseValue() {
        _variables.setResponseValue(_responseValue);
        verify(_variables).setResponseValue(_responseValue);
    }

    @Test
    public void testGetRequestorId() {
        _variables.setRequestorId(_requestorId);
        assertTrue(_variables.getRequestorId().equals(_requestorId));
    }

    @Test
    public void testSetRequestorId() {
        _variables.setRequestorId(_requestorId);
        verify(_variables).setRequestorId(_requestorId);
    }

}
