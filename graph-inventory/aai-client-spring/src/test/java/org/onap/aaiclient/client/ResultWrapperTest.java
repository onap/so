/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright © 2025 Deutsche Telekom AG Intellectual Property. All rights reserved.
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

package org.onap.aaiclient.client;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.aai.domain.yang.GraphNode;
import org.onap.aaiclient.client.api.AAIResultWrapper;
import org.onap.aaiclient.client.api.Relationships;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;

@ExtendWith(MockitoExtension.class)
public class ResultWrapperTest {

    @Mock
    private Relationships relationships;

    @Test
    public void testHasRelationshipsToTrue() {
        Mockito.when(relationships.hasRelationshipsTo(any())).thenReturn(true);

        AAIResultWrapper<GraphNode> wrapper = new AAIResultWrapper<>(null, relationships, null);

        assertTrue(wrapper.hasRelationshipsTo(Types.ACTIVITY));
    }

    @Test
    public void testHasRelationshipsToFalse() {
        Mockito.when(relationships.hasRelationshipsTo(any())).thenReturn(false);

        AAIResultWrapper<GraphNode> wrapper = new AAIResultWrapper<>(null, relationships, null);

        assertFalse(wrapper.hasRelationshipsTo(Types.ACTIVITY));
    }
}
