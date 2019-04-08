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

package org.onap.so.utils;

import static org.junit.Assert.*;
import java.util.List;
import org.junit.Test;
import org.onap.so.utils.CheckResults.CheckResult;

public class CheckResultsTest {

    /**
     * Test method for {@link org.onap.so.utils.CheckResults#getResults()}.
     */
    @Test
    public final void testGetResults() {
        CheckResults cr = new CheckResults();
        cr.addHostCheckResult("host1", 0, "output");
        cr.addHostCheckResult("host2", 2, "output2");
        cr.addServiceCheckResult("host1", "service1", 0, "outputServ");
        cr.addServiceCheckResult("host1", "service2", 2, "outputServ2");
        cr.addServiceCheckResult("host2", "service1", 0, "output2Serv");
        cr.addServiceCheckResult("host2", "service2", 2, "output2Serv2");
        List<CheckResult> res = cr.getResults();
        assertEquals(res.size(), 6);
        assertEquals(res.get(0).getHostname(), "host1");
        assertEquals(res.get(1).getHostname(), "host2");
        assertEquals(res.get(2).getHostname(), "host1");
        assertEquals(res.get(3).getHostname(), "host1");
        assertEquals(res.get(4).getHostname(), "host2");
        assertEquals(res.get(5).getHostname(), "host2");
        assertEquals(res.get(0).getServicename(), null);
        assertEquals(res.get(3).getServicename(), "service2");
        assertEquals(res.get(5).getState(), 2);
    }

}
