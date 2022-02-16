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
package org.openhab.binding.motion.internal.things.blind;

import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.motion.internal.models.DeviceStatus;
import org.openhab.binding.motion.internal.models.DeviceStatusResponse;
import org.openhab.binding.motion.internal.things.StatusSubscriber;

/**
 * The {@link BlindStatusSubscriber}
 *
 * @author Jan Wendland - Initial contribution
 */
@NonNullByDefault
public class BlindStatusSubscriber implements StatusSubscriber {

    private final String macAddress;
    private Consumer<DeviceStatus> consumer;

    public BlindStatusSubscriber(String macAddress, Consumer<DeviceStatus> consumer) {
        this.macAddress = macAddress;
        this.consumer = consumer;
    }

    @Override
    public boolean accepts(DeviceStatusResponse message) {
        return message.macAddress.equals(macAddress);
    }

    @Override
    public void consume(DeviceStatus deviceStatus) {
        consumer.accept(deviceStatus);
    }
}
