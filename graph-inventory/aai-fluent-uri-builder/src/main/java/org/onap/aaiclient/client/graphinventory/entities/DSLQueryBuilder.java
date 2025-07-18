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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.onap.aaiclient.client.graphinventory.GraphInventoryObjectName;
import com.google.common.base.Joiner;

public class DSLQueryBuilder<S, E> {

    private List<QueryStep> steps = new ArrayList<>();
    private String suffix = "";

    protected DSLQueryBuilder() {

    }

    protected DSLQueryBuilder(QueryStep node) {
        steps.add(node);
    }

    public <T> DSLQueryBuilder<S, DSLNodeBase<?>> node(DSLNodeBase<?> node) {
        steps.add(node);

        return (DSLQueryBuilder<S, DSLNodeBase<?>>) this;
    }

    public DSLQueryBuilder<S, Node> output() {
        callOnLambda(item -> item.output());
        return (DSLQueryBuilder<S, Node>) this;
    }

    public DSLQueryBuilder<S, Node> output(String... fields) {
        callOnLambda(item -> item.output(fields));
        return (DSLQueryBuilder<S, Node>) this;
    }

    protected void callOnLambda(Consumer<DSLNodeBase> consumer) {

        Object obj = steps.get(steps.size() - 1);
        if (obj instanceof DSLNodeBase) {
            consumer.accept((DSLNodeBase) steps.get(steps.size() - 1));
        } else if (obj.getClass().getName().contains("$$Lambda$")) {
            // process lambda expressions
            for (Field f : obj.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                Object o;
                try {
                    o = f.get(obj);
                    if (o instanceof DSLQueryBuilder && ((DSLQueryBuilder) o).steps.get(0) instanceof DSLNodeBase) {
                        consumer.accept(((DSLNodeBase) ((DSLQueryBuilder) o).steps.get(0)));
                    }
                } catch (IllegalArgumentException | IllegalAccessException e) {
                }
                f.setAccessible(false);
                break;
            }
        }
    }

    @SafeVarargs
    public final <E2> DSLQueryBuilder<S, E2> union(final DSLQueryBuilder<?, E2>... union) {

        List<DSLQueryBuilder<?, ?>> unions = Arrays.asList(union);
        steps.add(() -> {
            StringBuilder query = new StringBuilder();

            query.append("> [ ")
                    .append(Joiner.on(", ")
                            .join(unions.stream().map(item -> item.compile()).collect(Collectors.toList())))
                    .append(" ]");
            return query.toString();
        });

        return (DSLQueryBuilder<S, E2>) this;
    }

    public DSLQueryBuilder<S, E> where(DSLQueryBuilder<?, ?> where) {

        steps.add(() -> {
            StringBuilder query = new StringBuilder();
            query.append(where.compile()).append(")");
            String result = query.toString();
            if (!result.startsWith(">")) {
                result = "> " + result;
            }
            return "(" + result;
        });
        return this;
    }

    public <E2> DSLQueryBuilder<S, E2> to(DSLQueryBuilder<?, E2> to) {
        steps.add(() -> {
            StringBuilder query = new StringBuilder();

            query.append("> ").append(to.compile());
            return query.toString();
        });
        return (DSLQueryBuilder<S, E2>) this;
    }

    public DSLQueryBuilder<S, E> to(GraphInventoryObjectName name) {
        return (DSLQueryBuilder<S, E>) to(__.node(name));
    }

    public DSLQueryBuilder<S, E> to(GraphInventoryObjectName name, DSLNodeKey... key) {
        return (DSLQueryBuilder<S, E>) to(__.node(name, key));
    }

    public DSLQueryBuilder<S, E> limit(int limit) {
        suffix = " LIMIT " + limit;
        return this;
    }

    public DSLTraversal<E> build() {
        return new DSLTraversal<>(compile());
    }

    @Override
    public String toString() {
        return build().get();
    }

    @Override
    public boolean equals(Object o) {
        if (o != null) {
            return o.toString().equals(toString());
        }
        return false;
    }

    @Override
    public int hashCode() {

        return compile().hashCode();
    }

    private String compile() {
        return String.join(" ", steps.stream().map(item -> item.build()).collect(Collectors.toList())) + suffix;
    }

    protected QueryStep getFirst() {
        return steps.get(0);
    }
}
