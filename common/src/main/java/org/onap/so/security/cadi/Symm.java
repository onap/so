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

package org.onap.so.security.cadi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import org.onap.so.security.cadi.Access.Level;
import org.onap.so.security.cadi.config.Config;

/**
 * Key Conversion, primarily "Base64"
 *
 * Base64 is required for "Basic Authorization", which is an important part of the overall CADI Package.
 *
 * Note: This author found that there is not a "standard" library for Base64 conversion within Java. The source code
 * implementations available elsewhere were surprisingly inefficient, requiring, for instance, multiple string creation,
 * on a transaction pass. Integrating other packages that might be efficient enough would put undue Jar File
 * Dependencies given this Framework should have none-but-Java dependencies.
 *
 * The essential algorithm is good for a symmetrical key system, as Base64 is really just a symmetrical key that
 * everyone knows the values.
 *
 * This code is quite fast, taking about .016 ms for encrypting, decrypting and even .08 for key generation. The speed
 * quality, especially of key generation makes this a candidate for a short term token used for identity.
 *
 * It may be used to easily avoid placing Clear-Text passwords in configurations, etc. and contains supporting functions
 * such as 2048 keyfile generation (see keygen). This keyfile should, of course, be set to "400" (Unix) and protected as
 * any other mechanism requires.
 *
 * AES Encryption is also employed to include standards.
 *
 * @author Jonathan
 *
 */
public class Symm {
    private static final byte[] DOUBLE_EQ = new byte[] {'=', '='};
    public static final String ENC = "enc:";
    private static final Object LOCK = new Object();
    private static final SecureRandom random = new SecureRandom();

    public final char[] codeset;
    private final int splitLinesAt;
    private final String encoding;
    private final Convert convert;
    private final boolean endEquals;
    private byte[] keyBytes = null;
    // Note: AES Encryption is not Thread Safe. It is Synchronized
    // private AES aes = null; // only initialized from File, and only if needed for Passwords
    private String name;

    /**
     * This is the standard base64 Key Set. RFC 2045
     */
    public static final Symm base64 =
            new Symm("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray(), 76, Config.UTF_8,
                    true, "Base64");

    public static final Symm base64noSplit =
            new Symm("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray(),
                    Integer.MAX_VALUE, Config.UTF_8, true, "Base64, no Split");

    /**
     * This is the standard base64 set suitable for URLs and Filenames RFC 4648
     */
    public static final Symm base64url =
            new Symm("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_".toCharArray(), 76, Config.UTF_8,
                    true, "Base64 for URL");

    /**
     * A Password set, using US-ASCII RFC 4648
     */
    public static final Symm encrypt = new Symm(base64url.codeset, 1024, "US-ASCII", false, "Base64, 1024 size");
    private static final byte[] EMPTY = new byte[0];

    /**
     * A typical set of Password Chars Note, this is too large to fit into the algorithm. Only use with PassGen
     */
    private static char passChars[] =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+!@#$%^&*(){}[]?:;,.".toCharArray();


    private static Symm internalOnly = null;

    /**
     * Use this to create special case Case Sets and/or Line breaks
     *
     * If you don't know why you need this, use the Singleton Method
     *
     * @param codeset
     * @param split
     */
    public Symm(char[] codeset, int split, String charset, boolean useEndEquals, String name) {
        this.codeset = codeset;
        splitLinesAt = split;
        encoding = charset;
        endEquals = useEndEquals;
        this.name = name;
        char prev = 0, curr = 0, first = 0;
        int offset = Integer.SIZE; // something that's out of range for integer array

        // There can be time efficiencies gained when the underlying keyset consists mainly of ordered
        // data (i.e. abcde...). Therefore, we'll quickly analyze the keyset. If it proves to have
        // too much entropy, the "Unordered" algorithm, which is faster in such cases is used.
        ArrayList<int[]> la = new ArrayList<>();
        for (int i = 0; i < codeset.length; ++i) {
            curr = codeset[i];
            if (prev + 1 == curr) { // is next character in set
                prev = curr;
            } else {
                if (offset != Integer.SIZE) { // add previous range
                    la.add(new int[] {first, prev, offset});
                }
                first = prev = curr;
                offset = curr - i;
            }
        }
        la.add(new int[] {first, curr, offset});
        if (la.size() > codeset.length / 3) {
            convert = new Unordered(codeset);
        } else { // too random to get speed enhancement from range algorithm
            int[][] range = new int[la.size()][];
            la.toArray(range);
            convert = new Ordered(range);
        }
    }

