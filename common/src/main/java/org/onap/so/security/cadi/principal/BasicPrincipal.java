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

package org.onap.so.security.cadi.principal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import org.onap.so.security.cadi.BasicCred;
import org.onap.so.security.cadi.GetCred;
import org.onap.so.security.cadi.Symm;

public class BasicPrincipal extends BearerPrincipal implements GetCred {
    private static byte[] basic = "Basic ".getBytes();

    private String name = null;
    private String shortName = null;
    private String domain;
    private byte[] cred = null;
    private long created;


    public BasicPrincipal(String content, String defaultDomain) throws IOException {
        created = System.currentTimeMillis();
        ByteArrayInputStream bis = new ByteArrayInputStream(content.getBytes());
        // Read past "Basic ", ensuring it starts with it.
        for (int i = 0; i < basic.length; ++i) {
            if (bis.read() != basic[i]) {
                name = content;
                cred = null;
                return;
            }
        }
        BasicOS bos = new BasicOS(content.length());
        Symm.base64.decode(bis, bos); // note: writes directly to name until ':'
        if (name == null)
            throw new IOException("Invalid Coding");
        else
            cred = bos.toCred();
        int at;
        if ((at = name.indexOf('@')) > 0) {
            domain = name.substring(at + 1);
            shortName = name.substring(0, at);
        } else {
            shortName = name;
            domain = defaultDomain;
            name = name + '@' + defaultDomain;
        }
    }

    public BasicPrincipal(BasicCred bc, String domain) {
        name = bc.getUser();
        cred = bc.getCred();
        this.domain = domain;
    }

    private class BasicOS extends OutputStream {
        private boolean first = true;
        private ByteArrayOutputStream baos;

        public BasicOS(int size) {
            baos = new ByteArrayOutputStream(size);
        }

        @Override
        public void write(int b) throws IOException {
            if (b == ':' && first) {
                first = false;
                name = new String(baos.toByteArray());
                baos.reset(); //
            } else {
                baos.write(b);
            }
        }

        private byte[] toCred() {
            return baos.toByteArray();
        }
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public String getDomain() {
        return domain;
    }

    public byte[] getCred() {
        return cred;
    }

    public long created() {
        return created;
    }

    public String toString() {
        return "Basic Authorization for " + name + " evaluated on " + new Date(created).toString();
    }

    @Override
    public String tag() {
        return "BAth";
    }

    @Override
    public String personalName() {
        return name; // personalName not available with Basic Auth
    }
}
