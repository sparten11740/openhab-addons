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
package org.openhab.binding.motion.internal.models;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Message} describes the most basic form of message that the Motion hub
 * consumes.
 *
 * @author Jan Wendland - Initial contribution
 */

@NonNullByDefault
public class Message {

    public Message(MessageType type) {
        this.type = type;

        Instant instant = Instant.now();
        this.id = "" + instant.getEpochSecond() + instant.getNano();
    }

    @SerializedName("msgID")
    public String id;

    @SerializedName("msgType")
    public MessageType type;
}
