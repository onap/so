/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.aaiclient.client.aai.entities;

import java.io.Serializable;
import org.onap.aaiclient.client.graphinventory.GraphInventoryResultWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AAIResultWrapper extends GraphInventoryResultWrapper<Relationships> implements Serializable {

    private static final long serialVersionUID = 5895841925807816737L;
    private final static transient Logger logger = LoggerFactory.getLogger(AAIResultWrapper.class);

    public AAIResultWrapper(String json) {
        super(json, logger);
    }

    public AAIResultWrapper(Object aaiObject) {
        super(aaiObject, logger);
    }

    @Override
    protected Relationships createRelationships(String json) {
        return new Relationships(json);
    }
}
