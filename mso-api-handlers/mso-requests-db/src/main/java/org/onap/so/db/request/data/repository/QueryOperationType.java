/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
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
 * 
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.db.request.data.repository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * @author waqas.ikram@ericsson.com
 */
public enum QueryOperationType {

    EQ {
        @Override
        public <T> Predicate getPredicate(final CriteriaBuilder criteriaBuilder, final Root<T> tableRoot,
                final String key, final String value) {
            return criteriaBuilder.equal(tableRoot.get(key), value);
        }

    },
    NEQ {

        @Override
        public <T> Predicate getPredicate(final CriteriaBuilder criteriaBuilder, final Root<T> tableRoot,
                final String key, final String value) {
            return criteriaBuilder.notEqual(tableRoot.get(key), value);
        }

    },
    LIKE {

        @Override
        public <T> Predicate getPredicate(final CriteriaBuilder criteriaBuilder, final Root<T> tableRoot,
                final String key, final String value) {
            return criteriaBuilder.like(tableRoot.get(key), "%" + value + "%");
        }
    };

    public static QueryOperationType getQueryOperationType(final String type) {
        for (final QueryOperationType queryOperationType : QueryOperationType.values()) {
            if (queryOperationType.name().equalsIgnoreCase(type)) {
                return queryOperationType;
            }
        }
        return QueryOperationType.EQ;
    }

    public abstract <T> Predicate getPredicate(final CriteriaBuilder criteriaBuilder, final Root<T> tableRoot,
            final String key, final String value);

}
