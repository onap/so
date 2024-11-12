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

package org.onap.so.security.cadi.taf.dos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.onap.so.security.cadi.Access;
import org.onap.so.security.cadi.CachedPrincipal;
import org.onap.so.security.cadi.CadiException;
import org.onap.so.security.cadi.CachedPrincipal.Resp;
import org.onap.so.security.cadi.Taf.LifeForm;
import org.onap.so.security.cadi.config.Config;
import org.onap.so.security.cadi.taf.HttpTaf;
import org.onap.so.security.cadi.taf.PuntTafResp;
import org.onap.so.security.cadi.taf.TafResp;
import org.onap.so.security.cadi.taf.TafResp.RESP;

public class DenialOfServiceTaf implements HttpTaf {
    private static Map<String, Counter> deniedIP = null, deniedID = null;
    private Access access;
    private final TafResp puntNotDenied;
    private static File dosIP, dosID;

    /**
     *
     * @param hostname
     * @param prod
     * @throws CadiException
     */
    public DenialOfServiceTaf(Access access) throws CadiException {
        puntNotDenied = new PuntTafResp("DenialOfServiceTaf", "This Transaction is not denied");
        this.access = access;
        if (dosIP == null || dosID == null) {
            String dirStr;
            if ((dirStr = access.getProperty(Config.AAF_DATA_DIR, null)) != null) {
                dosIP = new File(dirStr + "/dosIP");
                readIP();
                dosID = new File(dirStr + "/dosID");
                readID();
            }
        }
    }

    @Override
    public TafResp validate(LifeForm reading, HttpServletRequest req, final HttpServletResponse resp) {
        // Performance, when not needed
        if (deniedIP != null) {
            String ip;
            Counter c = deniedIP.get(ip = req.getRemoteAddr());
            if (c != null) {
                c.inc();
                return respDenyIP(access, ip);
            }
        }

        // Note: Can't process Principal, because this is the first TAF, and no Principal is created.
        // Other TAFs use "isDenied()" on this Object to validate.
        return puntNotDenied;
    }

    @Override
    public Resp revalidate(CachedPrincipal prin, Object state) {
        // We always return NOT MINE, because DOS Taf does not ever validate
        return Resp.NOT_MINE;
    }

    /*
     * for use in Other TAFs, before they attempt backend validation of
     */
    public static Counter isDeniedID(String identity) {
        if (deniedID != null) {
            return deniedID.get(identity);
        }
        return null;
    }

    /**
     *
     */
    public static Counter isDeniedIP(String ipvX) {
        if (deniedIP != null) {
            return deniedIP.get(ipvX);
        }
        return null;
    }

    /**
     * Return of "True" means IP has been added. Return of "False" means IP already added.
     *
     * @param ip
     * @return
     */
    public static synchronized boolean denyIP(String ip) {
        boolean rv = false;
        if (deniedIP == null) {
            deniedIP = new HashMap<>();
            deniedIP.put(ip, new Counter(ip)); // Noted duplicated for minimum time spent
            rv = true;
        } else if (deniedIP.get(ip) == null) {
            deniedIP.put(ip, new Counter(ip));
            rv = true;
        }
        if (rv) {
            writeIP();
        }
        return rv;
    }

