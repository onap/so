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

import java.io.File;
import java.io.IOException;

public interface Chmod {
    public void chmod(File f) throws IOException;

    public static final Chmod to755 = new Chmod() {
        public void chmod(File f) throws IOException {
            f.setExecutable(true, false);
            f.setExecutable(true, true);
            f.setReadable(true, false);
            f.setReadable(true, true);
            f.setWritable(false, false);
            f.setWritable(true, true);
        }
    };

    public static final Chmod to644 = new Chmod() {
        public void chmod(File f) throws IOException {
            f.setExecutable(false, false);
            f.setExecutable(false, true);
            f.setReadable(true, false);
            f.setReadable(true, true);
            f.setWritable(false, false);
            f.setWritable(true, true);
        }
    };

    public static final Chmod to400 = new Chmod() {
        public void chmod(File f) throws IOException {
            f.setExecutable(false, false);
            f.setExecutable(false, true);
            f.setReadable(false, false);
            f.setReadable(true, true);
            f.setWritable(false, false);
            f.setWritable(false, true);
        }
    };
}
