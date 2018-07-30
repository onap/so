/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.utils;



import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.onap.so.logger.MessageEnum;
import org.onap.so.logger.MsoLogger;


/**
 * CryptoUtils adapted from RTTP client.
 * 
 */
public final class CryptoUtils {

    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA, CryptoUtils.class);

    public static final String AES = "AES";
    private static final String CLOUD_KEY = "aa3871669d893c7fb8abbcda31b88b4f";
    /**
     * encrypt a value and generate a keyfile
     * if the keyfile is not found then a new one is created
     * 
     * @throws GeneralSecurityException
     */
    public static String encrypt (String value, String keyString) throws GeneralSecurityException {
        SecretKeySpec sks = getSecretKeySpec (keyString);
        Cipher cipher = Cipher.getInstance (CryptoUtils.AES);
        cipher.init (Cipher.ENCRYPT_MODE, sks, cipher.getParameters ());
        byte[] encrypted = cipher.doFinal (value.getBytes ());
        return byteArrayToHexString (encrypted);
    }

    /**
     * decrypt a value
     * 
     * @throws GeneralSecurityException
     */
    public static String decrypt (String message, String keyString) throws GeneralSecurityException {
        SecretKeySpec sks = getSecretKeySpec (keyString);
        Cipher cipher = Cipher.getInstance (CryptoUtils.AES);
        cipher.init (Cipher.DECRYPT_MODE, sks);
        byte[] decrypted = cipher.doFinal (hexStringToByteArray (message));
        return new String (decrypted);
    }

    /**
     * decrypt a value or return defaultValue
     * 
     */
    public static String decryptProperty (String prop, String defaultValue, String encryptionKey) {
		 try {
			 return CryptoUtils.decrypt(prop, encryptionKey);			
		 }	
		 catch (GeneralSecurityException e) {
			 LOGGER.debug("Security exception", e);
		 }
		 return defaultValue;
	}
    
    public static String encryptCloudConfigPassword(String message) {
    	try {
	    	return CryptoUtils.encrypt(message, CryptoUtils.CLOUD_KEY);
	    } catch (GeneralSecurityException e) {
	        LOGGER.error (MessageEnum.RA_GENERAL_EXCEPTION, "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Exception in encryptPassword", e);
	        return null;
	    }
    }
    public static String decryptCloudConfigPassword(String message) {
    	try {
	    	return CryptoUtils.decrypt(message, CryptoUtils.CLOUD_KEY);
	    } catch (GeneralSecurityException e) {
	        LOGGER.error (MessageEnum.RA_GENERAL_EXCEPTION, "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Exception in encryptPassword", e);
	        return null;
	    }
    }
    private static SecretKeySpec getSecretKeySpec (String keyString) throws NoSuchAlgorithmException {
        byte[] key = hexStringToByteArray (keyString);
        SecretKeySpec sks = new SecretKeySpec (key, CryptoUtils.AES);
        return sks;
    }

    public static String byteArrayToHexString (byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte aB : b) {
            int v = aB & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString ().toUpperCase ();
    }

    private static byte[] hexStringToByteArray (String s) {
        byte[] b = new byte[s.length () / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt (s.substring (index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }
}
