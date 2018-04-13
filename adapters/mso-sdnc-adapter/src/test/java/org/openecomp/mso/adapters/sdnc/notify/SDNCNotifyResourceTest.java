/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *l
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.mso.adapters.sdnc.notify;

import org.junit.Test;
import org.mockito.Mock;

import javax.servlet.http.HttpServletRequest;

public class SDNCNotifyResourceTest {

    @Mock
    HttpServletRequest httpServletRequest;

    SDNCNotifyResource test = new SDNCNotifyResource();
//These tests are recheck for the class cast exception
 /*   @Test(expected = ClassFormatError.class)
    public void testPrintMessageException() {
        test.printMessage();
        test.printMessageParam("msg");
    }

    @Test(expected = ClassFormatError.class)
    public void testSDNCNotifyException() {
        test.SDNCNotify("reqXML", httpServletRequest);

    }*/
}
