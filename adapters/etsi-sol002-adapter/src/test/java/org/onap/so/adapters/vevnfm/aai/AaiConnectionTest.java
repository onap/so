/*-
 * ============LICENSE_START=======================================================
 * SO
 * ================================================================================
 * Copyright (C) 2020 Samsung. All rights reserved.
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

package org.onap.so.adapters.vevnfm.aai;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.onap.aai.domain.yang.RelatedToProperty;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aai.domain.yang.RelationshipData;

public class AaiConnectionTest {

    private static final String KEY = "key";
    private static final String VALUE = "value";

    @Test
    public void testRelationshipData() {
        // given
        final Relationship relationship = new Relationship();
        final RelationshipData data = new RelationshipData();
        data.setRelationshipKey(KEY);
        data.setRelationshipValue(VALUE);
        relationship.getRelationshipData().add(data);

        // when
        final String value = AaiConnection.getRelationshipData(relationship, KEY);

        // then
        assertEquals(VALUE, value);
    }

    @Test
    public void testRelatedToProperty() {
        // given
        final Relationship relationship = new Relationship();
        final RelatedToProperty property = new RelatedToProperty();
        property.setPropertyKey(KEY);
        property.setPropertyValue(VALUE);
        relationship.getRelatedToProperty().add(property);

        // when
        final String value = AaiConnection.getRelatedToProperty(relationship, KEY);

        // then
        assertEquals(VALUE, value);
    }
}
