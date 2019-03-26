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

public class IpAddressesTest {
    IpAddresses ipAddresses = new IpAddresses();

    @Test
    public void getType() {
        ipAddresses.getType();
    }

    @Test
    public void setType() {
        ipAddresses.setType("Dummy Type");
    }

    @Test
    public void getFixedAddresses() {
        ipAddresses.getFixedAddresses();
    }

    @Test
    public void setFixedAddresses() {
        ipAddresses.setFixedAddresses(new List<String>() {
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
            public Iterator<String> iterator() {
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
            public boolean add(String s) {
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
            public boolean addAll(Collection<? extends String> c) {
                return false;
            }

            @Override
            public boolean addAll(int index, Collection<? extends String> c) {
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
            public String get(int index) {
                return null;
            }

            @Override
            public String set(int index, String element) {
                return null;
            }

            @Override
            public void add(int index, String element) {

            }

            @Override
            public String remove(int index) {
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
            public ListIterator<String> listIterator() {
                return null;
            }

            @Override
            public ListIterator<String> listIterator(int index) {
                return null;
            }

            @Override
            public List<String> subList(int fromIndex, int toIndex) {
                return null;
            }
        });
    }

    @Test
    public void getNumDynamicAddresses() {
        ipAddresses.getNumDynamicAddresses();
    }

    @Test
    public void setNumDynamicAddresses() {
        ipAddresses.setNumDynamicAddresses(5);
    }

    @Test
    public void getAddressRange() {
        ipAddresses.getAddressRange();
    }

    @Test
    public void setAddressRange() {
        ipAddresses.setAddressRange(new AddressRange());
    }

    @Test
    public void getSubnetId() {
        ipAddresses.getSubnetId();
    }

    @Test
    public void setSubnetId() {
        ipAddresses.setSubnetId("c9f0a95e-dea0-4698-96e5-5a79bc5a233d");
    }
}
