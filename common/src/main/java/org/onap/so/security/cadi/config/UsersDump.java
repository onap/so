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

package org.onap.so.security.cadi.config;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.HashSet;
import org.onap.so.security.cadi.AbsUserCache;
import org.onap.so.security.cadi.lur.LocalLur;

public class UsersDump {

    /**
     * @param args
     */
    public static boolean write(OutputStream os, AbsUserCache<?> lur) {
        PrintStream ps;
        if (os instanceof PrintStream) {
            ps = (PrintStream) os;
        } else {
            ps = new PrintStream(os);
        }
        try {
            ps.println("<?xml version='1.0' encoding='utf-8'?>");
            ps.println("<!--");
            ps.print("     Code Generated Tomcat Users and Roles from AT&T LUR on ");
            ps.println(new Date());
            ps.println("-->");
            ps.println("<tomcat-users>");

            // We loop through Users, but want to write Groups first... therefore, save off print
            StringBuilder sb = new StringBuilder();

            // Obtain all unique role names
            HashSet<String> groups = new HashSet<>();
            for (AbsUserCache<?>.DumpInfo di : lur.dumpInfo()) {
                sb.append("\n  <user username=\"");
                sb.append(di.user);
                sb.append("\" roles=\"");
                boolean first = true;
                for (String role : di.perms) {
                    groups.add(role);
                    if (first)
                        first = false;
                    else
                        sb.append(',');
                    sb.append(role);
                }
                sb.append("\"/>");

            }

            // Print roles
            for (String group : groups) {
                ps.print("  <role rolename=\"");
                ps.print(group);
                ps.println("\"/>");
            }

            ps.println(sb);

            ps.println("</tomcat-users>");
            ps.flush();
        } catch (Exception t) {
            t.printStackTrace(ps);
            return false;
        }
        return true;
    }

    /**
     *
     * Note: This method returns a String if there's an error, or null if ok. This unusual style is necessitated by the
     * fact that any Exceptions thrown are likely to be unlogged and hidden from view, making debugging almost
     * impossible.
     *
     * @param writeto
     * @param up
     * @return
     */
    public static String updateUsers(String writeto, LocalLur up) {
        // Dump a Tomcat-user.xml lookalike (anywhere)
        if (writeto != null) {
            // First read content
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (UsersDump.write(baos, up)) {
                byte[] postulate = baos.toByteArray();
                // now get contents of file
                File file = new File(writeto);
                boolean writeIt;
                if (file.exists()) {
                    try {
                        FileInputStream fis = new FileInputStream(file);
                        byte[] orig = new byte[(int) file.length()];
                        int read;
                        try {
                            read = fis.read(orig);
                        } finally {
                            fis.close();
                        }
                        if (read <= 0) {
                            writeIt = false;
                        } else {
                            // Starting at third "<" (<tomcat-users> line)
                            int startA = 0, startB = 0;
                            for (int i = 0; startA < orig.length && i < 3; ++startA)
                                if (orig[startA] == '<')
                                    ++i;
                            for (int i = 0; startB < orig.length && i < 3; ++startB)
                                if (postulate[startB] == '<')
                                    ++i;

                            writeIt = orig.length - startA != postulate.length - startB; // first, check if remaining
                                                                                         // length is the same
                            while (!writeIt && startA < orig.length && startB < postulate.length) {
                                if (orig[startA++] != postulate[startB++])
                                    writeIt = true;
                            }
                        }
                    } catch (Exception e) {
                        writeIt = true;
                    }
                } else {
                    writeIt = true;
                }

                if (writeIt) {
                    try {
                        FileOutputStream fos = new FileOutputStream(file);
                        try {
                            fos.write(postulate);
                        } finally {
                            fos.close();
                        }
                    } catch (IOException e) {
                        return e.getMessage();
                    }
                }
            }
        }
        return null; // no message means ok.
    }

}
