/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.servicedecomposition;

import jakarta.persistence.Id;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface ShallowCopy<T> {
    default T shallowCopyId() {
        try {
            T obj = (T) this.getClass().newInstance();
            for (Field field : this.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class)) {
                    String fieldName = Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
                    Method setter = this.getClass().getMethod("set" + fieldName, field.getType());
                    Method getter = this.getClass().getMethod("get" + fieldName, null);
                    setter.invoke(obj, getter.invoke(this, null));
                }
            }
            return obj;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