    private static void writeIP() {
        if (dosIP != null && deniedIP != null) {
            if (deniedIP.isEmpty()) {
                if (dosIP.exists()) {
                    dosIP.delete();
                }
            } else {
                PrintStream fos;
                try {
                    fos = new PrintStream(new FileOutputStream(dosIP, false));
                    try {
                        for (String ip : deniedIP.keySet()) {
                            fos.println(ip);
                        }
                    } finally {
                        fos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace(System.err);
                }
            }
        }
    }

    private static void readIP() {
        if (dosIP != null && dosIP.exists()) {
            BufferedReader br;
            try {
                br = new BufferedReader(new FileReader(dosIP));
                try {
                    if (deniedIP == null) {
                        deniedIP = new HashMap<>();
                    }

                    String line;
                    while ((line = br.readLine()) != null) {
                        deniedIP.put(line, new Counter(line));
                    }
                } finally {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }
    }


    /**
     * Return of "True" means IP has was removed. Return of "False" means IP wasn't being denied.
     *
     * @param ip
     * @return
     */
    public static synchronized boolean removeDenyIP(String ip) {
        if (deniedIP != null && deniedIP.remove(ip) != null) {
            writeIP();
            if (deniedIP.isEmpty()) {
                deniedIP = null;
            }
            return true;
        }
        return false;
    }

    /**
     * Return of "True" means ID has been added. Return of "False" means ID already added.
     *
     * @param ip
     * @return
     */
    public static synchronized boolean denyID(String id) {
        boolean rv = false;
        if (deniedID == null) {
            deniedID = new HashMap<>();
            deniedID.put(id, new Counter(id)); // Noted duplicated for minimum time spent
            rv = true;
        } else if (deniedID.get(id) == null) {
            deniedID.put(id, new Counter(id));
            rv = true;
        }
        if (rv) {
            writeID();
        }
        return rv;

    }

    private static void writeID() {
        if (dosID != null && deniedID != null) {
            if (deniedID.isEmpty()) {
                if (dosID.exists()) {
                    dosID.delete();
                }
            } else {
                PrintStream fos;
                try {
                    fos = new PrintStream(new FileOutputStream(dosID, false));
                    try {
                        for (String ip : deniedID.keySet()) {
                            fos.println(ip);
                        }
                    } finally {
                        fos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace(System.err);
                }
            }
        }
    }

    private static void readID() {
        if (dosID != null && dosID.exists()) {
            BufferedReader br;
            try {
                br = new BufferedReader(new FileReader(dosID));
                try {
                    if (deniedID == null) {
                        deniedID = new HashMap<>();
                    }

                    String line;
                    while ((line = br.readLine()) != null) {
                        deniedID.put(line, new Counter(line));
                    }
                } finally {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }
    }

    /**
     * Return of "True" means ID has was removed. Return of "False" means ID wasn't being denied.
     *
     * @param ip
     * @return
     */
    public static synchronized boolean removeDenyID(String id) {
        if (deniedID != null && deniedID.remove(id) != null) {
            writeID();
            if (deniedID.isEmpty()) {
                deniedID = null;
            }

            return true;
        }
        return false;
    }

    public List<String> report() {
        int initSize = 0;
        if (deniedIP != null)
            initSize += deniedIP.size();
        if (deniedID != null)
            initSize += deniedID.size();
        ArrayList<String> al = new ArrayList<>(initSize);
        if (deniedID != null) {
            for (Counter c : deniedID.values()) {
                al.add(c.toString());
            }
        }
        if (deniedIP != null) {
            for (Counter c : deniedIP.values()) {
                al.add(c.toString());
            }
        }
        return al;
    }

    public static class Counter {
        private final String name;
        private int count = 0;
        private Date first;
        private long last; // note, we use "last" as long, to avoid popping useless dates on Heap.

        public Counter(String name) {
            this.name = name;
            first = null;
            last = 0L;
            count = 0;
        }

        public String getName() {
            return name;
        }

        public int getCount() {
            return count;
        }

        public long getLast() {
            return last;
        }

        /*
         * Only allow Denial of ServiceTaf to increment
         */
        private synchronized void inc() {
            ++count;
            last = System.currentTimeMillis();
            if (first == null) {
                first = new Date(last);
            }
        }

        public String toString() {
            if (count == 0)
                return name + " is on the denied list, but has not attempted Access";
            else
                return name + " has been denied " + count + " times since " + first + ".  Last denial was "
                        + new Date(last);
        }
    }

    public static TafResp respDenyID(Access access, String identity) {
        return new DenialOfServiceTafResp(access, RESP.NO_FURTHER_PROCESSING,
                identity + " is on the Identity Denial list");
    }

    public static TafResp respDenyIP(Access access, String ip) {
        return new DenialOfServiceTafResp(access, RESP.NO_FURTHER_PROCESSING, ip + " is on the IP Denial list");
    }

}
