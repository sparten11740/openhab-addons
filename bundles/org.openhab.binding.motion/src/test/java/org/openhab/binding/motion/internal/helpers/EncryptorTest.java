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
package org.openhab.binding.motion.internal.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.motion.internal.communication.Encryptor;

/**
 * The {@link EncryptorTest} makes sure that the {@link Encryptor} complies to the spec as outlined in
 * the Motion integration guide.
 *
 * @author Jan Wendland - Initial contribution
 */

@NonNullByDefault
class EncryptorTest {

    @Test
    void shouldEncryptMessageAndOutputAsHex() {
        String secret = "74ae544c-d16e-4c";
        Encryptor encryptor = new Encryptor(secret);

        try {
            String encrypted = encryptor.encrypt("780DA5B400963266");
            assertEquals("52C6279F5B99C52DA68FE0851AA8962E", encrypted);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
