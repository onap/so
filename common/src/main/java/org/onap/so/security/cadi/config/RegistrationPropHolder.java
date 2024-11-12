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

package org.onap.so.security.cadi.config;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import org.onap.so.security.cadi.Access;
import org.onap.so.security.cadi.Access.Level;
import org.onap.so.security.cadi.CadiException;
import org.onap.so.security.cadi.util.Split;

public class RegistrationPropHolder {
    private final String PUBLIC_NAME = "%NS.%N";
    private final String REGI = "RegistrationProperty: %s='%s'";
    private final Access access;
    public String hostname;
    private int port;
    public String public_fqdn;
    private Integer public_port;
    public Float latitude;
    public Float longitude;
    public final String default_fqdn;
    public final String default_container_ns;
    public final String default_name;
    public final String lentries;
    public final String lcontainer;
    public final String default_container;
    private static boolean firstlog = true;

    public RegistrationPropHolder(final Access access, final int port) throws UnknownHostException, CadiException {
        this.access = access;
        StringBuilder errs = new StringBuilder();
        String str;
        this.port = port;

        lentries = access.getProperty(Config.AAF_LOCATOR_ENTRIES, "");
        default_container = access.getProperty(Config.AAF_LOCATOR_CONTAINER, "");
        if (firstlog) {
            access.printf(Level.INIT, REGI, "default_container", default_container);
        }
        if (!default_container.isEmpty()) {
            lcontainer = ',' + default_container; // "" makes a blank default Public Entry
            str = access.getProperty(Config.AAF_LOCATOR_PUBLIC_PORT + '.' + default_container, null);
            if (str == null) {
                str = access.getProperty(Config.AAF_LOCATOR_PUBLIC_PORT, null);
            }
        } else {
            lcontainer = default_container;
            str = access.getProperty(Config.AAF_LOCATOR_PUBLIC_PORT, null);
        }
        if (str != null) {
            public_port = Integer.decode(str);
        }
        if (firstlog) {
            access.printf(Level.INIT, "RegistrationProperty: public_port='%d'", public_port);
        }

        hostname = access.getProperty(Config.HOSTNAME, null);
        if (hostname == null) {
            hostname = Inet4Address.getLocalHost().getHostName();
        }
        if (hostname == null) {
            mustBeDefined(errs, Config.HOSTNAME);
        }
        if (firstlog) {
            access.printf(Level.INIT, REGI, "hostname", hostname);
        }

        public_fqdn = access.getProperty(Config.AAF_LOCATOR_PUBLIC_FQDN, hostname);
        if (firstlog) {
            access.printf(Level.INIT, REGI, "public_fqdn", public_fqdn);
        }

        // Allow Container to reset the standard name for public
        String container_public_name =
                access.getProperty(Config.AAF_LOCATOR_PUBLIC_NAME + '.' + default_container, null);
        if (container_public_name == null) {
            container_public_name = access.getProperty(Config.AAF_LOCATOR_PUBLIC_NAME, null);
            if (container_public_name == null) {
                container_public_name = access.getProperty(Config.AAF_LOCATOR_NAME, PUBLIC_NAME);
            }
        }
        default_name = container_public_name;

        if (firstlog) {
            access.printf(Level.INIT, REGI, "default_name", default_name);
        }

        latitude = null;
        String slatitude = access.getProperty(Config.CADI_LATITUDE, null);
        if (slatitude == null) {
            mustBeDefined(errs, Config.CADI_LATITUDE);
        } else {
            latitude = Float.parseFloat(slatitude);
        }
        if (firstlog) {
            access.printf(Level.INIT, REGI, "latitude", slatitude);
        }

        longitude = null;
        String slongitude = access.getProperty(Config.CADI_LONGITUDE, null);
        if (slongitude == null) {
            mustBeDefined(errs, Config.CADI_LONGITUDE);
        } else {
            longitude = Float.parseFloat(slongitude);
        }
        if (firstlog) {
            access.printf(Level.INIT, REGI, "longitude", slongitude);
        }

        String dot_le;
        // Note: only one of the ports can be public... Therefore, only the last
        for (String le : Split.splitTrim(',', lcontainer)) {
            dot_le = le.isEmpty() ? le : "." + le;
            str = access.getProperty(Config.AAF_LOCATOR_PUBLIC_FQDN + dot_le, null);
            if (str != null && !str.isEmpty()) {
                public_fqdn = str;
                if (firstlog) {
                    access.printf(Level.INIT, "RegistrationProperty: public_hostname(overloaded by %s)='%s'", dot_le,
                            public_fqdn);
                }
            }
        }

        default_fqdn = access.getProperty(Config.AAF_LOCATOR_FQDN, hostname);
        if (firstlog) {
            access.printf(Level.INIT, REGI, "default_fqdn", default_fqdn);
        }
        default_container_ns = access.getProperty(Config.AAF_LOCATOR_CONTAINER_NS, "");
        if (firstlog) {
            access.printf(Level.INIT, REGI, "default_container_ns", default_container_ns);
        }
        if (errs.length() > 0) {
            throw new CadiException(errs.toString());
        }
        firstlog = false;
    }

