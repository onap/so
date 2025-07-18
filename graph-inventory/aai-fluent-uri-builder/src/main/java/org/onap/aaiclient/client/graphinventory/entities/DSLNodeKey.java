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
import java.util.List;
import com.google.common.base.Joiner;


public class DSLNodeKey implements QueryStep {

    private boolean not = false;
    private final StringBuilder query = new StringBuilder();
    private final String keyName;
    private final List<Object> values;

    public DSLNodeKey(String keyName, Object... value) {

        this.keyName = keyName;
        this.values = Arrays.asList(value);
    }

    public DSLNodeKey not() {

        this.not = true;
        return this;
    }

    @Override
    public String build() {
        StringBuilder result = new StringBuilder(query);

        if (not) {
            result.append(" !");
        }
        result.append("('").append(keyName).append("', ");
        List<Object> temp = new ArrayList<>();
        for (Object item : values) {
            if ("null".equals(item)) {
                temp.add(String.format("' %s '", item));
            } else if ("".equals(item)) {
                temp.add("' '");
            } else {
                if (item instanceof String) {
                    temp.add(String.format("'%s'", item));
                } else {
                    temp.add(item);
                }
            }
        }
        result.append(Joiner.on(", ").join(temp)).append(")");

        return result.toString();
    }
}
