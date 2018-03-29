/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.adapters.vnf;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import javax.xml.ws.Holder;

import org.junit.After;
import org.junit.Test;
import org.openecomp.mso.adapters.vnf.exceptions.VnfException;
import org.openecomp.mso.cloud.CloudConfigFactory;
import org.openecomp.mso.openstack.beans.HeatStatus;
import org.openecomp.mso.openstack.beans.StackInfo;
import org.openecomp.mso.openstack.beans.VnfStatus;
import org.openecomp.mso.openstack.exceptions.MsoException;
import org.openecomp.mso.openstack.utils.MsoHeatUtils;
import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.properties.MsoPropertiesFactory;

import mockit.Mock;
import mockit.MockUp;

public class QueryTest {

	private MockUp<MsoHeatUtils> mockUpHeatUtils1 = null;
	private MockUp<MsoHeatUtils> mockUpHeatUtils2 = null;
	
	@After
	 public void tearDown() {
		 if (mockUpHeatUtils1!=null) { mockUpHeatUtils1.tearDown(); mockUpHeatUtils1 = null; }
		 if (mockUpHeatUtils2!=null) { mockUpHeatUtils2.tearDown(); mockUpHeatUtils2 = null; }
   }
	
    @Test
    public void testQueryCreatedVnf() throws VnfException {
        {
        	mockUpHeatUtils1 = new MockUp<MsoHeatUtils>() {
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
        	mockUpHeatUtils2 = new MockUp<MsoHeatUtils>() {
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
    //    @Ignore // 1802 merge
    public void testQueryVnfWithException() throws VnfException {
        {
	    String propFile = MsoJavaProperties.class.getClassLoader().getResource("mso.properties").getPath();
	    String cloudConfigJsonFilePath = MsoJavaProperties.class.getClassLoader().getResource("cloud_config.json").getPath();

	    MsoPropertiesFactory msoPropFactory = new MsoPropertiesFactory();
	    CloudConfigFactory cloudConfigFact = new CloudConfigFactory();
	    try {
		msoPropFactory.initializeMsoProperties("MSO_PROP_VNF_ADAPTER", propFile);
		cloudConfigFact.initializeCloudConfig(cloudConfigJsonFilePath, 1);
	    } catch (org.openecomp.mso.properties.MsoPropertiesException e) {
		//		System.err.println("!?!?!?!! mso config exception: " + e);
		//		e.printStackTrace();
	    } catch (org.openecomp.mso.openstack.exceptions.MsoCloudIdentityNotFound e) {
		//		System.err.println("!?!?!?!! cloud config exception: " + e);
		//		e.printStackTrace();
	    }

            MsoVnfAdapter vnfAdapter = new MsoVnfAdapterImpl(msoPropFactory, cloudConfigFact);

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
