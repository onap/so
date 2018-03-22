/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.adapters.vnf;

import org.junit.Test;
import org.openecomp.mso.adapters.vnf.exceptions.VnfException;
import org.openecomp.mso.entity.MsoRequest;
import org.openecomp.mso.openstack.beans.VnfRollback;
import org.openecomp.mso.properties.MsoPropertiesException;

import javax.xml.ws.Holder;
import java.util.HashMap;
import java.util.Map;

public class MsoVnfCloudifyAdapterImplTest {

	@Test(expected = NullPointerException.class)
    public void queryVnfNullPointerExceptionTest() throws Exception {
        MsoVnfCloudifyAdapterImpl instance = new MsoVnfCloudifyAdapterImpl();
        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId("12345");
        msoRequest.setServiceInstanceId("12345");

        instance.queryVnf("siteid", "1234", "vfname",
                msoRequest, new Holder<>(), new Holder<>(), new Holder<>(),
                new Holder<>());
    }

	@Test(expected = VnfException.class)
    public void deleteVnfVnfExceptionTest() throws Exception {
        MsoVnfCloudifyAdapterImpl instance = new MsoVnfCloudifyAdapterImpl();
        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId("12345");
        msoRequest.setServiceInstanceId("12345");

        instance.deleteVnf("12344", "234", "vnfname", msoRequest);

    }

	@Test
	public void rollbackVnf() throws Exception {
		MsoVnfCloudifyAdapterImpl instance = new MsoVnfCloudifyAdapterImpl();
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");

        VnfRollback vnfRollback = new VnfRollback();
        vnfRollback.setModelCustomizationUuid("1234");
        vnfRollback.setVfModuleStackId("2134");
        vnfRollback.setVnfId("123");
        vnfRollback.setModelCustomizationUuid("1234");

        instance.rollbackVnf(vnfRollback);
	}

	@Test(expected = VnfException.class)
	public void createVfModuleVnfException() throws Exception {
		MsoVnfCloudifyAdapterImpl instance = new MsoVnfCloudifyAdapterImpl();
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");

		instance.createVfModule("123", "123", "vf", "v1", "module-005", "create", "3245", "234", "123", new HashMap<>(), true, true, msoRequest, new Holder<>(), new Holder<>(), new Holder<>());
	}

	@Test(expected = VnfException.class)
	public void updateVfModuleVnfException() throws Exception {
		MsoVnfCloudifyAdapterImpl instance = new MsoVnfCloudifyAdapterImpl();
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");

		instance.updateVfModule("123", "1234", "fw", "v2", "vnf1", "create", "123", "12", "233", "234", new HashMap<>(), msoRequest, new Holder<>(), new Holder<>());
	}

	@Test
	public void healthCheckVNFTest() {
		MsoVnfCloudifyAdapterImpl instance = new MsoVnfCloudifyAdapterImpl();
		instance.healthCheck();
	}

	@Test
	public void createVnfTest() {
		MsoVnfCloudifyAdapterImpl instance = new MsoVnfCloudifyAdapterImpl();
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");

		Map<String, String> map = new HashMap<>();
		map.put("key1", "value1");
		try {
			instance.createVnf("mdt1", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "vSAMP12", "VFMOD",
					"volumeGroupHeatStackId|1", map,
					Boolean.FALSE, Boolean.TRUE, msoRequest, new Holder<>(), new Holder<>(),
                new Holder<>());
		} catch (Exception e) {
		}
	}

	@Test
	public void updateVnfTest() {
		MsoVnfCloudifyAdapterImpl instance = new MsoVnfCloudifyAdapterImpl();
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");

		Map<String, String> map = new HashMap<>();
		
		map.put("key1", "value1");
		try {
			instance.updateVnf("mdt1", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "vSAMP12", "VFMOD",
					"volumeGroupHeatStackId|1",  map, msoRequest, new Holder<>(),
                new Holder<>());
		} catch (Exception e) {

		}
	}

	@Test
	public void deleteVnfTest() {
		MsoVnfCloudifyAdapterImpl instance = new MsoVnfCloudifyAdapterImpl();
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");
		try {
			instance.deleteVnf("mdt1", "88a6ca3ee0394ade9403f075db23167e", "vSAMP12", msoRequest);
		} catch (Exception e) {

		}
	}

}
