/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Intellectual Property. All rights reserved.
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

package org.openecomp.mso.rest;

import org.apache.http.HttpResponse;
import org.junit.Test;

public class APIResponseTest {

    HttpResponse httpResponse;
    APIResponse test = new APIResponse(httpResponse);

    public APIResponseTest() throws RESTException {
    }


    @Test
    public void getStatusCodeTest() throws Exception {
        test.getStatusCode();
    }

    @Test
    public void getResponseBodyAsByteArrayTest() throws Exception {
        test.getResponseBodyAsByteArray();
    }

    @Test
    public void getResponseBodyAsStringTest() throws Exception {
        test.getResponseBodyAsString();
    }

    @Test
    public void getAllHeadersTest() throws Exception {
        test.getAllHeaders();
    }

    @Test
    public void getFirstHeaderTest() throws Exception {
        test.getFirstHeader("name");
    }
}
