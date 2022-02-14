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

import com.google.gson.annotations.SerializedName;

/**
 * The {@link DeviceControlResponse} describes the response to a LIST_DEVICES message
 *
 * @author Jan Wendland - Initial contribution
 */

@NonNullByDefault
public class DeviceControlResponse {
    public DeviceStatus data = new DeviceStatus();

    @SerializedName("msgType")
    public MessageType messageType = MessageType.UPDATE_DEVICE_ACKNOWLEDGED;
}
