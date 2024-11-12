/**
 * ============LICENSE_START==================================================== org.onap.so
 * =========================================================================== Copyright (c) 2018 AT&T Intellectual
 * Property. All rights reserved.
 *
 * Modifications Copyright (C) 2018 IBM. ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ============LICENSE_END====================================================
 *
 */

package org.onap.so.security.cadi;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import org.onap.so.security.cadi.config.Config;
import org.onap.so.security.cadi.config.SecurityInfo;
import org.onap.so.security.cadi.util.Split;

public class PropAccess implements Access {
    // Sonar says cannot be static... it's ok. not too many PropAccesses created.
    private final SimpleDateFormat iso8601 = newISO8601();
    private Symm symm;
    public static final Level DEFAULT = Level.AUDIT;
    private int level;
    private Properties props;
    private List<String> recursionProtection = null;
    private LogIt logIt;
    private String name;

    public PropAccess() {
        logIt = new StreamLogIt(System.out);
        init(null);
    }

    /**
     * This Constructor soly exists to instantiate Servlet Context Based Logging that will call "init" later.
     * 
     * @param sc
     */
    protected PropAccess(Object o) {
        logIt = new StreamLogIt(System.out);
        props = new Properties();
    }

    public PropAccess(String... args) {
        this(System.out, args);
    }

    public PropAccess(PrintStream ps, String[] args) {
        logIt = new StreamLogIt(ps == null ? System.out : ps);
        init(logIt, args);
    }

    public PropAccess(LogIt logit, String[] args) {
        init(logit, args);
    }

    public PropAccess(Properties p) {
        this(System.out, p);
    }

    public PropAccess(PrintStream ps, Properties p) {
        logIt = new StreamLogIt(ps == null ? System.out : ps);
        init(p);
    }

    protected void init(final LogIt logIt, final String[] args) {
        this.logIt = logIt;
        Properties nprops = new Properties();
        int eq;
        for (String arg : args) {
            if ((eq = arg.indexOf('=')) > 0) {
                nprops.setProperty(arg.substring(0, eq), arg.substring(eq + 1));
            }
        }
        init(nprops);
    }

