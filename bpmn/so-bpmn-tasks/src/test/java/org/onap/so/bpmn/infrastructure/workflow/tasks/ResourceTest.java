/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2021 Bell Canada. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.workflow.tasks;

import static org.junit.Assert.assertEquals;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

public class ResourceTest {

    @Test
    public void testBaseFirstComparator() {
        Resource r1 = new Resource(null, "1", false);
        Resource r2 = new Resource(null, "2", false);
        r2.setBaseVfModule(true);

        List<Resource> sorted =
                Arrays.asList(r1, r2).stream().sorted(Resource.sortByPriorityAsc).collect(Collectors.toList());

        assertEquals("2", sorted.get(0).getResourceId());
    }

    @Test
    public void testPriorityAscComparator() {
        Resource r1 = new Resource(null, "1", false);
        Resource r2 = new Resource(null, "2", false);
        Resource r3 = new Resource(null, "3", false);
        Resource r4 = new Resource(null, "4", false);
        r1.setProcessingPriority(4);
        r2.setBaseVfModule(true);
        r3.setProcessingPriority(2);
        r4.setProcessingPriority(1);

        List<Resource> sorted =
                Arrays.asList(r1, r2, r3, r4).stream().sorted(Resource.sortByPriorityAsc).collect(Collectors.toList());

        assertEquals("2", sorted.get(0).getResourceId());
        assertEquals("4", sorted.get(1).getResourceId());
        assertEquals("3", sorted.get(2).getResourceId());
    }

    @Test
    public void testBaseLastComparator() {
        Resource r1 = new Resource(null, "1", false);
        Resource r2 = new Resource(null, "2", false);
        r1.setBaseVfModule(true);

        List<Resource> sorted =
                Arrays.asList(r1, r2).stream().sorted(Resource.sortByPriorityDesc).collect(Collectors.toList());

        assertEquals("1", sorted.get(1).getResourceId());
    }

    @Test
    public void testPriorityDescComparator() {
        Resource r1 = new Resource(null, "1", false);
        Resource r2 = new Resource(null, "2", false);
        Resource r3 = new Resource(null, "3", false);
        Resource r4 = new Resource(null, "4", false);
        r1.setProcessingPriority(4);
        r2.setBaseVfModule(true);
        r3.setProcessingPriority(2);
        r4.setProcessingPriority(1);

        List<Resource> sorted =
                Arrays.asList(r1, r2, r3, r4).stream().sorted(Resource.sortByPriorityDesc).collect(Collectors.toList());

        assertEquals("1", sorted.get(0).getResourceId());
        assertEquals("3", sorted.get(1).getResourceId());
        assertEquals("4", sorted.get(2).getResourceId());
    }

    @Test
    public void testPriorityReplaceBase() {
        Resource r1 = new Resource(null, "1", false);
        Resource r2 = new Resource(null, "2", false);
        Resource r3 = new Resource(null, "3", false);
        Resource r4 = new Resource(null, "4", false);
        r1.setProcessingPriority(4);
        r2.setBaseVfModule(true);
        r2.setProcessingPriority(6);
        r3.setProcessingPriority(2);
        r4.setProcessingPriority(1);

        List<Resource> sorted =
                Arrays.asList(r1, r2, r3, r4).stream().sorted(Resource.sortByPriorityAsc).collect(Collectors.toList());

        assertEquals("4", sorted.get(0).getResourceId());
        assertEquals("3", sorted.get(1).getResourceId());
        assertEquals("1", sorted.get(2).getResourceId());
    }

}