    public Symm copy(int lines) {
        return new Symm(codeset, lines, encoding, endEquals, "Copied " + lines);
    }

    // Only used by keygen, which is intentionally randomized. Therefore, always use unordered
    private Symm(char[] codeset, Symm parent) {
        this.codeset = codeset;
        splitLinesAt = parent.splitLinesAt;
        endEquals = parent.endEquals;
        encoding = parent.encoding;
        convert = new Unordered(codeset);
    }

    /**
     * Obtain the base64() behavior of this class, for use in standard BASIC AUTH mechanism, etc.
     * 
     * @return
     */
    @Deprecated
    public static final Symm base64() {
        return base64;
    }

    /**
     * Obtain the base64() behavior of this class, for use in standard BASIC AUTH mechanism, etc. No Line Splitting
     * 
     * @return
     */
    @Deprecated
    public static final Symm base64noSplit() {
        return base64noSplit;
    }

    /**
     * Obtain the base64 "URL" behavior of this class, for use in File Names, etc. (no "/")
     */
    @Deprecated
    public static final Symm base64url() {
        return base64url;
    }

    /**
     * Obtain a special ASCII version for Scripting, with base set of base64url use in File Names, etc. (no "/")
     */
    public static final Symm baseCrypt() {
        return encrypt;
    }

    public <T> T exec(SyncExec<T> exec) throws Exception {
        synchronized (LOCK) {
            if (keyBytes == null) {
                keyBytes = new byte[AES.AES_KEY_SIZE / 8];
                int offset = (Math.abs(codeset[0]) + 47) % (codeset.length - keyBytes.length);
                for (int i = 0; i < keyBytes.length; ++i) {
                    keyBytes[i] = (byte) codeset[i + offset];
                }
            }
        }
        return exec.exec(new AES(keyBytes, 0, keyBytes.length));
    }

    public interface Encryption {
        public CipherOutputStream outputStream(OutputStream os, boolean encrypt);

        public CipherInputStream inputStream(InputStream is, boolean encrypt);
    }

    public static interface SyncExec<T> {
        public T exec(Encryption enc) throws IOException, Exception;
    }

    public byte[] encode(byte[] toEncrypt) throws IOException {
        if (toEncrypt == null) {
            return EMPTY;
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream((int) (toEncrypt.length * 1.25));
            encode(new ByteArrayInputStream(toEncrypt), baos);
            return baos.toByteArray();
        }
    }

