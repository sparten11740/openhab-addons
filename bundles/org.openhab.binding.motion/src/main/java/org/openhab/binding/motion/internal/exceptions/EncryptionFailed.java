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
package org.openhab.binding.motion.internal.exceptions;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link EncryptionFailed} is thrown when encryption of the token fails
 *
 * @author Jan Wendland - Initial contribution
 */

@NonNullByDefault
public class EncryptionFailed extends Exception {
    private static final long serialVersionUID = 1L;

    public EncryptionFailed(@Nullable String message) {
        super(message);
    }
}
