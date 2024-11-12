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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import org.onap.so.security.cadi.Access;
import org.onap.so.security.cadi.Access.Level;
import org.onap.so.security.cadi.CadiException;

/**
 * Read CSV file for various purposes
 *
 * @author Instrumental(Jonathan)
 *
 */
public class CSV {
    private File csv;
    private Access access;
    private boolean processAll;
    private char delimiter = ',';
    private boolean go;

    public CSV(Access access, File file) {
        this.access = access;
        csv = file;
        processAll = false;
        go = true;
    }

    public CSV(Access access, String csvFilename) {
        this.access = access;
        csv = new File(csvFilename);
        processAll = false;
        go = true;
    }

    public CSV setDelimiter(char delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    public String name() {
        return csv.getName();
    }

    public CSV processAll() {
        processAll = true;
        return this;
    }

    /*
     * Create your code to accept the List<String> row.
     *
     * Your code may keep the List... CSV does not hold onto it.
     *
     * @author Instrumental(Jonathan)
     *
     */
    public interface Visitor {
        void visit(List<String> row) throws IOException, CadiException;
    }

    public void visit(Visitor visitor) throws IOException, CadiException {
        BufferedReader br = new BufferedReader(new FileReader(csv));
        try {
            String line;
            StringBuilder sb = new StringBuilder();
            while (go && (line = br.readLine()) != null) {
                line = line.trim();
                if (!line.startsWith("#") && line.length() > 0) {
                    // System.out.println(line); uncomment to debug
                    List<String> row = new ArrayList<>();
                    boolean quotes = false;
                    boolean escape = false;
                    char c = 0;
                    for (int i = 0; i < line.length(); ++i) {
                        switch (c = line.charAt(i)) {
                            case '"':
                                if (quotes) {
                                    if (i < line.length() - 1) { // may look ahead
                                        if ('"' == line.charAt(i + 1)) {
                                            sb.append(c);
                                            ++i;
                                        } else {
                                            quotes = false;
                                        }
                                    } else {
                                        quotes = false;
                                    }
                                } else {
                                    quotes = true;
                                }
                                break;
                            case '\\':
                                if (escape) {
                                    sb.append(c);
                                    escape = false;
                                } else {
                                    escape = true;
                                }
                                break;
                            case 'n':
                                if (escape) {
                                    sb.append("\\n");
                                    escape = false;
                                } else {
                                    sb.append(c);
                                }
                                break;
                            default:
                                if (delimiter == c) {
                                    if (quotes) {
                                        sb.append(c);
                                    } else {
                                        row.add(sb.toString());
                                        sb.setLength(0);
                                    }
                                } else {
                                    sb.append(c);
                                }
                        }
                    }
                    if (sb.length() > 0 || c == ',') {
                        row.add(sb.toString());
                        sb.setLength(0);
                    }
                    try {
                        visitor.visit(row);
                    } catch (CadiException e) {
                        if (processAll) {
                            access.log(Level.ERROR, e);
                        } else {
                            throw e;
                        }
                    }
                }
            }
        } finally {
            br.close();
        }
    }

    public Writer writer() throws FileNotFoundException {
        return new Writer(false);
    }

    public Writer writer(boolean append) throws FileNotFoundException {
        return new Writer(append);
    }

    public interface RowSetter {
        public void row(Object... objs);
    }

    public static class Saver implements RowSetter {
        List<String> ls = new ArrayList<>();

        @Override
        public void row(Object... objs) {
            if (objs.length > 0) {
                for (Object o : objs) {
                    if (o != null) {
                        if (o instanceof String[]) {
                            for (String str : (String[]) o) {
                                ls.add(str);
                            }
                        } else {
                            ls.add(o.toString());
                        }
                    }
                }
            }
        }

        public List<String> asList() {
            List<String> rv = ls;
            ls = new ArrayList<>();
            return rv;
        }
    }

    public class Writer implements RowSetter {
        private PrintStream ps;

        private Writer(final boolean append) throws FileNotFoundException {
            ps = new PrintStream(new FileOutputStream(csv, append));
        }

        @Override
        public void row(Object... objs) {
            if (objs.length > 0) {
                boolean first = true;
                for (Object o : objs) {
                    if (first) {
                        first = false;
                    } else {
                        ps.append(delimiter);
                    }
                    if (o == null) {
                    } else if (o instanceof String[]) {
                        for (String str : (String[]) o) {
                            print(str);
                        }
                    } else {
                        print(o.toString());
                    }
                }
                ps.println();
            }
        }

        private void print(String s) {
            boolean quote = s.matches(".*[,|\"].*");
            if (quote) {
                ps.append('"');
                ps.print(s.replace("\"", "\"\"").replace("'", "''").replace("\\", "\\\\"));
                ps.append('"');
            } else {
                ps.append(s);
            }


        }

        /**
         * Note: CSV files do not actually support Comments as a standard, but it is useful
         * 
         * @param comment
         */
        public void comment(String comment, Object... objs) {
            ps.print("# ");
            ps.printf(comment, objs);
            ps.println();
        }

        public void flush() {
            ps.flush();
        }

        public void close() {
            flush();
            ps.close();
        }

        public String toString() {
            return csv.getAbsolutePath();
        }
    }

    /**
     * Provides a way to stop processing records from inside a Visit
     */
    public void stop() {
        go = false;
    }

    public void delete() {
        csv.delete();
    }

    public String toString() {
        return csv.getAbsolutePath();
    }

}