    public byte[] decode(byte[] encrypted) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream((int) (encrypted.length * 1.25));
        decode(new ByteArrayInputStream(encrypted), baos);
        return baos.toByteArray();
    }

    /**
     * Helper function for String API of "Encode" use "getBytes" with appropriate char encoding, etc.
     *
     * @param str
     * @return
     * @throws IOException
     */
    public String encode(String str) throws IOException {
        byte[] array;
        boolean useDefaultEncoding = false;
        try {
            array = str.getBytes(encoding);
        } catch (IOException e) {
            array = str.getBytes(); // take default
            useDefaultEncoding = true;
        }
        // Calculate expected size to avoid any buffer expansion copies within the ByteArrayOutput code
        ByteArrayOutputStream baos = new ByteArrayOutputStream((int) (array.length * 1.363)); // account for 4 bytes for
                                                                                              // 3 and a byte or two
                                                                                              // more

        encode(new ByteArrayInputStream(array), baos);
        if (useDefaultEncoding) {
            return baos.toString();
        }
        return baos.toString(encoding);
    }

    /**
     * Helper function for the String API of "Decode" use "getBytes" with appropriate char encoding, etc.
     * 
     * @param str
     * @return
     * @throws IOException
     */
    public String decode(String str) throws IOException {
        byte[] array;
        boolean useDefaultEncoding = false;
        try {
            array = str.getBytes(encoding);
        } catch (IOException e) {
            array = str.getBytes(); // take default
            useDefaultEncoding = true;
        }
        // Calculate expected size to avoid any buffer expansion copies within the ByteArrayOutput code
        ByteArrayOutputStream baos = new ByteArrayOutputStream((int) (array.length * .76)); // Decoding is 3 bytes for
                                                                                            // 4. Allocate slightly more
                                                                                            // than 3/4s
        decode(new ByteArrayInputStream(array), baos);
        if (useDefaultEncoding) {
            return baos.toString();
        }
        return baos.toString(encoding);
    }

    /**
     * Convenience Function
     *
     * encode String into InputStream and call encode(InputStream, OutputStream)
     *
     * @param string
     * @param out
     * @throws IOException
     */
    public void encode(String string, OutputStream out) throws IOException {
        encode(new ByteArrayInputStream(string.getBytes()), out);
    }

    /**
     * Convenience Function
     *
     * encode String into InputStream and call decode(InputStream, OutputStream)
     *
     * @param string
     * @param out
     * @throws IOException
     */
    public void decode(String string, OutputStream out) throws IOException {
        decode(new ByteArrayInputStream(string.getBytes()), out);
    }

    public void encode(InputStream is, OutputStream os, byte[] prefix) throws IOException {
        os.write(prefix);
        encode(is, os);
    }

    /**
     * encode InputStream onto Output Stream
     *
     * @param is
     * @param estimate
     * @return
     * @throws IOException
     */
    public void encode(InputStream is, OutputStream os) throws IOException {
        // StringBuilder sb = new StringBuilder((int)(estimate*1.255)); // try to get the right size of StringBuilder
        // from start.. slightly more than 1.25 times
        int prev = 0;
        int read, idx = 0, line = 0;
        boolean go;
        do {
            read = is.read();
            if (go = read >= 0) {
                if (line >= splitLinesAt) {
                    os.write('\n');
                    line = 0;
                }
                switch (++idx) { // 1 based reading, slightly faster ++
                    case 1: // ptr is the first 6 bits of read
                        os.write(codeset[read >> 2]);
                        prev = read;
                        break;
                    case 2: // ptr is the last 2 bits of prev followed by the first 4 bits of read
                        os.write(codeset[((prev & 0x03) << 4) | (read >> 4)]);
                        prev = read;
                        break;
                    default: // (3+)
                             // Char 1 is last 4 bits of prev plus the first 2 bits of read
                             // Char 2 is the last 6 bits of read
                        os.write(codeset[(((prev & 0xF) << 2) | (read >> 6))]);
                        if (line == splitLinesAt) { // deal with line splitting for two characters
                            os.write('\n');
                            line = 0;
                        }
                        os.write(codeset[(read & 0x3F)]);
                        ++line;
                        idx = 0;
                        prev = 0;
                }
                ++line;
            } else { // deal with any remaining bits from Prev, then pad
                switch (idx) {
                    case 1: // just the last 2 bits of prev
                        os.write(codeset[(prev & 0x03) << 4]);
                        if (endEquals)
                            os.write(DOUBLE_EQ);
                        break;
                    case 2: // just the last 4 bits of prev
                        os.write(codeset[(prev & 0xF) << 2]);
                        if (endEquals)
                            os.write('=');
                        break;
                }
                idx = 0;
            }

        } while (go);
    }

    public void decode(InputStream is, OutputStream os, int skip) throws IOException {
        if (is.skip(skip) != skip) {
            throw new IOException("Error skipping on IOStream in Symm");
        }
        decode(is, os);
    }

    /**
     * Decode InputStream onto OutputStream
     * 
     * @param is
     * @param os
     * @throws IOException
     */
    public void decode(InputStream is, OutputStream os) throws IOException {
        int read, idx = 0;
        int prev = 0, index;
        while ((read = is.read()) >= 0) {
            index = convert.convert(read);
            if (index >= 0) {
                switch (++idx) { // 1 based cases, slightly faster ++
                    case 1: // index goes into first 6 bits of prev
                        prev = index << 2;
                        break;
                    case 2: // write second 2 bits of into prev, write byte, last 4 bits go into prev
                        os.write((byte) (prev | (index >> 4)));
                        prev = index << 4;
                        break;
                    case 3: // first 4 bits of index goes into prev, write byte, last 2 bits go into prev
                        os.write((byte) (prev | (index >> 2)));
                        prev = index << 6;
                        break;
                    default: // (3+) | prev and last six of index
                        os.write((byte) (prev | (index & 0x3F)));
                        idx = prev = 0;
                }
            }
        } ;
        os.flush();
    }

    /**
     * Interface to allow this class to choose which algorithm to find index of character in Key
     * 
     * @author Jonathan
     *
     */
    private interface Convert {
        public int convert(int read) throws IOException;
    }

    /**
     * Ordered uses a range of orders to compare against, rather than requiring the investigation of every character
     * needed.
     * 
     * @author Jonathan
     *
     */
    private static final class Ordered implements Convert {
        private int[][] range;

        public Ordered(int[][] range) {
            this.range = range;
        }

        public int convert(int read) throws IOException {
            // System.out.print((char)read);
            switch (read) {
                case -1:
                case '=':
                case ' ':
                case '\n':
                case '\r':
                    return -1;
            }
            for (int i = 0; i < range.length; ++i) {
                if (read >= range[i][0] && read <= range[i][1]) {
                    return read - range[i][2];
                }
            }
            throw new IOException("Unacceptable Character in Stream");
        }
    }

    /**
     * Unordered, i.e. the key is purposely randomized, simply has to investigate each character until we find a match.
     * 
     * @author Jonathan
     *
     */
    private static final class Unordered implements Convert {
        private char[] codec;

        public Unordered(char[] codec) {
            this.codec = codec;
        }

        public int convert(int read) throws IOException {
            switch (read) {
                case -1:
                case '=':
                case '\n':
                case '\r':
                    return -1;
            }
            for (int i = 0; i < codec.length; ++i) {
                if (codec[i] == read)
                    return i;
            }
            // don't give clue in Encryption mode
            throw new IOException("Unacceptable Character in Stream");
        }
    }

    /**
     * Generate a 2048 based Key from which we extract our code base
     *
     * @return
     * @throws IOException
     */
    public static byte[] keygen() throws IOException {
        byte inkey[] = new byte[0x600];
        new SecureRandom().nextBytes(inkey);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(0x800);
        base64url.encode(new ByteArrayInputStream(inkey), baos);
        return baos.toByteArray();
    }

    // A class allowing us to be less predictable about significant digits (i.e. not picking them up from the
    // beginning, and not picking them up in an ordered row. Gives a nice 2048 with no visible patterns.
    private class Obtain {
        private int last;
        private int skip;
        private int length;
        private byte[] key;

        private Obtain(Symm b64, byte[] key) {
            skip = Math.abs(key[key.length - 13] % key.length);
            if ((key.length & 0x1) == (skip & 0x1)) { // if both are odd or both are even
                ++skip;
            }
            length = b64.codeset.length;
            last = 17 + length % 59; // never start at beginning
            this.key = key;
        }

        private int next() {
            return Math.abs(key[(++last * skip) % key.length]) % length;
        }
    };

    /**
     * Obtain a Symm from "keyfile" (Config.KEYFILE) property
     *
     * @param acesss
     * @return
     * @throws IOException
     * @throws CadiException
     */
    public static Symm obtain(Access access) throws CadiException {
        String keyfile = access.getProperty(Config.CADI_KEYFILE, null);
        if (keyfile != null) {
            Symm symm = Symm.baseCrypt();

            File file = new File(keyfile);
            try {
                access.log(Level.INIT, Config.CADI_KEYFILE, "points to", file.getCanonicalPath());
            } catch (IOException e1) {
                access.log(Level.INIT, Config.CADI_KEYFILE, "points to", file.getAbsolutePath());
            }
            if (file.exists()) {
                try {
                    FileInputStream fis = new FileInputStream(file);
                    try {
                        symm = Symm.obtain(fis);
                    } finally {
                        try {
                            fis.close();
                        } catch (IOException e) {
                        }
                    }
                } catch (IOException e) {
                    access.log(e, "Cannot load keyfile");
                }
            } else {
                String filename;
                try {
                    filename = file.getCanonicalPath();
                } catch (IOException e) {
                    filename = file.getAbsolutePath();
                }
                throw new CadiException("ERROR: " + filename + " does not exist!");
            }
            return symm;
        } else {
            try {
                return internalOnly();
            } catch (IOException e) {
                throw new CadiException(e);
            }
        }
    }

    /**
     * Create a new random key
     */
    public Symm obtain() throws IOException {
        byte inkey[] = new byte[0x800];
        new SecureRandom().nextBytes(inkey);
        Symm s = obtain(inkey);
        s.name = "from Random";
        return s;
    }

    /**
     * Obtain a Symm from 2048 key from a String
     *
     * @param key
     * @return
     * @throws IOException
     */
    public static Symm obtain(String key) throws IOException {
        Symm s = obtain(new ByteArrayInputStream(key.getBytes()));
        s.name = "from String";
        return s;
    }

    /**
     * Obtain a Symm from 2048 key from a Stream
     *
     * @param is
     * @return
     * @throws IOException
     */
    public static Symm obtain(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            base64url.decode(is, baos);
        } catch (IOException e) {
            // don't give clue
            throw new IOException("Invalid Key");
        }
        byte[] bkey = baos.toByteArray();
        if (bkey.length < 0x88) { // 2048 bit key
            throw new IOException("Invalid key");
        }
        Symm s = baseCrypt().obtain(bkey);
        s.name = "from InputStream";
        return s;
    }

    /**
     * Convenience for picking up Keyfile
     *
     * @param f
     * @return
     * @throws IOException
     */
    public static Symm obtain(File f) throws IOException {
        FileInputStream fis = new FileInputStream(f);
        try {
            Symm s = obtain(fis);
            s.name = "From " + f.getCanonicalPath() + " dated " + new Date(f.lastModified());
            return s;
        } finally {
            fis.close();
        }
    }

    /**
     * Decrypt into a String
     *
     * Convenience method
     *
     * @param password
     * @return
     * @throws IOException
     */
    public String enpass(String password) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        enpass(password, baos);
        return new String(baos.toByteArray());
    }

    /**
     * Create an encrypted password, making sure that even short passwords have a minimum length.
     *
     * @param password
     * @param os
     * @throws IOException
     */
    public void enpass(final String password, final OutputStream os) throws IOException {
        if (password == null) {
            throw new IOException("Invalid password passed");
        }
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        byte[] bytes = password.getBytes();
        if (this.getClass().getSimpleName().startsWith("base64")) { // don't expose randomization
            dos.write(bytes);
        } else {

            Random r = new SecureRandom();
            int start = 0;
            byte b;
            for (int i = 0; i < 3; ++i) {
                dos.writeByte(b = (byte) r.nextInt());
                start += Math.abs(b);
            }
            start %= 0x7;
            for (int i = 0; i < start; ++i) {
                dos.writeByte(r.nextInt());
            }
            dos.writeInt((int) System.currentTimeMillis());
            int minlength = Math.min(0x9, bytes.length);
            dos.writeByte(minlength); // expect truncation
            if (bytes.length < 0x9) {
                for (int i = 0; i < bytes.length; ++i) {
                    dos.writeByte(r.nextInt());
                    dos.writeByte(bytes[i]);
                }
                // make sure it's long enough
                for (int i = bytes.length; i < 0x9; ++i) {
                    dos.writeByte(r.nextInt());
                }
            } else {
                dos.write(bytes);
            }
        }

        // 7/21/2016 Jonathan add AES Encryption to the mix
        try {
            exec(new SyncExec<Void>() {
                @Override
                public Void exec(Encryption enc) throws Exception {
                    CipherInputStream cis = enc.inputStream(new ByteArrayInputStream(baos.toByteArray()), true);
                    try {
                        encode(cis, os);
                    } finally {
                        os.flush();
                        cis.close();
                    }
                    return null;
                }
            });
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * Decrypt a password into a String
     *
     * Convenience method
     *
     * @param password
     * @return
     * @throws IOException
     */
    public String depass(String password) throws IOException {
        if (password == null)
            return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        depass(password, baos);
        return new String(baos.toByteArray());
    }

    /**
     * Decrypt a password
     *
     * Skip Symm.ENC
     *
     * @param password
     * @param os
     * @return
     * @throws IOException
     */
    public long depass(final String password, final OutputStream os) throws IOException {
        int offset = password.startsWith(ENC) ? 4 : 0;
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ByteArrayInputStream bais =
                new ByteArrayInputStream(password.getBytes(), offset, password.length() - offset);
        try {
            exec(new SyncExec<Void>() {
                @Override
                public Void exec(Encryption enc) throws IOException {
                    CipherOutputStream cos = enc.outputStream(baos, false);
                    decode(bais, cos);
                    cos.close(); // flush
                    return null;
                }
            });
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }

        byte[] bytes = baos.toByteArray();
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));
        long time;
        if (this.getClass().getSimpleName().startsWith("base64")) { // don't expose randomization
            os.write(bytes);
            time = 0L;
        } else {
            int start = 0;
            for (int i = 0; i < 3; ++i) {
                start += Math.abs(dis.readByte());
            }
            start %= 0x7;
            for (int i = 0; i < start; ++i) {
                dis.readByte();
            }
            time = (dis.readInt() & 0xFFFF) | (System.currentTimeMillis() & 0xFFFF0000);
            int minlength = dis.readByte();
            if (minlength < 0x9) {
                DataOutputStream dos = new DataOutputStream(os);
                for (int i = 0; i < minlength; ++i) {
                    dis.readByte();
                    dos.writeByte(dis.readByte());
                }
            } else {
                int pre = ((Byte.SIZE * 3 + Integer.SIZE + Byte.SIZE) / Byte.SIZE) + start;
                os.write(bytes, pre, bytes.length - pre);
            }
        }
        return time;
    }

    public static String randomGen(int numBytes) {
        return randomGen(passChars, numBytes);
    }

    public static String randomGen(char[] chars, int numBytes) {
        int rint;
        StringBuilder sb = new StringBuilder(numBytes);
        for (int i = 0; i < numBytes; ++i) {
            rint = random.nextInt(chars.length);
            sb.append(chars[rint]);
        }
        return sb.toString();
    }

    // Internal mechanism for helping to randomize placement of characters within a Symm codeset
    // Based on an incoming data stream (originally created randomly, but can be recreated within
    // 2048 key), go after a particular place in the new codeset. If that codeset spot is used, then move
    // right or left (depending on iteration) to find the next available slot. In this way, key generation
    // is speeded up by only enacting N iterations, but adds a spreading effect of the random number stream, so that
    // keyset is also
    // shuffled for a good spread. It is, however, repeatable, given the same number set, allowing for
    // quick recreation when the official stream is actually obtained.
    public Symm obtain(byte[] key) throws IOException {
        int filled = codeset.length;
        char[] seq = new char[filled];
        int end = filled--;

        boolean right = true;
        int index;
        Obtain o = new Obtain(this, key);

        while (filled >= 0) {
            index = o.next();
            if (index < 0 || index >= codeset.length) {
                System.out.println("uh, oh");
            }
            if (right) { // alternate going left or right to find the next open slot (keeps it from taking too long to
                         // hit something)
                for (int j = index; j < end; ++j) {
                    if (seq[j] == 0) {
                        seq[j] = codeset[filled];
                        --filled;
                        break;
                    }
                }
                right = false;
            } else {
                for (int j = index; j >= 0; --j) {
                    if (seq[j] == 0) {
                        seq[j] = codeset[filled];
                        --filled;
                        break;
                    }
                }
                right = true;
            }
        }
        Symm newSymm = new Symm(seq, this);
        newSymm.name = "from bytes";
        // Set the KeyBytes
        try {
            newSymm.keyBytes = new byte[AES.AES_KEY_SIZE / 8];
            int offset = (Math.abs(key[(47 % key.length)]) + 137) % (key.length - newSymm.keyBytes.length);
            for (int i = 0; i < newSymm.keyBytes.length; ++i) {
                newSymm.keyBytes[i] = key[i + offset];
            }
        } catch (Exception e) {
            throw new IOException(e);
        }

        return newSymm;
    }

    /**
     * This Symm is generated for internal JVM use. It has no external keyfile, but can be used for securing Memory, as
     * it remains the same ONLY of the current JVM
     * 
     * @return
     * @throws IOException
     */
    public static synchronized Symm internalOnly() throws IOException {
        if (internalOnly == null) {
            ByteArrayInputStream baos = new ByteArrayInputStream(keygen());
            try {
                internalOnly = Symm.obtain(baos);
            } finally {
                baos.close();
            }
        }
        return internalOnly;
    }

    @Override
    public String toString() {
        return name;
    }
}
