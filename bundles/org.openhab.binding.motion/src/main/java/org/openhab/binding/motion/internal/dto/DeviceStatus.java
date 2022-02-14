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
 * The {@link DeviceStatus} model representing the status of a device.
 *
 * @author Jan Wendland - Initial contribution
 */

@NonNullByDefault
public class DeviceStatus {
    public int type = -1;
    public int operation = -1;
    public int currentPosition = -1;
    public int currentAngle = -1;
    public int currentState = -1;
    public int voltageMode = -1;
    public int batteryLevel = -1;
    public int wirelessMode = -1;
    @SerializedName("RSSI")
    public int rssi = -1;
}
