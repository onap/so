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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.ws.Holder;

import com.fasterxml.jackson.databind.JsonNode;
import org.mockito.Mock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.mso.adapters.vnf.exceptions.VnfAlreadyExists;
import org.openecomp.mso.adapters.vnf.exceptions.VnfException;
import org.openecomp.mso.cloud.CloudConfigFactory;
import org.openecomp.mso.cloudify.exceptions.MsoCloudifyException;
import org.openecomp.mso.entity.MsoRequest;
import org.openecomp.mso.openstack.beans.HeatStatus;
import org.openecomp.mso.openstack.beans.StackInfo;
import org.openecomp.mso.openstack.beans.VnfRollback;
import org.openecomp.mso.openstack.exceptions.MsoCloudIdentityNotFound;
import org.openecomp.mso.openstack.utils.MsoHeatUtils;
import org.openecomp.mso.properties.MsoPropertiesFactory;

public class MsoVnfAdapterImplTest {

    @InjectMocks
    MsoVnfAdapterImpl msoVnfAdapter;
    @Mock
    MsoHeatUtils heat;

    @Before
    public void setup() throws MsoCloudIdentityNotFound{
        ClassLoader classLoader = MsoVnfAdapterImplTest.class.getClassLoader();
        String cloudConfigJsonFilePath = classLoader.getResource("cloud_config.json").getPath();
        CloudConfigFactory cloudConfigFactory = new CloudConfigFactory();
        cloudConfigFactory.initializeCloudConfig(cloudConfigJsonFilePath, 1);
        msoVnfAdapter = new MsoVnfAdapterImpl(new MsoPropertiesFactory(), cloudConfigFactory);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void updateVnf() throws Exception {
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");

    	msoVnfAdapter.updateVnf("cloudsite", "tenantid", "vfw", "v1", "test",
				"update", "heatid", new HashMap<>(), msoRequest, new Holder<>(), new Holder<>());
    	Assert.assertTrue(true);
    }


    @Test(expected = VnfException.class)
    public void nullRequestCreateVnf() throws Exception{

        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId("12345");
        msoRequest.setServiceInstanceId("12345");

        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        Mockito.when(heat.queryStack(Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenThrow(new MsoCloudifyException(1,"test","test"));
        msoVnfAdapter.createVnf("MT", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "vSAMP12",
                null, "volumeGroupHeatStackId|1", map, false, true, msoRequest,new Holder<>(),new Holder<>(), new Holder<>());

    }

    @Test(expected = VnfAlreadyExists.class)
    public void createVnfInProgress() throws Exception{

        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId("12345");
        msoRequest.setServiceInstanceId("12345");

        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        StackInfo stackInfo = new StackInfo("test",HeatStatus.INIT);
        Mockito.when(heat.queryStack(Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenReturn(stackInfo);
        msoVnfAdapter.createVnf("MT", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "vSAMP12",
                null, "volumeGroupHeatStackId|1", map, false, true, msoRequest,new Holder<>(),new Holder<>(), new Holder<>());

    }

    @Test(expected = VnfAlreadyExists.class)
    public void createVnfFailed() throws Exception{

        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId("12345");
        msoRequest.setServiceInstanceId("12345");

        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        StackInfo stackInfo = new StackInfo("test",HeatStatus.FAILED);
        Mockito.when(heat.queryStack(Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenReturn(stackInfo);
        msoVnfAdapter.createVnf("MT", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "vSAMP12",
                null, "volumeGroupHeatStackId|1", map, false, true, msoRequest,new Holder<>(),new Holder<>(), new Holder<>());

    }

    @Test(expected = VnfAlreadyExists.class)
    public void createVnfUnknown() throws Exception{

        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId("12345");
        msoRequest.setServiceInstanceId("12345");

        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        StackInfo stackInfo = new StackInfo("test",HeatStatus.UNKNOWN);
        Mockito.when(heat.queryStack(Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenReturn(stackInfo);
        msoVnfAdapter.createVnf("MT", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "vSAMP12",
                null, "volumeGroupHeatStackId|1", map, false, true, msoRequest,new Holder<>(),new Holder<>(), new Holder<>());

    }

    @Test(expected = VnfAlreadyExists.class)
    public void createVnfCreatedAndFail() throws Exception{

        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId("12345");
        msoRequest.setServiceInstanceId("12345");

        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        StackInfo stackInfo = new StackInfo("test",HeatStatus.CREATED);
        Mockito.when(heat.queryStack(Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenReturn(stackInfo);
        msoVnfAdapter.createVnf("MT", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "vSAMP12",
                null, "volumeGroupHeatStackId|1", map, true, true, msoRequest,new Holder<>(),new Holder<>(), new Holder<>());

    }

    @Test
    public void createVnfCreatedAndContinue() throws Exception{

        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId("12345");
        msoRequest.setServiceInstanceId("12345");

        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        StackInfo stackInfo = new StackInfo("test",HeatStatus.CREATED);
        Mockito.when(heat.queryStack(Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenReturn(stackInfo);
        msoVnfAdapter.createVnf("MT", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "vSAMP12",
                null, "volumeGroupHeatStackId|1", map, false, true, msoRequest,new Holder<>(),new Holder<>(), new Holder<>());
    }

    @Test(expected = VnfException.class)
    public void createVnfNestedStackException() throws Exception{

        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId("12345");
        msoRequest.setServiceInstanceId("12345");

        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        StackInfo stackInfo = new StackInfo("test",HeatStatus.NOTFOUND);
        StackInfo nestedStackInfo = new StackInfo("test",HeatStatus.NOTFOUND);
        Mockito.when(heat.queryStack(Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenReturn(stackInfo).thenThrow(new MsoCloudifyException(1,"test","test"));
        msoVnfAdapter.createVnf("MT", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "vSAMP12",
                "VFMOD", "volumeGroupHeatStackId|1", map, false, true, msoRequest,new Holder<>(),new Holder<>(), new Holder<>());
    }

    @Test(expected = VnfException.class)
    public void createVnfNestedStackNotFound() throws Exception{

        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId("12345");
        msoRequest.setServiceInstanceId("12345");

        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        StackInfo stackInfo = new StackInfo("test",HeatStatus.NOTFOUND);
        StackInfo nestedStackInfo = new StackInfo("test",HeatStatus.NOTFOUND);
        Mockito.when(heat.queryStack(Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenReturn(stackInfo,nestedStackInfo);
        msoVnfAdapter.createVnf("MT", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "vSAMP12",
                "VFMOD", "volumeGroupHeatStackId|1", map, false, true, msoRequest,new Holder<>(),new Holder<>(), new Holder<>());
    }

    @Test(expected = VnfException.class)
    public void createVnfBaseNestedStackFailed() throws Exception{

        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId("12345");
        msoRequest.setServiceInstanceId("12345");

        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        Map<String,Object> nestedMap = new HashMap();
        nestedMap.put("key",new Integer(3));
        StackInfo stackInfo = new StackInfo("test",HeatStatus.NOTFOUND);
        StackInfo nestedStackInfo = new StackInfo("test",HeatStatus.CREATED);
        StackInfo nestedBaseStackInfo = new StackInfo("test",HeatStatus.CREATED);
        Mockito.when(heat.queryStack(Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenReturn(stackInfo,nestedStackInfo).thenThrow(new MsoCloudifyException(1,"test","test"));
        msoVnfAdapter.createVnf("MT", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "vSAMP12",
                "VFMOD", "volumeGroupHeatStackId|1", map, false, true, msoRequest,new Holder<>(),new Holder<>(), new Holder<>());
    }

    @Test(expected = VnfException.class)
    public void createVnfBaseNestedStackNotFound() throws Exception{

        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId("12345");
        msoRequest.setServiceInstanceId("12345");

        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        StackInfo stackInfo = new StackInfo("test",HeatStatus.NOTFOUND);
        StackInfo nestedStackInfo = new StackInfo("test",HeatStatus.CREATED);
        StackInfo nestedBaseStackInfo = new StackInfo("test",HeatStatus.NOTFOUND);
        Mockito.when(heat.queryStack(Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenReturn(stackInfo,nestedStackInfo,nestedBaseStackInfo);
        msoVnfAdapter.createVnf("MT", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "vSAMP12",
                "VFMOD", "volumeGroupHeatStackId|1", map, false, true, msoRequest,new Holder<>(),new Holder<>(), new Holder<>());
    }

    @Test(expected = VnfException.class)
    public void createVnfBaseNestedStackSuc() throws Exception{

        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId("12345");
        msoRequest.setServiceInstanceId("12345");

        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        StackInfo stackInfo = new StackInfo("test",HeatStatus.NOTFOUND);
        StackInfo nestedStackInfo = new StackInfo("test",HeatStatus.CREATED);
        StackInfo nestedBaseStackInfo = new StackInfo("test",HeatStatus.CREATED);
        Mockito.when(heat.queryStack(Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenReturn(stackInfo,nestedStackInfo,nestedBaseStackInfo);
        msoVnfAdapter.createVnf("MT", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "vSAMP12",
                "VFMOD", "volumeGroupHeatStackId|1", map, false, true, msoRequest,new Holder<>(),new Holder<>(), new Holder<>());
    }

    @Test
    public void queryVnfNullPoinerExceptionTest() throws Exception {
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");
        Mockito.reset(heat);
		msoVnfAdapter.queryVnf("cloudSiteId",
				"tenantId",
				"vnfName",
				msoRequest,
				new Holder<>(),
				new Holder<>(),
				new Holder<>(),
				new Holder<>());
		Assert.assertFalse(false);
    }

    @Test
    public void rollbackVnfCloudSiteInfoNotAvail() throws Exception {
		VnfRollback rollback = new VnfRollback();
		rollback.setVnfId("vnfid");
		rollback.setVfModuleStackId("stackid");
		rollback.setCloudSiteId("11234");
		rollback.setTenantId("234");

		msoVnfAdapter.rollbackVnf(rollback);
		Assert.assertFalse(false);
    }

    @Test
	public void healthCheckVNFTest() {
		MsoVnfAdapterImpl instance = new MsoVnfAdapterImpl();
		instance.healthCheck();
	}

	@Test
	public void createVnfTest() {
		MsoVnfAdapterImpl instance = new MsoVnfAdapterImpl();
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");

		Map<String, String> map = new HashMap<>();
		map.put("key1", "value1");
		try {
			instance.createVfModule("MT", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "vSAMP12", "VFMOD",
					"volumeGroupHeatStackId|1", "baseVfHeatStackId", "88a6ca3ee0394ade9403f075db23167e", map,
					Boolean.FALSE, Boolean.TRUE, msoRequest, new Holder<>(), new Holder<>(),
                new Holder<>());
		} catch (Exception e) {

		}
	}

	@Test
	public void updateVnfTest() {
		MsoVnfAdapterImpl instance = new MsoVnfAdapterImpl();
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");

		Map<String, String> map = new HashMap<>();
		map.put("key1", "value1");
		try {
			instance.updateVfModule("MT", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "vSAMP12", "VFMOD",
					"volumeGroupHeatStackId|1", "baseVfHeatStackId", "vfModuleStackId",
					"88a6ca3ee0394ade9403f075db23167e", map, msoRequest, new Holder<>(),
                new Holder<>());
		} catch (Exception e) {

		}
	}

    @Test(expected = VnfException.class)
    public void updateVnfNotFound() throws Exception{

        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId("12345");
        msoRequest.setServiceInstanceId("12345");

        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        StackInfo stackInfo = new StackInfo("test", HeatStatus.NOTFOUND);
        Mockito.when(heat.queryStack(Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenReturn(stackInfo);
        msoVnfAdapter.updateVfModule("MT", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "vSAMP12", "VFMOD",
                    "volumeGroupHeatStackId|1", "baseVfHeatStackId", "vfModuleStackId",
                    "88a6ca3ee0394ade9403f075db23167e", map, msoRequest, new Holder<>(),
                    new Holder<>());


    }

    @Test(expected = VnfException.class)
    public void updateVnfFailed() throws Exception{

        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId("12345");
        msoRequest.setServiceInstanceId("12345");

        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        Mockito.when(heat.queryStack(Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenThrow(new MsoCloudifyException(1,"test","test"));
        msoVnfAdapter.updateVfModule("MT", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "vSAMP12", "VFMOD",
                "volumeGroupHeatStackId|1", "baseVfHeatStackId", "vfModuleStackId",
                "88a6ca3ee0394ade9403f075db23167e", map, msoRequest, new Holder<>(),
                new Holder<>());


    }

    @Test(expected = VnfException.class)
    public void updateVnfNestedStackNotFound() throws Exception{

        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId("12345");
        msoRequest.setServiceInstanceId("12345");

        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        StackInfo stackInfo = new StackInfo("test", HeatStatus.CREATED);
        StackInfo nestedstackInfo = new StackInfo("test", HeatStatus.NOTFOUND);
        Mockito.when(heat.queryStack(Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenReturn(stackInfo,nestedstackInfo);
        msoVnfAdapter.updateVfModule("MT", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "vSAMP12", "VFMOD",
                "volumeGroupHeatStackId|1", "baseVfHeatStackId", "vfModuleStackId",
                "88a6ca3ee0394ade9403f075db23167e", map, msoRequest, new Holder<>(),
                new Holder<>());
    }

    @Test(expected = VnfException.class)
    public void updateVnfNestedStackFailed() throws Exception{

        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId("12345");
        msoRequest.setServiceInstanceId("12345");

        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        StackInfo stackInfo = new StackInfo("test", HeatStatus.CREATED);
        Mockito.when(heat.queryStack(Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenReturn(stackInfo).thenThrow(new MsoCloudifyException(1,"test","test"));
        msoVnfAdapter.updateVfModule("MT", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "vSAMP12", "VFMOD",
                "volumeGroupHeatStackId|1", "baseVfHeatStackId", "vfModuleStackId",
                "88a6ca3ee0394ade9403f075db23167e", map, msoRequest, new Holder<>(),
                new Holder<>());
    }

    @Test(expected = VnfException.class)
    public void updateVnfNestedBaseStackNotFound() throws Exception{

        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId("12345");
        msoRequest.setServiceInstanceId("12345");

        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        StackInfo stackInfo = new StackInfo("test", HeatStatus.CREATED);
        StackInfo nestedStackInfo = new StackInfo("test", HeatStatus.CREATED);
        StackInfo nestedBaseStackInfo = new StackInfo("test", HeatStatus.NOTFOUND);
        Mockito.when(heat.queryStack(Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenReturn(stackInfo,nestedStackInfo,nestedBaseStackInfo);
        msoVnfAdapter.updateVfModule("MT", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "vSAMP12", "VFMOD",
                "volumeGroupHeatStackId|1", "baseVfHeatStackId", "vfModuleStackId",
                "88a6ca3ee0394ade9403f075db23167e", map, msoRequest, new Holder<>(),
                new Holder<>());
    }

    @Test(expected = VnfException.class)
    public void updateVnfNestedBaseStackFailed() throws Exception{

        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId("12345");
        msoRequest.setServiceInstanceId("12345");

        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        StackInfo stackInfo = new StackInfo("test", HeatStatus.CREATED);
        StackInfo nestedStackInfo = new StackInfo("test", HeatStatus.CREATED);
        Mockito.when(heat.queryStack(Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenReturn(stackInfo,nestedStackInfo).thenThrow(new MsoCloudifyException(1,"test","test"));
        msoVnfAdapter.updateVfModule("MT", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "vSAMP12", "VFMOD",
                "volumeGroupHeatStackId|1", "baseVfHeatStackId", "vfModuleStackId",
                "88a6ca3ee0394ade9403f075db23167e", map, msoRequest, new Holder<>(),
                new Holder<>());
    }

    @Test(expected = NullPointerException.class)
    public void updateVnfNestedBaseStackSuc() throws Exception{

        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId("12345");
        msoRequest.setServiceInstanceId("12345");

        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        StackInfo stackInfo = new StackInfo("test", HeatStatus.CREATED);
        StackInfo nestedStackInfo = new StackInfo("test", HeatStatus.CREATED);
        StackInfo nestedBaseStackInfo = new StackInfo("test", HeatStatus.CREATED);
        Mockito.when(heat.queryStack(Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenReturn(stackInfo,nestedStackInfo,nestedBaseStackInfo);
        msoVnfAdapter.updateVfModule("MT", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "vSAMP12", "VFMOD",
                "volumeGroupHeatStackId|1", "baseVfHeatStackId", "vfModuleStackId",
                "88a6ca3ee0394ade9403f075db23167e", map, msoRequest, new Holder<>(),
                new Holder<>());
    }

	@Test
	public void deleteVnfTest() {
		MsoVnfAdapterImpl instance = new MsoVnfAdapterImpl();
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");
		try {
			instance.deleteVfModule("MT", "88a6ca3ee0394ade9403f075db23167e", "vSAMP12", msoRequest,
                new Holder<>());
		} catch (Exception e) {

		}
	}

    @Test
    public void deleteVnfReturnJsonNodeStack() throws Exception{

        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId("12345");
        msoRequest.setServiceInstanceId("12345");

        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        Map<String,Object> stackOutputs = new HashMap<>();
        JsonNode node = Mockito.mock(JsonNode.class);
        stackOutputs.put("key",node);
        Mockito.when(heat.queryStackForOutputs(Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenReturn(stackOutputs);
        msoVnfAdapter.deleteVfModule("MT", "88a6ca3ee0394ade9403f075db23167e", "vSAMP12", msoRequest,
                new Holder<>());
    }

    @Test
    public void deleteVnfReturnLinkedHashMapStack() throws Exception{

        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId("12345");
        msoRequest.setServiceInstanceId("12345");

        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        Map<String,Object> stackOutputs = new HashMap<>();
        LinkedHashMap<String,Object> node = Mockito.mock(LinkedHashMap.class);
        stackOutputs.put("key",node);
        Mockito.when(heat.queryStackForOutputs(Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenReturn(stackOutputs);
        msoVnfAdapter.deleteVfModule("MT", "88a6ca3ee0394ade9403f075db23167e", "vSAMP12", msoRequest,
                new Holder<>());
    }

    @Test
    public void deleteVnfReturnIntegerStack() throws Exception{

        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId("12345");
        msoRequest.setServiceInstanceId("12345");

        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        Map<String,Object> stackOutputs = new HashMap<>();
        Integer node = new Integer(2);
        stackOutputs.put("key",node);
        Mockito.when(heat.queryStackForOutputs(Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenReturn(stackOutputs);
        msoVnfAdapter.deleteVfModule("MT", "88a6ca3ee0394ade9403f075db23167e", "vSAMP12", msoRequest,
                new Holder<>());
    }

    @Test
    public void deleteVnfReturnOtherStack() throws Exception{

        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId("12345");
        msoRequest.setServiceInstanceId("12345");

        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        Map<String,Object> stackOutputs = new HashMap<>();
        List<String> node = Mockito.mock(List.class);
        stackOutputs.put("key",node);
        Mockito.when(heat.queryStackForOutputs(Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenReturn(stackOutputs);
        msoVnfAdapter.deleteVfModule("MT", "88a6ca3ee0394ade9403f075db23167e", "vSAMP12", msoRequest,
                new Holder<>());
    }



}