    public static SimpleDateFormat newISO8601() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    }

    protected synchronized void init(Properties p) {
        // Make sure these two are set before any changes in Logging
        name = "cadi";

        props = new Properties();
        // First, load related System Properties
        for (Entry<Object, Object> es : System.getProperties().entrySet()) {
            String key = es.getKey().toString();
            for (String start : new String[] {"cadi_", "aaf_", "cm_"}) {
                if (key.startsWith(start)) {
                    props.put(key, es.getValue());
                }
            }
        }
        // Second, overlay or fill in with Passed in Props
        if (p != null) {
            props.putAll(p);
        }

        // Preset LogLevel
        String sLevel = props.getProperty(Config.CADI_LOGLEVEL);
        // Third, load any Chained Property Files
        load(props.getProperty(Config.CADI_PROP_FILES));

        if (sLevel == null) { // if LogLev wasn't set before, check again after Chained Load
            sLevel = props.getProperty(Config.CADI_LOGLEVEL);
            if (sLevel == null) {
                level = DEFAULT.maskOf();
            } else {
                level = Level.valueOf(sLevel).maskOf();
            }
        }
        // Setup local Symmetrical key encryption
        if (symm == null) {
            try {
                symm = Symm.obtain(this);
            } catch (CadiException e) {
                System.err.append("FATAL ERROR: Cannot obtain Key Information.");
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }

        name = props.getProperty(Config.CADI_LOGNAME, name);

        SecurityInfo.setHTTPProtocols(this);

    }


    private void load(String cadi_prop_files) {
        if (cadi_prop_files == null) {
            return;
        }
        String prevKeyFile = props.getProperty(Config.CADI_KEYFILE);


        for (String filename : Split.splitTrim(File.pathSeparatorChar, cadi_prop_files)) {
            Properties fileProps = new Properties();
            File file = new File(filename);
            if (file.exists()) {
                printf(Level.INIT, "Loading CADI Properties from %s", file.getAbsolutePath());
                try {
                    FileInputStream fis = new FileInputStream(file);
                    try {
                        fileProps.load(fis);
                        // Only load props from recursion which are not already in props
                        // meaning top Property file takes precedence
                        for (Entry<Object, Object> es : fileProps.entrySet()) {
                            if (props.get(es.getKey()) == null) {
                                String key = es.getKey().toString();
                                String value = es.getValue().toString();
                                props.put(key, value);
                                if (key.contains("pass")) {
                                    value = "vi XX";
                                }
                                printf(Level.DEBUG, "  %s=%s", key, value);
                            }
                        }
                        // Recursively Load
                        String chainProp = fileProps.getProperty(Config.CADI_PROP_FILES);
                        if (chainProp != null) {
                            if (recursionProtection == null) {
                                recursionProtection = new ArrayList<>();
                                recursionProtection.add(cadi_prop_files);
                            }
                            if (!recursionProtection.contains(chainProp)) {
                                recursionProtection.add(chainProp);
                                load(chainProp); // recurse
                            }
                        }
                    } finally {
                        fis.close();
                    }
                } catch (Exception e) {
                    log(e, filename, "cannot be opened");
                }
            } else {
                printf(Level.WARN, "Warning: recursive CADI Property %s does not exist", file.getAbsolutePath());
            }
        }

        // Trim
        for (Entry<Object, Object> es : props.entrySet()) {
            Object value = es.getValue();
            if (value instanceof String) {
                String trim = ((String) value).trim();
                // Remove Beginning/End Quotes, which might be there if mixed with Bash Props
                int s = 0, e = trim.length() - 1;
                if (s < e && trim.charAt(s) == '"' && trim.charAt(e) == '"') {
                    trim = trim.substring(s + 1, e);
                }
                if (trim != value) { // Yes, I want OBJECT equals
                    props.setProperty((String) es.getKey(), trim);
                }
            }
        }
        // Reset Symm if Keyfile Changes:
        String newKeyFile = props.getProperty(Config.CADI_KEYFILE);
        if ((prevKeyFile != null && newKeyFile != null) || (newKeyFile != null && !newKeyFile.equals(prevKeyFile))) {
            try {
                symm = Symm.obtain(this);
            } catch (CadiException e) {
                System.err.append("FATAL ERROR: Cannot obtain Key Information.");
                e.printStackTrace(System.err);
                System.exit(1);
            }

            prevKeyFile = newKeyFile;
        }

        String loglevel = props.getProperty(Config.CADI_LOGLEVEL);
        if (loglevel != null) {
            try {
                level = Level.valueOf(loglevel).maskOf();
            } catch (IllegalArgumentException e) {
                printf(Level.ERROR, "%s=%s is an Invalid Log Level", Config.CADI_LOGLEVEL, loglevel);
            }
        }
    }

    @Override
    public void load(InputStream is) throws IOException {
        props.load(is);
        load(props.getProperty(Config.CADI_PROP_FILES));
    }

    @Override
    public void log(Level level, Object... elements) {
        if (willLog(level)) {
            logIt.push(level, elements);
        }
    }

    public StringBuilder buildMsg(Level level, Object[] elements) {
        return buildMsg(name, iso8601, level, elements);
    }

    /*
     * Need to pass in DateFormat per thread, because not marked as thread safe
     */
    public static StringBuilder buildMsg(final String name, final DateFormat sdf, Level level, Object[] elements) {
        final StringBuilder sb;
        int end = elements.length;
        if (sdf == null) {
            sb = new StringBuilder();
            write(true, sb, elements);
        } else {
            sb = new StringBuilder(sdf.format(new Date()));
            sb.append(' ');
            sb.append(level.name());
            sb.append(" [");
            sb.append(name);
            if (end <= 0) {
                sb.append("] ");
            } else {
                int idx = 0;
                if (elements[idx] != null && elements[idx] instanceof Integer) {
                    sb.append('-');
                    sb.append(elements[idx]);
                    ++idx;
                }
                sb.append("] ");
                write(true, sb, elements);
            }
        }
        return sb;
    }

    private static boolean write(boolean first, StringBuilder sb, Object[] elements) {
        String s;
        for (Object o : elements) {
            if (o != null) {
                if (o.getClass().isArray()) {
                    first = write(first, sb, (Object[]) o);
                } else if (o instanceof Throwable) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    PrintStream ps = new PrintStream(baos);
                    ((Throwable) o).printStackTrace(ps);
                    sb.append(baos.toString());
                } else {
                    s = o.toString();
                    if (first) {
                        first = false;
                    } else {
                        int l = s.length();
                        if (l > 0) {
                            switch (s.charAt(l - 1)) {
                                case ' ':
                                    break;
                                default:
                                    sb.append(' ');
                            }
                        }
                    }
                    sb.append(s);
                }
            }
        }
        return first;
    }

    @Override
    public void log(Exception e, Object... elements) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println();
        e.printStackTrace(pw);
        log(Level.ERROR, elements, sw.toString());
    }

    @Override
    public void printf(Level level, String fmt, Object... elements) {
        if (willLog(level)) {
            log(level, String.format(fmt, elements));
        }
    }

    @Override
    public void setLogLevel(Level level) {
        this.level = level.maskOf();
    }

    @Override
    public boolean willLog(Level level) {
        return level.inMask(this.level);
    }

    @Override
    public ClassLoader classLoader() {
        return ClassLoader.getSystemClassLoader();
    }

    @Override
    public String getProperty(String tag, String def) {
        return props.getProperty(tag, def);
    }

    @Override
    public String decrypt(String encrypted, boolean anytext) throws IOException {
        return (encrypted != null && (anytext == true || encrypted.startsWith(Symm.ENC))) ? symm.depass(encrypted)
                : encrypted;
    }

    public String encrypt(String unencrypted) throws IOException {
        return Symm.ENC + symm.enpass(unencrypted);
    }

    //////////////////
    // Additional
    //////////////////
    public String getProperty(String tag) {
        return props.getProperty(tag);
    }


    public Properties getProperties() {
        return props;
    }

    public void setProperty(String tag, String value) {
        if (value != null) {
            props.put(tag, value);
            if (Config.CADI_KEYFILE.equals(tag)) {
                // reset decryption too
                try {
                    symm = Symm.obtain(this);
                } catch (CadiException e) {
                    System.err.append("FATAL ERROR: Cannot obtain Key Information.");
                    e.printStackTrace(System.err);
                    System.exit(1);
                }
            }
        }
    }

    public interface LogIt {
        public void push(Level level, Object... elements);
    }

    private class StreamLogIt implements LogIt {
        private PrintStream ps;

        public StreamLogIt(PrintStream ps) {
            this.ps = ps;
        }

        @Override
        public void push(Level level, Object... elements) {
            ps.println(buildMsg(level, elements));
            ps.flush();
        }
    }

    public void set(LogIt logit) {
        logIt = logit;
    }

    public void setStreamLogIt(PrintStream ps) {
        logIt = new StreamLogIt(ps);
    }

    public String toString() {
        return props.toString();
    }
}
