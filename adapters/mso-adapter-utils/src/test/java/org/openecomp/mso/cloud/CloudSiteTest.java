/*
* ============LICENSE_START=======================================================
* ONAP : APPC
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

package org.openecomp.mso.cloud;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CloudSiteTest {

    @Mock
    CloudIdentity ci= new CloudIdentity();
    
    @InjectMocks
    CloudSite cs = new CloudSite();

    
@Before
public void init(){
    MockitoAnnotations.initMocks(this);
 }
    
    @Test
    public void testCloudSite() {
        cs.setAic_version("aic_version");
        cs.setClli("clli");
        cs.setId("id");
        cs.setIdentityService(ci);
        cs.setRegionId("regionId");
        assert(cs.getAic_version().equals("aic_version"));
        assert(cs.getClli().equals("clli"));
        assert(cs.getId().equals("id"));
        assert(cs.getIdentityService().equals(ci));
        assert(cs.getRegionId().equals("regionId"));
        
    }
    
    @Test
    public void testtoStringmethod(){
        assert(cs.toString()!=null);
    }
    
    @Test
    public void testhashCodemethod(){
        assert(cs.hashCode()!=0);
    }
    
    @Test
    public void testclone(){
    assert(cs.clone()!=null);
    }
    
    @Test
    public void testEquals(){
        Object a = null, b = null;
       assert(cs.cmp(a,b)!=false);
       assertEquals(a, b);
    }

}
