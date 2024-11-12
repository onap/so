/**
 * ============LICENSE_START==================================================== org.onap.so
 * =========================================================================== Copyright (c) 2018 AT&T Intellectual
 * Property. All rights reserved. =========================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ============LICENSE_END====================================================
 *
 */

package org.onap.so.security.cadi.util;

/**
 * Split by Char, optional Trim
 *
 * Note: Copied from Inno to avoid linking issues. Note: I read the String split and Pattern split code, and we can do
 * this more efficiently for a single Character
 *
 * 8/20/2015
 */

public class Split {
    private static final String[] EMPTY = new String[0];

    public static String[] split(char c, String value) {
        if (value == null) {
            return EMPTY;
        }

        return split(c, value, 0, value.length());
    }

    public static String[] split(char c, String value, int start, int end) {
        if (value == null) {
            return EMPTY;
        }

        // Count items to preallocate Array (memory alloc is more expensive than counting twice)
        int count, idx;
        for (count = 1, idx = value.indexOf(c, start); idx >= 0 && idx < end; idx = value.indexOf(c, ++idx), ++count);
        String[] rv = new String[count];
        if (count == 1) {
            rv[0] = value.substring(start, end);
        } else {
            int last = 0;
            count = -1;
            for (idx = value.indexOf(c, start); idx >= 0 && idx < end; idx = value.indexOf(c, idx)) {
                rv[++count] = value.substring(last, idx);
                last = ++idx;
            }
            rv[++count] = value.substring(last, end);
        }
        return rv;
    }

    public static String[] splitTrim(char c, String value, int start, int end) {
        if (value == null) {
            return EMPTY;
        }

        // Count items to preallocate Array (memory alloc is more expensive than counting twice)
        int count, idx;
        for (count = 1, idx = value.indexOf(c, start); idx >= 0 && idx < end; idx = value.indexOf(c, ++idx), ++count);
        String[] rv = new String[count];
        if (count == 1) {
            rv[0] = value.substring(start, end).trim();
        } else {
            int last = start;
            count = -1;
            for (idx = value.indexOf(c, start); idx >= 0 && idx < end; idx = value.indexOf(c, idx)) {
                rv[++count] = value.substring(last, idx).trim();
                last = ++idx;
            }
            rv[++count] = value.substring(last, end).trim();
        }
        return rv;
    }

    public static String[] splitTrim(char c, String value) {
        if (value == null) {
            return EMPTY;
        }
        return splitTrim(c, value, 0, value.length());
    }

    public static String[] splitTrim(char c, String value, int size) {
        if (value == null) {
            return EMPTY;
        }

        int idx;
        String[] rv = new String[size];
        if (size == 1) {
            rv[0] = value.trim();
        } else {
            int last = 0;
            int count = -1;
            size -= 2;
            for (idx = value.indexOf(c); idx >= 0 && count < size; idx = value.indexOf(c, idx)) {
                rv[++count] = value.substring(last, idx).trim();
                last = ++idx;
            }
            if (idx > 0) {
                rv[++count] = value.substring(last, idx).trim();
            } else {
                rv[++count] = value.substring(last).trim();
            }
        }
        return rv;
    }

}
