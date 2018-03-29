/*
* ============LICENSE_START=======================================================
* ONAP : SO
* ================================================================================
* Copyright 2018 TechMahindra
*=================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* ============LICENSE_END=========================================================
*/
package org.openecomp.mso.requestsdb;

import static org.junit.Assert.*;
import java.sql.Timestamp;
import org.junit.Test;

public class SiteStatusTest {

    SiteStatus ss=new SiteStatus();
    Timestamp time=new Timestamp(10);
    @Test
    public void test() {
      ss.setCreated(time);
      ss.setSiteName("siteName");
      ss.setStatus(true);
      assertEquals(ss.getCreated(), time);
      assertEquals(ss.getSiteName(), "siteName");
      assertEquals(ss.getStatus(), true);
    }
    @Test
    public void testToString(){
       assert(ss.toString()!=null);
    }
}
