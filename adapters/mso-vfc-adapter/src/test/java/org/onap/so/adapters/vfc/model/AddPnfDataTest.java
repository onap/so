/*
 * Copyright (C) 2019 Verizon. All Rights Reserved Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.onap.so.adapters.vfc.model;

import org.junit.Test;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import static org.junit.Assert.*;

public class AddPnfDataTest {
    AddPnfData pnfData = new AddPnfData();

    @Test
    public void getPnfId() {
        pnfData.setPnfId("123");
        String id = pnfData.getPnfId();
        assertEquals(id, "123");
    }


    @Test
    public void setPnfId() {
        pnfData.setPnfId("c9f0a95e-dea0-4698-96e5-5a79bc5a233d");
    }

    @Test
    public void getPnfName() {
        pnfData.setPnfName("Router");
        String name = pnfData.getPnfName();
        assertEquals(name, "Router");
    }

    @Test
    public void setPnfName() {
        pnfData.setPnfName("Router");
    }

    @Test
    public void getPnfdId() {
        pnfData.setPnfdId("123");
        String dId = pnfData.getPnfdId();
        assertEquals(dId, "123");
    }

    @Test
    public void setPnfdId() {
        pnfData.setPnfdId("c9f0a95e-dea0-4698-96e5-5a79bc5a233d");
    }

    @Test
    public void getPnfProfileId() {
        pnfData.setPnfProfileId("abc");
        String pId = pnfData.getPnfProfileId();
        assertEquals(pId, "abc");
    }

    @Test
    public void setPnfProfileId() {
        pnfData.setPnfProfileId("c9f0a95e-dea0-4698-96e5-5a79bc5a233d");
    }

    @Test
    public void getCpData() {
        pnfData.getCpData();
    }

    @Test
    public void setCpData() {
        pnfData.setCpData(new List < PnfExtCpData > () {
            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean contains(Object o) {
                return false;
            }

            @Override
            public Iterator < PnfExtCpData > iterator() {
                return null;
            }

            @Override
            public Object[] toArray() {
                return new Object[0];
            }

            @Override
            public < T > T[] toArray(T[] a) {
                return null;
            }

            @Override
            public boolean add(PnfExtCpData pnfExtCpData) {
                return false;
            }

            @Override
            public boolean remove(Object o) {
                return false;
            }

            @Override
            public boolean containsAll(Collection << ? > c) {
                return false;
            }

            @Override
            public boolean addAll(Collection << ? extends PnfExtCpData > c) {
                return false;
            }

            @Override
            public boolean addAll(int index, Collection << ? extends PnfExtCpData > c) {
                return false;
            }

            @Override
            public boolean removeAll(Collection << ? > c) {
                return false;
            }

            @Override
            public boolean retainAll(Collection << ? > c) {
                return false;
            }

            @Override
            public void clear() {

            }

            @Override
            public PnfExtCpData get(int index) {
                return null;
            }

            @Override
            public PnfExtCpData set(int index, PnfExtCpData element) {
                return null;
            }

            @Override
            public void add(int index, PnfExtCpData element) {

            }

            @Override
            public PnfExtCpData remove(int index) {
                return null;
            }

            @Override
            public int indexOf(Object o) {
                return 0;
            }

            @Override
            public int lastIndexOf(Object o) {
                return 0;
            }

            @Override
            public ListIterator < PnfExtCpData > listIterator() {
                return null;
            }

            @Override
            public ListIterator < PnfExtCpData > listIterator(int index) {
                return null;
            }

            @Override
            public List < PnfExtCpData > subList(int fromIndex, int toIndex) {
                return null;
            }
        });
    }
}