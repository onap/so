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

public class ResourceChangesTest {
    ResourceChanges resourceChanges = new ResourceChanges();

    @Test
    public void getAffectedVnfs() {
        resourceChanges.getAffectedVnfs();
    }

    @Test
    public void setAffectedVnfs() {
        resourceChanges.setAffectedVnfs(new List<AffectedVnf>() {
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
            public Iterator<AffectedVnf> iterator() {
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
            public boolean add(AffectedVnf affectedVnf) {
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
            public boolean addAll(Collection<? extends AffectedVnf> c) {
                return false;
            }

            @Override
            public boolean addAll(int index, Collection<? extends AffectedVnf> c) {
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
            public AffectedVnf get(int index) {
                return null;
            }

            @Override
            public AffectedVnf set(int index, AffectedVnf element) {
                return null;
            }

            @Override
            public void add(int index, AffectedVnf element) {

            }

            @Override
            public AffectedVnf remove(int index) {
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
            public ListIterator<AffectedVnf> listIterator() {
                return null;
            }

            @Override
            public ListIterator<AffectedVnf> listIterator(int index) {
                return null;
            }

            @Override
            public List<AffectedVnf> subList(int fromIndex, int toIndex) {
                return null;
            }
        });
    }

    @Test
    public void getAffectedPnfs() {
        resourceChanges.getAffectedPnfs();
    }

    @Test
    public void setAffectedPnfs() {
        resourceChanges.setAffectedPnfs(new List<AffectedPnf>() {
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
            public Iterator<AffectedPnf> iterator() {
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
            public boolean add(AffectedPnf affectedPnf) {
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
            public boolean addAll(Collection<? extends AffectedPnf> c) {
                return false;
            }

            @Override
            public boolean addAll(int index, Collection<? extends AffectedPnf> c) {
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
            public AffectedPnf get(int index) {
                return null;
            }

            @Override
            public AffectedPnf set(int index, AffectedPnf element) {
                return null;
            }

            @Override
            public void add(int index, AffectedPnf element) {

            }

            @Override
            public AffectedPnf remove(int index) {
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
            public ListIterator<AffectedPnf> listIterator() {
                return null;
            }

            @Override
            public ListIterator<AffectedPnf> listIterator(int index) {
                return null;
            }

            @Override
            public List<AffectedPnf> subList(int fromIndex, int toIndex) {
                return null;
            }
        });
    }

    @Test
    public void getAffectedVls() {
        resourceChanges.getAffectedVls();
    }

    @Test
    public void setAffectedVls() {
        resourceChanges.setAffectedVls(new List<AffectedVirtualLink>() {
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
            public Iterator<AffectedVirtualLink> iterator() {
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
            public boolean add(AffectedVirtualLink affectedVirtualLink) {
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
            public boolean addAll(Collection<? extends AffectedVirtualLink> c) {
                return false;
            }

            @Override
            public boolean addAll(int index, Collection<? extends AffectedVirtualLink> c) {
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
            public AffectedVirtualLink get(int index) {
                return null;
            }

            @Override
            public AffectedVirtualLink set(int index, AffectedVirtualLink element) {
                return null;
            }

            @Override
            public void add(int index, AffectedVirtualLink element) {

            }

            @Override
            public AffectedVirtualLink remove(int index) {
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
            public ListIterator<AffectedVirtualLink> listIterator() {
                return null;
            }

            @Override
            public ListIterator<AffectedVirtualLink> listIterator(int index) {
                return null;
            }

            @Override
            public List<AffectedVirtualLink> subList(int fromIndex, int toIndex) {
                return null;
            }
        });
    }

    @Test
    public void getAffectedVnffgs() {
        resourceChanges.getAffectedVnffgs();
    }

    @Test
    public void setAffectedVnffgs() {
        resourceChanges.setAffectedVnffgs(new List<AffectedVnffg>() {
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
            public Iterator<AffectedVnffg> iterator() {
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
            public boolean add(AffectedVnffg affectedVnffg) {
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
            public boolean addAll(Collection<? extends AffectedVnffg> c) {
                return false;
            }

            @Override
            public boolean addAll(int index, Collection<? extends AffectedVnffg> c) {
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
            public AffectedVnffg get(int index) {
                return null;
            }

            @Override
            public AffectedVnffg set(int index, AffectedVnffg element) {
                return null;
            }

            @Override
            public void add(int index, AffectedVnffg element) {

            }

            @Override
            public AffectedVnffg remove(int index) {
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
            public ListIterator<AffectedVnffg> listIterator() {
                return null;
            }

            @Override
            public ListIterator<AffectedVnffg> listIterator(int index) {
                return null;
            }

            @Override
            public List<AffectedVnffg> subList(int fromIndex, int toIndex) {
                return null;
            }
        });
    }

    @Test
    public void getAffectedNss() {
        resourceChanges.getAffectedNss();
    }

    @Test
    public void setAffectedNss() {
        resourceChanges.setAffectedNss(new List<AffectedNs>() {
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
            public Iterator<AffectedNs> iterator() {
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
            public boolean add(AffectedNs affectedNs) {
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
            public boolean addAll(Collection<? extends AffectedNs> c) {
                return false;
            }

            @Override
            public boolean addAll(int index, Collection<? extends AffectedNs> c) {
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
            public AffectedNs get(int index) {
                return null;
            }

            @Override
            public AffectedNs set(int index, AffectedNs element) {
                return null;
            }

            @Override
            public void add(int index, AffectedNs element) {

            }

            @Override
            public AffectedNs remove(int index) {
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
            public ListIterator<AffectedNs> listIterator() {
                return null;
            }

            @Override
            public ListIterator<AffectedNs> listIterator(int index) {
                return null;
            }

            @Override
            public List<AffectedNs> subList(int fromIndex, int toIndex) {
                return null;
            }
        });
    }

    @Test
    public void getAffectedSaps() {
        resourceChanges.getAffectedSaps();
    }

    @Test
    public void setAffectedSaps() {
        resourceChanges.setAffectedSaps(new List<AffectedSap>() {
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
            public Iterator<AffectedSap> iterator() {
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
            public boolean add(AffectedSap affectedSap) {
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
            public boolean addAll(Collection<? extends AffectedSap> c) {
                return false;
            }

            @Override
            public boolean addAll(int index, Collection<? extends AffectedSap> c) {
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
            public AffectedSap get(int index) {
                return null;
            }

            @Override
            public AffectedSap set(int index, AffectedSap element) {
                return null;
            }

            @Override
            public void add(int index, AffectedSap element) {

            }

            @Override
            public AffectedSap remove(int index) {
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
            public ListIterator<AffectedSap> listIterator() {
                return null;
            }

            @Override
            public ListIterator<AffectedSap> listIterator(int index) {
                return null;
            }

            @Override
            public List<AffectedSap> subList(int fromIndex, int toIndex) {
                return null;
            }
        });
    }
}
