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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.onap.so.security.cadi.Symm.Encryption;
import org.onap.so.security.cadi.util.Chmod;


/**
 * AES Class wraps Cipher AES, 128 NOTE: While not explicitly stated in JavaDocs, Ciphers AND SecretKeySpecs are NOT
 * ThreadSafe Ciphers take time to create, therefore, we have pooled them.
 *
 * @author Jonathan
 *
 */
public class AES implements Encryption {
    public static final String AES = AES.class.getSimpleName();
    public static final int AES_KEY_SIZE = 128; // 256 isn't supported on all JDKs.

    private SecretKeySpec aeskeySpec;

    public static SecretKey newKey() throws NoSuchAlgorithmException {
        KeyGenerator kgen = KeyGenerator.getInstance(AES);
        kgen.init(AES_KEY_SIZE);
        return kgen.generateKey();
    }

    public AES(byte[] aeskey, int offset, int len) {
        aeskeySpec = new SecretKeySpec(aeskey, offset, len, AES);
    }

    public byte[] encrypt(byte[] in) throws CadiException {
        try {
            Cipher c = Cipher.getInstance(AES);
            c.init(Cipher.ENCRYPT_MODE, aeskeySpec);
            return c.doFinal(in);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException
                | NoSuchPaddingException e) {
            throw new CadiException(e);
        }
    }

    public byte[] decrypt(byte[] in) throws CadiException {
        try {
            Cipher c = Cipher.getInstance(AES);
            c.init(Cipher.DECRYPT_MODE, aeskeySpec);
            return c.doFinal(in);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException
                | NoSuchPaddingException e) {
            throw new CadiException(e);
        }
    }

    public void save(File keyfile) throws IOException {
        FileOutputStream fis = new FileOutputStream(keyfile);
        try {
            fis.write(aeskeySpec.getEncoded());
        } finally {
            fis.close();
        }
        Chmod.to400.chmod(keyfile);
    }

    public CipherOutputStream outputStream(OutputStream os, boolean encrypt) {
        try {
            Cipher c = Cipher.getInstance(AES);
            if (encrypt) {
                c.init(Cipher.ENCRYPT_MODE, aeskeySpec);
            } else {
                c.init(Cipher.DECRYPT_MODE, aeskeySpec);
            }
            return new CipherOutputStream(os, c);
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            // Cannot add Exception to this API. throw Runtime
            System.err.println("Error creating Aes CipherOutputStream");
            return null; // should never get here.
        }
    }

    public CipherInputStream inputStream(InputStream is, boolean encrypt) {
        try {
            Cipher c = Cipher.getInstance(AES);
            if (encrypt) {
                c.init(Cipher.ENCRYPT_MODE, aeskeySpec);
            } else {
                c.init(Cipher.DECRYPT_MODE, aeskeySpec);
            }
            return new CipherInputStream(is, c);
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            // Cannot add Exception to this API. throw Runtime
            System.err.println("Error creating Aes CipherInputStream");
            return null; // should never get here.
        }
    }
}
