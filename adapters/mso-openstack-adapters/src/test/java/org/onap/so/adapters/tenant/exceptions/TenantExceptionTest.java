/*
 * ============LICENSE_START======================================================= ONAP : SO
 * ================================================================================ Copyright (C) 2018 TechMahindra
 * ================================================================================ Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.adapters.tenant.exceptions;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.onap.so.openstack.exceptions.MsoExceptionCategory;
import static org.junit.Assert.*;

public class TenantExceptionTest {

    @Mock
    private TenantExceptionBean teb;

    @Mock
    private MsoExceptionCategory mec;

    @InjectMocks
    private TenantException te;

    @Test
    public void test() {
        teb = new TenantExceptionBean("message");
        teb.setMessage("message");
        teb.setCategory(MsoExceptionCategory.INTERNAL);
        te = new TenantException("message", mec);
        te.setFaultInfo(teb);
        assert (te.getFaultInfo() != null);
        assert (te.getFaultInfo().equals(teb));
        assertNotNull(teb);
    }
}
