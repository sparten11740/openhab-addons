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
package org.openhab.binding.motion.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link Command} model describing a command that can be send to the Motion hub.
 *
 * @author Jan Wendland - Initial contribution
 */

@NonNullByDefault
public class Command {
    public int operation;
    @Nullable
    public Number targetPosition;

    private Command() {
    }

    public static Command fromTargetPosition(Number targetPosition) {
        Command cmd = new Command();
        cmd.targetPosition = targetPosition;
        cmd.operation = 6;
        return cmd;
    }

    public static Command fromType(CommandType type) {
        Command cmd = new Command();
        cmd.operation = type.value;
        return cmd;
    }
}
