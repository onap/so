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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import org.onap.so.security.cadi.util.Chmod;
import org.onap.so.security.cadi.util.JsonOutputStream;



/**
 * A Class to run on command line to determine suitability of environment for certain TAFs. *
 * 
 * @author Jonathan
 *
 */
public class CmdLine {

    private static boolean systemExit = true;

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            if ("digest".equalsIgnoreCase(args[0])
                    && (args.length > 2 || (args.length > 1 && System.console() != null))) {
                String keyfile;
                String password;
                if (args.length > 2) {
                    password = args[1];
                    keyfile = args[2];
                    if ("-i".equals(password)) {
                        int c;
                        StringBuilder sb = new StringBuilder();
                        try {
                            while ((c = System.in.read()) >= 0) {
                                sb.append((char) c);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        password = sb.toString();
                    }
                } else {
                    keyfile = args[1];
                    password = new String(System.console().readPassword("Type here (keystrokes hidden): "));
                }

                try {
                    Symm symm;
                    FileInputStream fis = new FileInputStream(keyfile);
                    try {
                        symm = Symm.obtain(fis);
                    } finally {
                        fis.close();
                    }
                    symm.enpass(password, System.out);
                    System.out.println();
                    System.out.flush();
                    return;
                    /*
                     * testing code... don't want it exposed System.out.println(" ******** Testing *********"); for (int
                     * i=0;i<100000;++i) { System.out.println(args[1]); ByteArrayOutputStream baos = new
                     * ByteArrayOutputStream(); b64.enpass(args[1], baos); String pass; System.out.println(pass=new
                     * String(baos.toByteArray())); ByteArrayOutputStream reconstituted = new ByteArrayOutputStream();
                     * b64.depass(pass, reconstituted); String r = reconstituted.toString(); System.out.println(r); if
                     * (!r.equals(args[1])) { System.err.println("!!!!! STOP - ERROR !!!!!"); return; }
                     * System.out.println(); } System.out.flush();
                     */

                } catch (IOException e) {
                    System.err.println("Cannot digest password");
                    System.err.println("   \"" + e.getMessage() + '"');
                }
                // DO NOT LEAVE THIS METHOD Compiled IN CODE... Do not want looking at passwords on disk too easy
                // Jonathan. Oh, well, Deployment services need this behavior. I will put this code in, but leave it
                // undocumented.
                // One still needs access to the keyfile to read.
                // July 2016 - thought of a tool "CMPass" to regurgitate from properties, but only if allowed.
            } else if (("regurgitate".equalsIgnoreCase(args[0]) || "undigest".equalsIgnoreCase(args[0]))
                    && args.length > 2) {
                try {
                    Symm symm;
                    FileInputStream fis = new FileInputStream(args[2]);
                    try {
                        symm = Symm.obtain(fis);
                    } finally {
                        fis.close();
                    }
                    boolean isFile = false;
                    if ("-i".equals(args[1]) || (isFile = "-f".equals(args[1]))) {
                        BufferedReader br;
                        if (isFile) {
                            if (args.length < 4) {
                                System.err.println("Filename in 4th position");
                                return;
                            }
                            br = new BufferedReader(new FileReader(args[3]));
                        } else {
                            br = new BufferedReader(new InputStreamReader(System.in));
                        }
                        try {
                            String line;
                            boolean cont = false;
                            StringBuffer sb = new StringBuffer();
                            JsonOutputStream jw = new JsonOutputStream(System.out);
                            while ((line = br.readLine()) != null) {
                                if (cont) {
                                    int end;
                                    if ((end = line.indexOf('"')) >= 0) {
                                        sb.append(line, 0, end);
                                        cont = false;
                                    } else {
                                        sb.append(line);
                                    }
                                } else {
                                    int idx;
                                    if ((idx = line.indexOf(' ')) >= 0 && (idx = line.indexOf(' ', ++idx)) > 0
                                            && (idx = line.indexOf('=', ++idx)) > 0) {
                                        System.out.println(line.substring(0, idx - 5));
                                        int start = idx + 2;
                                        int end;
                                        if ((end = line.indexOf('"', start)) < 0) {
                                            end = line.length();
                                            cont = true;
                                        }
                                        sb.append(line, start, end);
                                    }
                                }
                                if (sb.length() > 0) {
                                    symm.depass(sb.toString(), jw);
                                    if (!cont) {
                                        System.out.println();
                                    }
                                }
                                System.out.flush();
                                sb.setLength(0);
                                if (!cont) {
                                    jw.resetIndent();
                                }
                            }
                        } finally {
                            if (isFile) {
                                br.close();
                            }
                        }
                    } else {
                        symm.depass(args[1], System.out);
                    }
                    System.out.println();
                    System.out.flush();
                    return;
                } catch (IOException e) {
                    System.err.println("Cannot undigest password");
                    System.err.println("   \"" + e.getMessage() + '"');
                }
            } else if ("encode64".equalsIgnoreCase(args[0]) && args.length > 1) {
                try {
                    Symm.base64.encode(args[1], System.out);
                    System.out.println();
                    System.out.flush();
                    return;
                } catch (IOException e) {
                    System.err.println("Cannot encode Base64 with " + args[1]);
                    System.err.println("   \"" + e.getMessage() + '"');
                }
            } else if ("decode64".equalsIgnoreCase(args[0]) && args.length > 1) {
                try {
                    Symm.base64.decode(args[1], System.out);
                    System.out.println();
                    System.out.flush();
                    return;
                } catch (IOException e) {
                    System.err.println("Cannot decode Base64 text from " + args[1]);
                    System.err.println("   \"" + e.getMessage() + '"');
                }
            } else if ("encode64url".equalsIgnoreCase(args[0]) && args.length > 1) {
                try {
                    Symm.base64url.encode(args[1], System.out);
                    System.out.println();
                    System.out.flush();
                    return;
                } catch (IOException e) {
                    System.err.println("Cannot encode Base64url with " + args[1]);
                    System.err.println("   \"" + e.getMessage() + '"');
                }
            } else if ("decode64url".equalsIgnoreCase(args[0]) && args.length > 1) {
                try {
                    Symm.base64url.decode(args[1], System.out);
                    System.out.println();
                    System.out.flush();
                    return;
                } catch (IOException e) {
                    System.err.println("Cannot decode Base64url text from " + args[1]);
                    System.err.println("   \"" + e.getMessage() + '"');
                }
            } else if ("md5".equalsIgnoreCase(args[0]) && args.length > 1) {
                try {
                    System.out.println(Hash.hashMD5asStringHex(args[1]));
                    System.out.flush();
                } catch (NoSuchAlgorithmException e) {
                    System.err.println("Cannot hash MD5 from " + args[1]);
                    System.err.println("   \"" + e.getMessage() + '"');
                }
                return;
            } else if ("sha256".equalsIgnoreCase(args[0]) && args.length > 1) {
                try {
                    if (args.length > 2) {
                        int max = args.length > 7 ? 7 : args.length;
                        for (int i = 2; i < max; ++i) {
                            int salt = Integer.parseInt(args[i]);
                            System.out.println(Hash.hashSHA256asStringHex(args[1], salt));
                        }
                    } else {
                        System.out.println(Hash.hashSHA256asStringHex(args[1]));
                    }
                } catch (NoSuchAlgorithmException e) {
                    System.err.println("Cannot hash SHA256 text from " + args[1]);
                    System.err.println("   \"" + e.getMessage() + '"');
                }
                System.out.flush();
                return;
            } else if ("keygen".equalsIgnoreCase(args[0])) {
                try {
                    if (args.length > 1) {
                        File f = new File(args[1]);
                        FileOutputStream fos = new FileOutputStream(f);
                        try {
                            fos.write(Symm.keygen());
                            fos.flush();
                        } finally {
                            fos.close();
                            Chmod.to400.chmod(f);
                        }
                    } else {
                        // create a Symmetric Key out of same characters found in base64
                        System.out.write(Symm.keygen());
                        System.out.flush();
                    }
                    return;
                } catch (IOException e) {
                    System.err.println("Cannot create a key " + args[0]);
                    System.err.println("   \"" + e.getMessage() + '"');
                }

            } else if ("passgen".equalsIgnoreCase(args[0])) {
                int numDigits;
                if (args.length <= 1) {
                    numDigits = 24;
                } else {
                    numDigits = Integer.parseInt(args[1]);
                    if (numDigits < 8)
                        numDigits = 8;
                }
                String pass;
                boolean noLower, noUpper, noDigits, noSpecial, repeatingChars, missingChars;
                do {
                    pass = Symm.randomGen(numDigits);
                    missingChars = noLower = noUpper = noDigits = noSpecial = true;
                    repeatingChars = false;
                    int c = -1, last;
                    for (int i = 0; i < numDigits; ++i) {
                        last = c;
                        c = pass.charAt(i);
                        if (c == last) {
                            repeatingChars = true;
                            break;
                        }
                        if (noLower) {
                            noLower = !(c >= 0x61 && c <= 0x7A);
                        }
                        if (noUpper) {
                            noUpper = !(c >= 0x41 && c <= 0x5A);
                        }
                        if (noDigits) {
                            noDigits = !(c >= 0x30 && c <= 0x39);
                        }
                        if (noSpecial) {
                            noSpecial = "+!@#$%^&*(){}[]?:;,.".indexOf(c) < 0;
                        }

                        missingChars = (noLower || noUpper || noDigits || noSpecial);
                    }
                } while (missingChars || repeatingChars);
                System.out.println(pass.substring(0, numDigits));
            } else if ("urlgen".equalsIgnoreCase(args[0])) {
                int numDigits;
                if (args.length <= 1) {
                    numDigits = 24;
                } else {
                    numDigits = Integer.parseInt(args[1]);
                }
                System.out.println(Symm.randomGen(Symm.base64url.codeset, numDigits).substring(0, numDigits));
            }
        } else {
            System.out.println("Usage: java -jar <this jar> ...");
            System.out.println("  keygen [<keyfile>]                     (Generates Key on file, or Std Out)");
            System.out.println("  digest [<passwd>|-i|] <keyfile>        (Encrypts Password with \"keyfile\"");
            System.out.println("                                          if passwd = -i, will read StdIn");
            System.out.println("                                          if passwd is blank, will ask securely)");
            System.out.println("  undigest <enc:...> <keyfile>           (Decrypts Encoded with \"keyfile\")");
            System.out.println("  passgen <digits>                       (Generate Password of given size)");
            System.out.println("  urlgen <digits>                        (Generate URL field of given size)");
            System.out.println("  encode64 <your text>                   (Encodes to Base64)");
            System.out.println("  decode64 <base64 encoded text>         (Decodes from Base64)");
            System.out.println("  encode64url <your text>                (Encodes to Base64 URL charset)");
            System.out.println("  decode64url <base64url encoded text>   (Decodes from Base64 URL charset)");
            System.out.println("  sha256 <text> <salts(s)>               (Digest String into SHA256 Hash)");
            System.out.println("  md5 <text>                             (Digest String into MD5 Hash)");
        }
        if (systemExit) {
            System.exit(1);
        }
    }

    public static void setSystemExit(boolean shouldExit) {
        systemExit = shouldExit;
    }

}
