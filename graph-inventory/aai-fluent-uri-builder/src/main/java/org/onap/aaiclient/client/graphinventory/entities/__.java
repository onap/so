/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.aaiclient.client.graphinventory.entities;

import org.onap.aaiclient.client.graphinventory.GraphInventoryObjectName;

public class __ {

    protected __() {

    }

    public static <A> DSLQueryBuilder<A, A> identity() {
        return new DSLQueryBuilder<>();
    }

    public static <A> DSLQueryBuilder<A, A> start(Start node) {
        return new DSLQueryBuilder<>(node);
    }

    public static DSLQueryBuilder<DSLStartNode, DSLStartNode> node(GraphInventoryObjectName name) {

        return __.<DSLStartNode>start(new DSLStartNode(name));
    }

    public static DSLQueryBuilder<DSLStartNode, DSLStartNode> node(GraphInventoryObjectName name, DSLNodeKey... key) {
        return __.<DSLStartNode>start(new DSLStartNode(name, key));
    }

    public static DSLNodeKey key(String keyName, Object... value) {
        return new DSLNodeKey(keyName, value);
    }

    @SafeVarargs
    public static final <A, B> DSLQueryBuilder<A, B> union(final DSLQueryBuilder<?, B>... traversal) {

        return __.<A>identity().union(traversal);
    }

    public static <A> DSLQueryBuilder<A, A> where(DSLQueryBuilder<A, A> traversal) {

        return __.<A>identity().where(traversal);
    }
}
