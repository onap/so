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

package org.onap.so.adapters.vevnfm.util;

public final class NullOptional<T> {

    private static final NullOptional<?> EMPTY = new NullOptional<>();

    private final boolean present;
    private final T value;

    private NullOptional() {
        this.present = false;
        this.value = null;
    }

    private NullOptional(final T value) {
        this.present = true;
        this.value = value;
    }

    public static <T> NullOptional<T> empty() {
        return (NullOptional<T>) EMPTY;
    }

    public static <T> NullOptional<T> of(final T value) {
        return new NullOptional<>(value);
    }

    public boolean isPresent() {
        return present;
    }

    public T get() {
        return value;
    }
}
