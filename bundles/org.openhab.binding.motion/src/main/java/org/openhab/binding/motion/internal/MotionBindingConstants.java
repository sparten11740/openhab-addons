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
package org.openhab.binding.motion.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MotionBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jan Wendland - Initial contribution
 */
@NonNullByDefault
public class MotionBindingConstants {

    public static final String BINDING_ID = "motion";

    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_BLIND = new ThingTypeUID(BINDING_ID, "blind");

    public static final String CHANNEL_CONTROL = "control";

    public static final String BRIDGE_TYPE_ID = "02000002";

    public static final int PORT_SEND = 32100;
    public static final int PORT_RECEIVE = 32101;

    public static final String MULTICAST_ADDRESS = "238.0.0.18";
}
