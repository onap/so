/**
 * ============LICENSE_START==================================================== Log
 * =========================================================================== Copyright (c) May 11, 2020 Gathman
 * Systems. All rights reserved. =========================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ============LICENSE_END====================================================
 */
package org.onap.so.security.cadi.util;

/**
 * A basic log interface used to Facade into Log Libraries used locally.
 * 
 * @author Jonathan
 *
 */
public interface Log {
    enum Type {
        debug, info, warn, error, trace
    };

    public void log(Log.Type type, Object... o);

    public final static Log NULL = new Log() {
        @Override
        public void log(Log.Type type, Object... o) {}
    };
}
