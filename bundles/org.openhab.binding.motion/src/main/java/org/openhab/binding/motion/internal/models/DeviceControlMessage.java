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

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link DeviceControlMessage} describes a message to execute a command on a connected device
 * through the Motion hub.
 *
 * @author Jan Wendland - Initial contribution
 */

@NonNullByDefault
public class DeviceControlMessage extends Message {

    public DeviceControlMessage(Device device) {
        super(MessageType.UPDATE_DEVICE);
        deviceType = device.type;
        macAddress = device.macAddress;
    }

    @SerializedName("mac")
    public String macAddress;

    @SerializedName("AccessToken")
    public String token = "";

    public String deviceType;

    @SerializedName("data")
    public Command command = Command.fromType(CommandType.STATUS);
}
