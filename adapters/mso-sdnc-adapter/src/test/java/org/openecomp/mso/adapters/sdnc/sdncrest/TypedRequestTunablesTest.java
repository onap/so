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
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.mso.adapters.sdnc.sdncrest;

import org.junit.Assert;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class TypedRequestTunablesTest {

    @Test
    public void testTypedRequestTunables() {

        TypedRequestTunables test = new TypedRequestTunables("reqId","myUrlSuffix");
        test.setServiceKey("service","operation");
        assertEquals(test.getReqId(),"reqId");
        Assert.assertNull(test.getError());
        Assert.assertNull(test.getReqMethod());
        Assert.assertNull(test.getTimeout());
        Assert.assertNull(test.getSdncUrl());
        Assert.assertNull(test.getHeaderName());
        Assert.assertNull(test.getNamespace());
        Assert.assertNull(test.getMyUrl());
        Assert.assertFalse(test.setTunables());

    }

}

