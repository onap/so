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
 */

package org.onap.so.security.cadi.util;

import java.net.URI;

/**
 * URI and URL, if the host does not have "dots", will interpret Host:port as Authority
 *
 * This is very problematic for Containers, which like single name entries.
 * 
 * @author Instrumental(Jonathan)
 *
 */
public class FixURIinfo {
    private String auth;
    private String host;
    private int port;

    public FixURIinfo(URI uri) {
        auth = uri.getAuthority();
        host = uri.getHost();
        if (host == null || (auth != null && auth.startsWith(host))) {
            if (auth != null) {
                int colon = auth.indexOf(':');
                if (colon >= 0) {
                    host = auth.substring(0, colon);
                    port = Integer.parseInt(auth.substring(colon + 1));
                } else {
                    host = auth;
                    port = uri.getPort();
                    if (port < 1) {
                        if ("http".equals(uri.getScheme())) {
                            port = 80;
                        } else if ("https".equals(uri.getScheme())) {
                            port = 443;
                        } else {
                            throw new RuntimeException("Invalid scheme provided for URI " + uri);
                        }
                    }
                }
                auth = null;
            }
        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUserInfo() {
        return auth;
    }
}
