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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openecomp.mso.cloud.CloudIdentity;
import org.openecomp.mso.cloud.CloudSite;
import org.openecomp.mso.openstack.beans.MsoTenant;
import org.openecomp.mso.openstack.exceptions.MsoException;
import org.openecomp.mso.properties.MsoJavaProperties;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.woorea.openstack.keystone.Keystone;
import com.woorea.openstack.keystone.model.Tenant;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MsoKeystoneUtils.class,CloudSite.class,CloudIdentity.class,Tenant.class,Keystone.class,MsoTenant.class,MsoJavaProperties.class})
public class MsoKeystoneUtilsTest {
    
    @Mock
    Tenant tenant;

    @Mock
    Keystone adminClient;
    
    @Mock
    MsoTenant mst;
    
    @Mock
    CloudSite cs;
    
    @Mock
    CloudIdentity cloudIdentity;
    
    @Mock
    MsoJavaProperties msoProps;

    @Test
     public  void testcreateTenant() throws MsoException{
        MsoKeystoneUtils msk = PowerMockito.spy(new MsoKeystoneUtils("ID"));
        Map<String,String>metadata=new HashMap<>();
        metadata.put("1", "value");
        PowerMockito.when(tenant.getId ()).thenReturn("ID");
       doReturn(tenant.getId ()).when(msk).createTenant("tenantName", "cloudSiteId", metadata, true);
       PowerMockito.spy(tenant.getId ());
       String Id = msk.createTenant("tenantName", "cloudSiteId", metadata, true);
       Assert.assertEquals(tenant.getId (), Id);
       assert(msk.createTenant("tenantName", "cloudSiteId", metadata, true)!=null);
    }
    @Test
    public  void testdeleteTenant() throws MsoException{
        MsoKeystoneUtils msk = PowerMockito.spy(new MsoKeystoneUtils("ID"));
        doReturn(true).when(msk).deleteTenant("tenantId", "cloudSiteId");
       assertTrue(msk.deleteTenant("tenantId", "cloudSiteId"));
    }
    @Test
    public  void testfindTenantByName() throws Exception{
        MsoKeystoneUtils msk = PowerMockito.spy(new MsoKeystoneUtils("ID"));
       doReturn(null).when(msk).findTenantByName(adminClient, "tenantName");
       assertNull(msk.findTenantByName(adminClient, "tenantName"));
    }
    @Test
    public  void testqueryTenant() throws MsoException{
        MsoKeystoneUtils msk = PowerMockito.spy(new MsoKeystoneUtils("ID"));
        Map<String,String>metadata=new HashMap<>();
        metadata.put("1", "value");  
       PowerMockito.when(mst.getTenantId()).thenReturn("tenantId");
       PowerMockito.when(mst.getMetadata()).thenReturn(metadata);
       PowerMockito.when(mst.getTenantName()).thenReturn("name");
       doReturn(mst).when(msk).queryTenant ("tenantId", "cloudSiteId");
        assertNotNull(msk.queryTenant("tenantId", "cloudSiteId"));       
    }
        
    @Test
    public  void testqueryTenantByName()throws MsoException {
        MsoKeystoneUtils msk = PowerMockito.spy(new MsoKeystoneUtils("ID"));
        Map<String,String>metadata=new HashMap<>();
        metadata.put("1", "value");  
        PowerMockito.when(mst.getTenantId()).thenReturn("tenantId");
        PowerMockito.when(mst.getMetadata()).thenReturn(metadata);
        PowerMockito.when(mst.getTenantName()).thenReturn("name");
        doReturn(mst).when(msk).queryTenantByName ("tenantId", "cloudSiteId");
        assertNotNull(msk.queryTenantByName("tenantId", "cloudSiteId"));   
        
    }
    
    @Test
    public void testgetKeystoneAdminClient() throws MsoException{
        Keystone keystone = new Keystone (cloudIdentity.getKeystoneUrl ("region", "msoPropID"));
        MsoKeystoneUtils msk = PowerMockito.spy(new MsoKeystoneUtils("ID"));
        doReturn(keystone).when(msk).getKeystoneAdminClient(cs);
        assertNotNull(msk.getKeystoneAdminClient(cs));
    }
    
    }
