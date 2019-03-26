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

public class IpOverEthernetAddressDataTest {
    IpOverEthernetAddressData ipOverEthernetAddressData = new IpOverEthernetAddressData();

    @Test
    public void getMacAddress() {
        ipOverEthernetAddressData.getMacAddress();
    }

    @Test
    public void setMacAddress() {
        ipOverEthernetAddressData.setMacAddress("4e:86:9f:62:c1:bf");
    }

    @Test
    public void getIpAddresses() {
        ipOverEthernetAddressData.getIpAddresses();
    }

    @Test
    public void setIpAddresses() {
        ipOverEthernetAddressData.setIpAddresses(new List<IpAddresses>() {
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
            public Iterator<IpAddresses> iterator() {
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
            public boolean add(IpAddresses ipAddresses) {
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
            public boolean addAll(Collection<? extends IpAddresses> c) {
                return false;
            }

            @Override
            public boolean addAll(int index, Collection<? extends IpAddresses> c) {
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
            public IpAddresses get(int index) {
                return null;
            }

            @Override
            public IpAddresses set(int index, IpAddresses element) {
                return null;
            }

            @Override
            public void add(int index, IpAddresses element) {

            }

            @Override
            public IpAddresses remove(int index) {
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
            public ListIterator<IpAddresses> listIterator() {
                return null;
            }

            @Override
            public ListIterator<IpAddresses> listIterator(int index) {
                return null;
            }

            @Override
            public List<IpAddresses> subList(int fromIndex, int toIndex) {
                return null;
            }
        });
    }
}
