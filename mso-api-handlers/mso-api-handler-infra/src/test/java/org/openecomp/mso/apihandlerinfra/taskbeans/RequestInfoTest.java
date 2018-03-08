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

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.mso.apihandlerinfra.tasksbeans.RequestInfo;
import org.openecomp.mso.apihandlerinfra.tasksbeans.ValidResponses;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RequestInfoTest {

    RequestInfo _requestInfo;
    String _source;
    ValidResponses _responseValue;
    String _requestorId;

    public RequestInfoTest() {
    }

    @Before
    public void setUp() {
        _requestInfo = mock(RequestInfo.class);
        _responseValue = ValidResponses.abort;
        _requestorId = "ab1234";
        _source = "VID";
        when(_requestInfo.getRequestorId()).thenReturn(_requestorId);
        when(_requestInfo.getSource()).thenReturn(_source);
        when(_requestInfo.getResponseValue()).thenReturn(_responseValue);

    }

    @After
    public void tearDown() {
        _requestInfo = null;
        _responseValue = null;
    }

    /**
     * Test of getSource method
     */
    @Test
    public void testGetSource() {
        String result = _requestInfo.getSource();
        assertEquals(_source, result);

    }

    /**
     * Test setSource
     */
    @Test
    public void testSetSource() {
        _requestInfo.setSource("VID");
        verify(_requestInfo).setSource(_source);
    }

    /**
     * Test of getRequestorId method
     */
    @Test
    public void testGetRequestorId() {
        String result = _requestInfo.getRequestorId();
        assertEquals(_requestorId, result);

    }

    /**
     * Test setRequestInfo
     */
    @Test
    public void testSetRequestorId() {
        _requestInfo.setRequestorId(_requestorId);
        verify(_requestInfo).setRequestorId(_requestorId);
    }


    /**
     * Test of getResponseValue method
     */
    @Test
    public void testGetResponseValue() {
        ValidResponses result = _requestInfo.getResponseValue();
        assertEquals(_responseValue, result);

    }

    /**
     * Test setResponseValues method
     */
    @Test
    public void testSetResponseValue() {
        _requestInfo.setResponseValue(ValidResponses.abort);
        verify(_requestInfo).setResponseValue(_responseValue);
    }
}
