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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.onap.aaiclient.client.graphinventory.GraphInventoryObjectName;

public abstract class DSLNodeBase<T extends DSLNodeBase<?>> implements QueryStep {

    protected final String nodeName;
    protected final Collection<String> fields;
    protected final List<DSLNodeKey> nodeKeys;
    protected final StringBuilder query;
    protected boolean output = false;

    public DSLNodeBase() {
        this.nodeName = "";
        this.nodeKeys = new ArrayList<>();
        this.query = new StringBuilder();
        this.fields = new LinkedHashSet<>();

    }

    public DSLNodeBase(GraphInventoryObjectName name) {
        this.nodeName = name.typeName();
        this.nodeKeys = new ArrayList<>();
        this.query = new StringBuilder();
        this.fields = new LinkedHashSet<>();
        query.append(nodeName);
    }

    public DSLNodeBase(GraphInventoryObjectName name, DSLNodeKey... key) {
        this.nodeName = name.typeName();
        this.nodeKeys = Arrays.asList(key);
        this.query = new StringBuilder();
        this.fields = new LinkedHashSet<>();
        query.append(nodeName);
    }

    public DSLNodeBase(DSLNodeBase<?> copy) {
        this.nodeName = copy.nodeName;
        this.nodeKeys = copy.nodeKeys;
        this.query = new StringBuilder(copy.query);
        this.fields = copy.fields;
        this.output = copy.output;
    }

    public DSLOutputNode output() {
        this.output = true;

        return new DSLOutputNode(this);
    }

    public DSLOutputNode output(String... fields) {
        this.output = true;
        this.fields.addAll(Arrays.asList(fields));
        return new DSLOutputNode(this);
    }

    public T and(DSLNodeKey... key) {
        this.nodeKeys.addAll(Arrays.asList(key));

        return (T) this;
    }

    @Override
    public String build() {
        StringBuilder result = new StringBuilder(query);
        if (output) {
            if (fields.isEmpty()) {
                result.append("*");
            } else {
                String items =
                        fields.stream().map(item -> String.format("'%s'", item)).collect(Collectors.joining(", "));
                result.append("{").append(items).append("}");
            }
        }
        for (DSLNodeKey key : nodeKeys) {
            result.append(key.build());
        }

        return result.toString();
    }
}
