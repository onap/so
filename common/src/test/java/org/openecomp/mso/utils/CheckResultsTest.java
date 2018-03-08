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

package org.openecomp.mso.utils;

import java.util.List;

import org.junit.Test;

import org.openecomp.mso.utils.CheckResults.CheckResult;

public class CheckResultsTest {

    /**
     * Test method for {@link org.openecomp.mso.utils.CheckResults#getResults()}.
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
        assert(res.size() == 6);
        assert(res.get(0).getHostname().equals("host1"));
        assert(res.get(1).getHostname().equals("host2"));
        assert(res.get(2).getHostname().equals("host1"));
        assert(res.get(3).getHostname().equals("host1"));
        assert(res.get(4).getHostname().equals("host2"));
        assert(res.get(5).getHostname().equals("host2"));
        assert(res.get(0).getServicename() == null);
        assert(res.get(3).getServicename().equals("service2"));
        assert(res.get(5).getState() == 2);
    }

}
