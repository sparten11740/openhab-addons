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
package org.openhab.binding.motion.internal.things.bridge;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link BridgeConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Jan Wendland - Initial contribution
 */
@NonNullByDefault
public class BridgeConfiguration {
    public String hostname = "";
    public String secret = "";
    public Boolean pushUpdates = false;
}