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

package org.openecomp.mso.adapters.sdnc.util;

import java.util.UUID;
import org.junit.Test;


public class SDNCRequestIdUtilTest {

    /**
     * Test method for {@link org.openecomp.mso.adapters.sdnc.SDNCRequestIdUtil#getSDNCOriginalRequestId()}.
     */
    @Test
    public final void testGetSDNCOriginalRequestId () {
    	String originalRequestId = UUID.randomUUID().toString();
    	String postfixedRequestId = originalRequestId + "-1466203712068";
    	String postfixedRequestId2 = originalRequestId + "-1466203712068-2";

        assert(SDNCRequestIdUtil.getSDNCOriginalRequestId(postfixedRequestId).equals(originalRequestId));
        assert(SDNCRequestIdUtil.getSDNCOriginalRequestId(postfixedRequestId2).equals(postfixedRequestId2));
       
    }

}
