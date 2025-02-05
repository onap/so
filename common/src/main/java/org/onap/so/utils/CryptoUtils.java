/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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


import org.onap.so.logger.LoggingAnchor;
import org.onap.so.logging.filter.base.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;


/**
 * CryptoUtils adapted from RTTP client.
 * 
 */
public final class CryptoUtils {

    private static final Logger logger = LoggerFactory.getLogger(CryptoUtils.class);


    private static final String AES = "AES";
    private static final String CLOUD_KEY = "aa3871669d893c7fb8abbcda31b88b4f";
    private static final int GCM_TAG_LENGTH = 16;
    private static final int GCM_IV_LENGTH = 12;
    private static final String AES_GCM_NO_PADDING = "AES/GCM/NoPadding";

    /**
     * encrypt a value and generate a keyfile if the keyfile is not found then a new one is created
     * 
     * @throws GeneralSecurityException
     */
    public static String encrypt(String value, String keyString) throws GeneralSecurityException {
        SecretKeySpec sks = getSecretKeySpec(keyString);
        Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
        byte[] initVector = new byte[GCM_IV_LENGTH];
        (new SecureRandom()).nextBytes(initVector);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * java.lang.Byte.SIZE, initVector);
        cipher.init(Cipher.ENCRYPT_MODE, sks, spec);
        byte[] encoded = value.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] cipherText = new byte[initVector.length + cipher.getOutputSize(encoded.length)];
        System.arraycopy(initVector, 0, cipherText, 0, initVector.length);
        cipher.doFinal(encoded, 0, encoded.length, cipherText, initVector.length);
        return byteArrayToHexString(cipherText);
    }

    /**
     * decrypt a value
     * 
     * @throws GeneralSecurityException
     */
    public static String decrypt(String message, String keyString) throws GeneralSecurityException {
        if (message.equals(System.getenv("PLAINTEXTPASSWORD")))
            return message;
        SecretKeySpec sks = getSecretKeySpec(keyString);
        byte[] cipherText = hexStringToByteArray(message);
        Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
        byte[] initVector = Arrays.copyOfRange(cipherText, 0, GCM_IV_LENGTH);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * java.lang.Byte.SIZE, initVector);
        cipher.init(Cipher.DECRYPT_MODE, sks, spec);
        byte[] plaintext = cipher.doFinal(cipherText, GCM_IV_LENGTH, cipherText.length - GCM_IV_LENGTH);
        return new String(plaintext);
    }

    public static String encryptCloudConfigPassword(String message) {
        try {
            return CryptoUtils.encrypt(message, CLOUD_KEY);
        } catch (GeneralSecurityException e) {
            logger.error(LoggingAnchor.THREE, MessageEnum.RA_GENERAL_EXCEPTION.toString(),
                    ErrorCode.BusinessProcessError.getValue(), "Exception in encryptPassword ", e);
            return null;
        }
    }

    public static String decryptCloudConfigPassword(String message) {
        try {
            return CryptoUtils.decrypt(message, CLOUD_KEY);
        } catch (GeneralSecurityException e) {
            logger.error(LoggingAnchor.THREE, MessageEnum.RA_GENERAL_EXCEPTION.toString(),
                    ErrorCode.BusinessProcessError.getValue(), "Exception in encryptPassword ", e);
            return null;
        }
    }

    private static SecretKeySpec getSecretKeySpec(String keyString) {
        byte[] key = hexStringToByteArray(keyString);
        return new SecretKeySpec(key, AES);
    }

    public static String byteArrayToHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte aB : b) {
            int v = aB & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString().toUpperCase();
    }

    private static byte[] hexStringToByteArray(String s) {
        byte[] b = new byte[s.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(s.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }
}
