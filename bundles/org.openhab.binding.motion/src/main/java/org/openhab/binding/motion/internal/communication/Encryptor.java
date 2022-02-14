/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.motion.internal.communication;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.jose4j.keys.AesKey;

/**
 * The {@link Encryptor} provides AES encryption as outlined in the Motion integration guide.
 *
 * @author Jan Wendland - Initial contribution
 */

@NonNullByDefault
public class Encryptor {

    private final SecretKey secretKey;

    public Encryptor(String secret) {
        this.secretKey = new AesKey(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String encrypt(String input) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, this.secretKey);
        byte[] cipherText = cipher.doFinal(input.getBytes());
        return bytesToHex(cipherText);
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
