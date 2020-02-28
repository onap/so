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

public class InstantiateNsRequestTest {
    InstantiateNsRequest instantiateNsRequest = new InstantiateNsRequest();

    @Test
    public void getNsFlavourId() {
        instantiateNsRequest.getNsFlavourId();
    }

    @Test
    public void setNsFlavourId() {
        instantiateNsRequest.setNsFlavourId("c9f0a95e-dea0-4698-96e5-5a79bc5a233d");
    }

    @Test
    public void getSapData() {
        instantiateNsRequest.getSapData();
    }

    @Test
    public void setSapData() {
        instantiateNsRequest.setSapData(new List<SapData>() {
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
            public Iterator<SapData> iterator() {
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
            public boolean add(SapData sapData) {
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
            public boolean addAll(Collection<? extends SapData> c) {
                return false;
            }

            @Override
            public boolean addAll(int index, Collection<? extends SapData> c) {
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
            public SapData get(int index) {
                return null;
            }

            @Override
            public SapData set(int index, SapData element) {
                return null;
            }

            @Override
            public void add(int index, SapData element) {

            }

            @Override
            public SapData remove(int index) {
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
            public ListIterator<SapData> listIterator() {
                return null;
            }

            @Override
            public ListIterator<SapData> listIterator(int index) {
                return null;
            }

            @Override
            public List<SapData> subList(int fromIndex, int toIndex) {
                return null;
            }
        });
    }

    @Test
    public void getAddpnfData() {
        instantiateNsRequest.getAddpnfData();
    }

    @Test
    public void setAddpnfData() {
        instantiateNsRequest.setAddpnfData(new List<AddPnfData>() {
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
            public Iterator<AddPnfData> iterator() {
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
            public boolean add(AddPnfData addPnfData) {
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
            public boolean addAll(Collection<? extends AddPnfData> c) {
                return false;
            }

            @Override
            public boolean addAll(int index, Collection<? extends AddPnfData> c) {
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
            public AddPnfData get(int index) {
                return null;
            }

            @Override
            public AddPnfData set(int index, AddPnfData element) {
                return null;
            }

            @Override
            public void add(int index, AddPnfData element) {

            }

            @Override
            public AddPnfData remove(int index) {
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
            public ListIterator<AddPnfData> listIterator() {
                return null;
            }

            @Override
            public ListIterator<AddPnfData> listIterator(int index) {
                return null;
            }

            @Override
            public List<AddPnfData> subList(int fromIndex, int toIndex) {
                return null;
            }
        });
    }

    @Test
    public void getVnfInstanceData() {
        instantiateNsRequest.getVnfInstanceData();
    }

    @Test
    public void setVnfInstanceData() {
        instantiateNsRequest.setVnfInstanceData(new List<VnfInstanceData>() {
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
            public Iterator<VnfInstanceData> iterator() {
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
            public boolean add(VnfInstanceData vnfInstanceData) {
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
            public boolean addAll(Collection<? extends VnfInstanceData> c) {
                return false;
            }

            @Override
            public boolean addAll(int index, Collection<? extends VnfInstanceData> c) {
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
            public VnfInstanceData get(int index) {
                return null;
            }

            @Override
            public VnfInstanceData set(int index, VnfInstanceData element) {
                return null;
            }

            @Override
            public void add(int index, VnfInstanceData element) {

            }

            @Override
            public VnfInstanceData remove(int index) {
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
            public ListIterator<VnfInstanceData> listIterator() {
                return null;
            }

            @Override
            public ListIterator<VnfInstanceData> listIterator(int index) {
                return null;
            }

            @Override
            public List<VnfInstanceData> subList(int fromIndex, int toIndex) {
                return null;
            }
        });
    }

    @Test
    public void getNestedNsInstanceId() {
        instantiateNsRequest.getNestedNsInstanceId();
    }
}
