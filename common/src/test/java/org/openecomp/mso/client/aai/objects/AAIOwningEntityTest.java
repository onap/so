/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Intellectual Property. All rights reserved.
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


package org.openecomp.mso.client.aai.objects;

import org.junit.Test;

public class AAIOwningEntityTest {

    AAIOwningEntity test = new AAIOwningEntity();

    @Test
    public void getOwningEntityNameTest() throws Exception {
        test.getOwningEntityName();
    }

    @Test
    public void setOwningEntityNameTest() throws Exception {
        test.setOwningEntityName("name");
    }

    @Test
    public void getOwningEntityIdTest() throws Exception {
        test.getOwningEntityId();
    }

    @Test
    public void setOwningEntityIdTest() throws Exception {
        test.setOwningEntityId("id");
    }

    @Test
    public void withOwningEntityTest() throws Exception {
        test.withOwningEntity("name","id");
    }

    @Test
    public void getUriTest() throws Exception {
        test.getUri();
    }

}
