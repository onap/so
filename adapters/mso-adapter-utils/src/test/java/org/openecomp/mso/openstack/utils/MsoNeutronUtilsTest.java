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

package org.openecomp.mso.openstack.utils;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openecomp.mso.cloud.CloudConfig;
import org.openecomp.mso.cloud.CloudConfigFactory;
import org.openecomp.mso.cloud.CloudSite;
import org.openecomp.mso.openstack.beans.NetworkInfo;
import org.openecomp.mso.openstack.exceptions.MsoException;
import org.openecomp.mso.openstack.utils.MsoNeutronUtils.NetworkType;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.woorea.openstack.quantum.Quantum;
import com.woorea.openstack.quantum.model.Network;
import com.woorea.openstack.quantum.model.Segment;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MsoNeutronUtils.class,MsoCommonUtils.class,NetworkInfo.class,CloudConfigFactory.class,CloudConfig.class,Segment.class,Network.class,Quantum.class})
public class MsoNeutronUtilsTest{

@Mock
MsoCommonUtils utils;

@Mock
NetworkInfo net;

@Mock
CloudConfig cloudConfig;

@Mock
Segment segment;

@Mock
CloudConfigFactory cloudConfigFactory;

@Mock
Network network;

@Mock
NetworkInfo ninfo;

@Mock
Quantum neutronClient;

@Mock
CloudSite cloudSite;


	@Test
	public void testcreateNetwork() throws MsoException{
	    List<Integer> vlans=new ArrayList();
	    vlans.add(1);
	    MsoNeutronUtils mnu=PowerMockito.spy(new MsoNeutronUtils("msoProp",cloudConfigFactory));
        NetworkType type=NetworkType.PROVIDER;
        doReturn(ninfo).when(mnu).createNetwork("cloudSiteId", "tenantId", type, "networkName", "provider", vlans);
        assert(mnu.createNetwork("cloudSiteId", "tenantId", type, "networkName", "provider", vlans)!=null);
	    
	}
	@Test
    public void testqueryNetwork() throws MsoException{
	    MsoNeutronUtils mnu=PowerMockito.spy(new MsoNeutronUtils("msoProp",cloudConfigFactory));
	    doReturn(ninfo).when(mnu).queryNetwork("networkNameOrId", "tenantId", "cloudSiteId");
	    assert(mnu.queryNetwork("networkNameOrId", "tenantId", "cloudSiteId")!=null);
	}
	
	@Test
	public void testdeleteNetwork() throws MsoException{
	    MsoNeutronUtils mnu=PowerMockito.spy(new MsoNeutronUtils("msoProp",cloudConfigFactory));
	    doReturn(true).when(mnu).deleteNetwork("networkId", "tenantId", "cloudSiteId");
	    assertTrue(mnu.deleteNetwork("networkId", "tenantId", "cloudSiteId"));
	    
	}
	@Test
	public void testupdateNetwork() throws MsoException{
	    List<Integer> vlans=new ArrayList();
        vlans.add(1);
        NetworkType type=NetworkType.PROVIDER;
        MsoNeutronUtils mnu=PowerMockito.spy(new MsoNeutronUtils("msoProp",cloudConfigFactory));
        doReturn(ninfo).when(mnu).updateNetwork("cloudSiteId", "tenantId", "Nid", type, "provider", vlans);
        assert(mnu.updateNetwork("cloudSiteId", "tenantId", "Nid", type, "provider", vlans)!=null);
	}	
	
	}


