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

package org.onap.so.listener;

import java.lang.annotation.Annotation;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import jakarta.annotation.Priority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public abstract class ListenerRunner {

    @Autowired
    protected ApplicationContext context;

    public <T> List<T> filterListeners(List<T> validators, Predicate<T> predicate) {
        return validators.stream().filter(item -> {
            return !item.getClass().isAnnotationPresent(Skip.class) && predicate.test(item);
        }).sorted(Comparator.comparing(item -> {
            Priority p = Optional.ofNullable(item.getClass().getAnnotation(Priority.class)).orElse(new Priority() {
                public int value() {
                    return 1000;
                }

                @Override
                public Class<? extends Annotation> annotationType() {
                    return Priority.class;
                }
            });
            return p.value();
        })).collect(Collectors.toList());
    }

}
