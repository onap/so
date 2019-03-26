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

public class SapDataTest {

    SapData sapData = new SapData();

    @Test
    public void getSapdId() {
        sapData.getSapdId();
    }

    @Test
    public void setSapdId() {
        sapData.setSapdId("c9f0a95e-dea0-4698-96e5-5a79bc5a233d");
    }

    @Test
    public void getSapName() {
        sapData.getSapName();
    }

    @Test
    public void setSapName() {
        sapData.setSapName("Dummy SapName");
    }

    @Test
    public void getDescription() {
        sapData.getDescription();
    }

    @Test
    public void setDescription() {
        sapData.setDescription("Dummy Description");
    }

    @Test
    public void getSapProtocolData() {
        sapData.getSapProtocolData();
    }

    @Test
    public void setSapProtocolData() {
        sapData.setSapProtocolData(new List<CpProtocolData>() {
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
            public Iterator<CpProtocolData> iterator() {
                return null;
            }

            @Override
            public Object[] toArray() {
                return new Object[0];
            }

            @Override
            public <T> T[] toArray(T[] a) {
                return null;
            }

            @Override
            public boolean add(CpProtocolData cpProtocolData) {
                return false;
            }

            @Override
            public boolean remove(Object o) {
                return false;
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                return false;
            }

            @Override
            public boolean addAll(Collection<? extends CpProtocolData> c) {
                return false;
            }

            @Override
            public boolean addAll(int index, Collection<? extends CpProtocolData> c) {
                return false;
            }

            @Override
            public boolean removeAll(Collection<?> c) {
                return false;
            }

            @Override
            public boolean retainAll(Collection<?> c) {
                return false;
            }

            @Override
            public void clear() {

            }

            @Override
            public CpProtocolData get(int index) {
                return null;
            }

            @Override
            public CpProtocolData set(int index, CpProtocolData element) {
                return null;
            }

            @Override
            public void add(int index, CpProtocolData element) {

            }

            @Override
            public CpProtocolData remove(int index) {
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
            public ListIterator<CpProtocolData> listIterator() {
                return null;
            }

            @Override
            public ListIterator<CpProtocolData> listIterator(int index) {
                return null;
            }

            @Override
            public List<CpProtocolData> subList(int fromIndex, int toIndex) {
                return null;
            }
        });
    }
}
