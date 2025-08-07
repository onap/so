/*
 * ============LICENSE_START======================================================= ONAP : SO
 * ================================================================================ Copyright (C) 2018 AT&T Intellectual
 * Property. All rights reserved. ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.so.bpmn.core.domain;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class CompareModelsResultTest {

    private CompareModelsResult modelsResult;
    private List<ResourceModelInfo> addedResourceList;
    private List<ResourceModelInfo> deletedResourceList;
    private ResourceModelInfo resourceModelInfo1;
    private ResourceModelInfo resourceModelInfo2;
    private List<String> requestInputs;

    @Before
    public void before() {
        resourceModelInfo1 = new ResourceModelInfo();
        resourceModelInfo1.setResourceCustomizationUuid("f1d563e8-e714-4393-8f99-cc480144a05e");
        resourceModelInfo1.setResourceInvariantUuid("e1d563e8-e714-4393-8f99-cc480144a05f");
        resourceModelInfo1.setResourceName("resourceName1");
        resourceModelInfo1.setResourceUuid("f1d563e8-e714-4393-8f99-cc480144a05g");
        resourceModelInfo2 = new ResourceModelInfo();
        resourceModelInfo2.setResourceCustomizationUuid("a1d563e8-e714-4393-8f99-cc480144a05d");
        resourceModelInfo2.setResourceInvariantUuid("b1d563e8-e714-4393-8f99-cc480144a05e");
        resourceModelInfo2.setResourceName("resourceName2");
        resourceModelInfo2.setResourceUuid("c1d563e8-e714-4393-8f99-cc480144a05f");
    }

    @Test
    public void testSetAddedResourceList() {
        addedResourceList = new ArrayList<ResourceModelInfo>();
        addedResourceList.add(resourceModelInfo1);
        addedResourceList.add(resourceModelInfo2);
        modelsResult = new CompareModelsResult();
        modelsResult.setAddedResourceList(addedResourceList);
        assertEquals(addedResourceList, modelsResult.getAddedResourceList());
    }

    @Test
    public void testSetDeletedResourceList() {
        deletedResourceList = new ArrayList<ResourceModelInfo>();
        deletedResourceList.add(resourceModelInfo1);
        deletedResourceList.add(resourceModelInfo2);
        modelsResult = new CompareModelsResult();
        modelsResult.setDeletedResourceList(deletedResourceList);
        assertEquals(deletedResourceList, modelsResult.getDeletedResourceList());
    }

    @Test
    public void testSetRequestInputs() {
        requestInputs = new ArrayList<String>();
        requestInputs.add("requestInput1");
        requestInputs.add("requestInput2");
        modelsResult = new CompareModelsResult();
        modelsResult.setRequestInputs(requestInputs);
        assertEquals(requestInputs, modelsResult.getRequestInputs());
    }

}
