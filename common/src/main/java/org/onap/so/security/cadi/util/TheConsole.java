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

public class TheConsole implements MyConsole {
    @Override
    public String readLine(String fmt, Object... args) {
        String rv = System.console().readLine(fmt, args);
        if (args.length > 0 && args[0] != null && rv.length() == 0) {
            rv = args[0].toString();
        }
        return rv;
    }

    @Override
    public char[] readPassword(String fmt, Object... args) {
        return System.console().readPassword(fmt, args);
    }

    public static boolean implemented() {
        return System.console() != null;
    }

    @Override
    public void printf(String fmt, Object... args) {
        System.console().printf(fmt, args);
    }
}
