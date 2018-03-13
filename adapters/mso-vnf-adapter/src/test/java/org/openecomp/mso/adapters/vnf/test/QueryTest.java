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

package org.openecomp.mso.adapters.vnf.test;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import javax.xml.ws.Holder;
import mockit.Mock;
import mockit.MockUp;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.mso.adapters.vnf.MsoVnfAdapter;
import org.openecomp.mso.adapters.vnf.MsoVnfAdapterImpl;
import org.openecomp.mso.adapters.vnf.exceptions.VnfException;
import org.openecomp.mso.openstack.beans.HeatStatus;
import org.openecomp.mso.openstack.beans.StackInfo;
import org.openecomp.mso.openstack.beans.VnfStatus;
import org.openecomp.mso.openstack.exceptions.MsoException;
import org.openecomp.mso.openstack.utils.MsoHeatUtils;

public class QueryTest {

    @Test
    public void testQueryCreatedVnf() throws VnfException {
        {
            new MockUp<MsoHeatUtils>() {
                @Mock
                public StackInfo queryStack(String cloudSiteId, String tenantId, String stackName) throws MsoException {
                    StackInfo info = new StackInfo("stackName", HeatStatus.CREATED);
                    return info;
                }
            };

            MsoVnfAdapter vnfAdapter = new MsoVnfAdapterImpl();
            String cloudId = "MT";
            String tenantId = "MSO_Test";
            String vnfName = "VNF_TEST1";
            Holder<Boolean> vnfExists = new Holder<>();
            Holder<String> vnfId = new Holder<>();
            Holder<VnfStatus> status = new Holder<>();
            Holder<Map<String, String>> outputs = new Holder<>();

            vnfAdapter.queryVnf(cloudId, tenantId, vnfName, null,
                    vnfExists, vnfId, status, outputs);

            assertTrue(vnfExists.value);
        }
    }

    @Test
    public void testQueryNotFoundVnf() throws VnfException {
        {
            new MockUp<MsoHeatUtils>() {
                @Mock
                public StackInfo queryStack(String cloudSiteId, String tenantId, String stackName) throws MsoException {
                    StackInfo info = new StackInfo("stackName", HeatStatus.NOTFOUND);
                    return info;
                }
            };

            MsoVnfAdapter vnfAdapter = new MsoVnfAdapterImpl();
            String cloudId = "MT";
            String tenantId = "MSO_Test";
            String vnfName = "VNF_TEST1";
            Holder<Boolean> vnfExists = new Holder<>();
            Holder<String> vnfId = new Holder<>();
            Holder<VnfStatus> status = new Holder<>();
            Holder<Map<String, String>> outputs = new Holder<>();

            vnfAdapter.queryVnf(cloudId, tenantId, vnfName, null,
                    vnfExists, vnfId, status, outputs);

            assertFalse(vnfExists.value);
        }
    }

    @Test(expected = VnfException.class)
    @Ignore // 1802 merge
    public void testQueryVnfWithException() throws VnfException {
        {
            MsoVnfAdapter vnfAdapter = new MsoVnfAdapterImpl();
            String cloudId = "MT";
            String tenantId = "MSO_Test";
            String vnfName = "VNF_TEST1";
            Holder<Boolean> vnfExists = new Holder<>();
            Holder<String> vnfId = new Holder<>();
            Holder<VnfStatus> status = new Holder<>();
            Holder<Map<String, String>> outputs = new Holder<>();

            vnfAdapter.queryVnf(cloudId, tenantId, vnfName, null,
                    vnfExists, vnfId, status, outputs);
        }
    }
}
