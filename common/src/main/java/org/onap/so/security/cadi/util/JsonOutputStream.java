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

import java.io.IOException;
import java.io.OutputStream;

public class JsonOutputStream extends OutputStream {
    private static final byte[] TWO_SPACE = "  ".getBytes();
    private OutputStream os;
    private boolean closeable;
    private int indent = 0;
    private int prev, ret = 0;

    public JsonOutputStream(OutputStream os) {
        // Don't close these, or dire consequences.
        closeable = !os.equals(System.out) && !os.equals(System.err);
        this.os = os;
    }

    @Override
    public void write(int b) throws IOException {
        if (ret == '\n') {
            ret = 0;
            if (prev != ',' || (b != '{' && b != '[')) {
                os.write('\n');
                for (int i = 0; i < indent; ++i) {
                    os.write(TWO_SPACE);
                }
            }
        }
        switch (b) {
            case '{':
            case '[':
                ret = '\n';
                ++indent;
                break;
            case '}':
            case ']':
                --indent;
                os.write('\n');
                for (int i = 0; i < indent; ++i) {
                    os.write(TWO_SPACE);
                }
                break;
            case ',':
                ret = '\n';
                break;

        }
        os.write(b);
        prev = b;
    }

    public void resetIndent() {
        indent = 1;
    }

    @Override
    public void flush() throws IOException {
        os.flush();
    }

    @Override
    public void close() throws IOException {
        if (closeable) {
            os.close();
        }
    }

}
