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

package org.onap.so.client.aai;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.Pserver;

@RunWith(MockitoJUnitRunner.class)
public class AAIValidatorTest {

    @Mock
    protected AAIRestClientI client;
    String vnfName = "testVnf";
    String uuid = "UUID";
    AAIValidatorImpl validator;

    @Before
    public void init() {
        validator = new AAIValidatorImpl();
        validator.setClient(client);
    }

    public List<Pserver> getPserversLocked() {
        Pserver pserver1 = new Pserver();
        pserver1.setInMaint(true);
        Pserver pserver2 = new Pserver();
        pserver2.setInMaint(false);
        List<Pserver> pservers = new ArrayList<Pserver>();
        pservers.add(pserver1);
        pservers.add(pserver2);
        return pservers;
    }

    public List<Pserver> getPserversNotLocked() {
        Pserver pserver1 = new Pserver();
        pserver1.setInMaint(false);
        Pserver pserver2 = new Pserver();
        pserver2.setInMaint(false);
        List<Pserver> pservers = new ArrayList<Pserver>();
        pservers.add(pserver1);
        pservers.add(pserver2);
        return pservers;
    }

    public GenericVnf createGenericVnfs(boolean locked) {
        GenericVnf genericVnf = new GenericVnf();
        genericVnf.setInMaint(locked);
        return genericVnf;
    }

    @Test
    public void test_IsPhysicalServerLocked_True() throws IOException {
        when(client.getPhysicalServerByVnfId(vnfName)).thenReturn(getPserversLocked());
        boolean locked = validator.isPhysicalServerLocked(vnfName);
        assertEquals(true, locked);
    }

    @Test
    public void test_IsPhysicalServerLocked_False() throws IOException {
        when(client.getPhysicalServerByVnfId(vnfName)).thenReturn(getPserversNotLocked());
        boolean locked = validator.isPhysicalServerLocked(vnfName);
        assertEquals(false, locked);
    }

    @Test
    public void test_IsPhysicalServerLocked_NoServers_False() throws IOException {
        when(client.getPhysicalServerByVnfId(vnfName)).thenReturn(null);
        boolean locked = validator.isPhysicalServerLocked(vnfName);
        assertEquals(false, locked);
    }

    @Test
    public void test_IsVNFLocked_False() {
        when(client.getVnfByName(vnfName)).thenReturn(createGenericVnfs(false));
        boolean locked = validator.isVNFLocked(vnfName);
        assertEquals(false, locked);
    }

    @Test
    public void test_IsVNFLocked_True() {
        when(client.getVnfByName(vnfName)).thenReturn(createGenericVnfs(true));
        boolean locked = validator.isVNFLocked(vnfName);
        assertEquals(true, locked);
    }
}