    private void mustBeDefined(StringBuilder errs, String propname) {
        errs.append('\n');
        errs.append(propname);
        errs.append(" must be defined.");

    }

    public String getEntryFQDN(final String entry, final String dot_le) {
        String str;
        if (public_fqdn != null && dot_le.isEmpty()) {
            str = public_fqdn;
        } else {
            str = access.getProperty(Config.AAF_LOCATOR_FQDN + dot_le, default_fqdn);
        }
        return replacements("RegistrationPropHolder.getEntryFQDN", str, entry, dot_le);
    }

    public String getEntryName(final String entry, final String dot_le) {
        String str;
        if (dot_le.isEmpty()) {
            str = default_name;
        } else {
            str = access.getProperty(Config.AAF_LOCATOR_NAME + dot_le, default_name);
        }
        return replacements("RegistrationPropHolder.getEntryName", str, entry, dot_le);
    }

    public String getPublicEntryName(final String entry, final String dot_le) {
        String str = access.getProperty(Config.AAF_LOCATOR_PUBLIC_NAME + dot_le, null);
        if (str == null) {
            str = access.getProperty(Config.AAF_LOCATOR_PUBLIC_NAME, null);
        }
        if (str == null) {
            str = default_name;
        }
        return replacements("RegistrationPropHolder.getEntryName", str, entry, dot_le);
    }


    private String getNS(String dot_le) {
        String ns;
        ns = access.getProperty(Config.AAF_LOCATOR_APP_NS + dot_le, null);
        if (ns == null) {
            ns = access.getProperty(Config.AAF_LOCATOR_APP_NS, "AAF_NS");
        }
        return ns;
    }


    public String replacements(final String fromCode, final String source, final String name, final String _dot_le) {
        if (source == null) {
            return "";
        } else if (source.isEmpty()) {
            return source;
        }
        String value = source;
        String dot_le;
        if (_dot_le == null) {
            dot_le = default_container.isEmpty() ? "" : '.' + default_container;
        } else {
            dot_le = _dot_le;
        }

        String aaf_locator_host = access.getProperty(Config.AAF_LOCATE_URL + dot_le, null);
        if (aaf_locator_host == null) {
            aaf_locator_host = access.getProperty(Config.AAF_LOCATE_URL, null);
        }

        String str;
        if (aaf_locator_host != null) {
            if ("https://AAF_LOCATE_URL".equals(value)) {
                value = aaf_locator_host;
            } else {
                str = aaf_locator_host;
                if (value.indexOf(Config.AAF_LOCATE_URL_TAG) >= 0) {
                    if (!str.endsWith("/")) {
                        str += '/';
                    }
                    if (!str.endsWith("/locate/")) {
                        str += "locate/";
                    }
                    if (value.startsWith("http:")) {
                        value = value.replace("http://AAF_LOCATE_URL/", str);
                    } else {
                        value = value.replace("https://AAF_LOCATE_URL/", str);

                    }
                }
            }
        }

        int atC = value.indexOf("%C");
        if (atC >= 0) {
            // aaf_locator_container_ns
            str = access.getProperty(Config.AAF_LOCATOR_CONTAINER_NS + dot_le, default_container_ns);
            if (str.isEmpty()) {
                value = value.replace("%CNS" + '.', str);
            }
            value = value.replace("%CNS", str);

            str = access.getProperty(Config.AAF_LOCATOR_CONTAINER + dot_le, default_container);
            if (str.isEmpty()) {
                value = value.replace("%C" + '.', str);
            }
            value = value.replace("%C", str);
        }

        if (value.indexOf("%NS") >= 0) {
            str = getNS(dot_le);
            if (str == null || str.isEmpty()) {
                value = value.replace("%NS" + '.', "");
            } else {
                value = value.replace("%NS", str);
            }
        }

        // aaf_root_ns
        if (value.indexOf("AAF_NS") >= 0) {
            str = access.getProperty(Config.AAF_ROOT_NS, Config.AAF_ROOT_NS_DEF) + '.';
            String temp = value.replace("%AAF_NS.", str);
            if (temp.equals(value)) { // intended
                value = value.replace("AAF_NS.", str); // Backward Compatibility
            } else {
                value = temp;
            }
        }


        if (value.indexOf('%') >= 0) {
            // These shouldn't be expected to have dot elements
            if (name != null) {
                value = value.replace("%N", name);
            }
            if (default_fqdn != null) {
                value = value.replace("%DF", default_fqdn);
            }
            if (public_fqdn != null) {
                value = value.replace("%PH", public_fqdn);
            }
        }
        access.printf(Level.DEBUG, "RegistrationReplacement from %s, source: %s, dot_le: %s, value: %s", fromCode,
                source, dot_le, value);

        return value;
    }

    public int getEntryPort(final String dot_le) {
        return public_port != null && dot_le.isEmpty() ? public_port : port;
    }

    public Access access() {
        return access;
    }
}
