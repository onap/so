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

import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.regex.Pattern;
import org.onap.so.security.cadi.GetCred;
import org.onap.so.security.cadi.taf.basic.BasicHttpTaf;

public class X509Principal extends BearerPrincipal implements GetCred {
    private static final Pattern pattern = Pattern.compile("[a-zA-Z0-9]*\\@[a-zA-Z0-9.]*");
    private final X509Certificate cert;
    private final String name;
    private byte[] content;
    private BasicHttpTaf bht;

    public X509Principal(String identity, X509Certificate cert) {
        name = identity;
        content = null;
        this.cert = cert;
    }

    public X509Principal(String identity, X509Certificate cert, byte[] content, BasicHttpTaf bht) {
        name = identity;
        this.content = content;
        this.cert = cert;
        this.bht = bht;
    }

    public X509Principal(X509Certificate cert, byte[] content, BasicHttpTaf bht) throws IOException {
        this.content = content;
        this.cert = cert;
        String _name = null;
        String subj = cert.getSubjectDN().getName();
        int cn = subj.indexOf("OU=");
        if (cn >= 0) {
            cn += 3;
            int space = subj.indexOf(',', cn);
            if (space >= 0) {
                String id = subj.substring(cn, space);
                if (pattern.matcher(id).matches()) {
                    _name = id;
                }
            }
        }
        if (_name == null) {
            throw new IOException("X509 does not have Identity as CN");
        }
        name = _name;
        this.bht = bht;
    }

    public String getAsHeader() throws IOException {
        try {
            if (content == null) {
                content = cert.getEncoded();
            }
        } catch (CertificateEncodingException e) {
            throw new IOException(e);
        }
        return "X509 " + content;
    }

    public String toString() {
        return "X509 Authentication for " + name;
    }


    public byte[] getCred() {
        try {
            return content == null ? (content = cert.getEncoded()) : content;
        } catch (CertificateEncodingException e) {
            return null;
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public String tag() {
        return "x509";
    }

    public BasicHttpTaf getBasicHttpTaf() {
        return bht;
    }

}
