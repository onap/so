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

public final class StringUsage {

    private static final StringUsage EMPTY = new StringUsage();

    private final String value;
    private final boolean present;

    private StringUsage() {
        this.value = null;
        this.present = false;
    }

    private StringUsage(final String value) {
        this.value = value;
        this.present = true;
    }

    public static StringUsage empty() {
        return EMPTY;
    }

    public static StringUsage of(final String value) {
        return new StringUsage(value);
    }

    public boolean isPresent() {
        return present;
    }

    public String get() {
        return value;
    }
}
