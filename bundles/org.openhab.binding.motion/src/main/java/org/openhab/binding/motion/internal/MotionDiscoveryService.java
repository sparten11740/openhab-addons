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

import static org.openhab.binding.motion.internal.MotionBindingConstants.BRIDGE_TYPE_ID;
import static org.openhab.binding.motion.internal.MotionBindingConstants.THING_TYPE_BLIND;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.motion.internal.communication.Client;
import org.openhab.binding.motion.internal.things.bridge.BridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DiscoveryService} is responsible for discovering Motion devices connected to the hub.
 *
 * @author Jan Wendland - Initial contribution
 */
@NonNullByDefault
public class MotionDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {
    private static final int DISCOVER_TIMEOUT_SECONDS = 2;

    private final Logger logger = LoggerFactory.getLogger(MotionDiscoveryService.class);

    @Nullable
    private BridgeHandler bridgeHandler;

    public MotionDiscoveryService() throws IllegalArgumentException {
        super(Set.of(THING_TYPE_BLIND), DISCOVER_TIMEOUT_SECONDS, true);
    }

    // @formatter:off
    private static final Map<String, String> LABEL_BY_DEVICE_TYPE = Map.of(
            BRIDGE_TYPE_ID, "Motion Bridge",
            "10000000", "Roller Blind",
            "10000001", "Top Down Bottom Up Blind",
            "10000002", "Double Roller Blind"
    );
    // @formatter:on

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof BridgeHandler) {
            bridgeHandler = (BridgeHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    protected void startScan() {
        logger.debug("Started manual discovery");

        if (bridgeHandler == null) {
            return;
        }

        try {
            Client client = bridgeHandler.getClient();

            if (client == null) {
                return;
            }

            ThingUID bridgeUid = bridgeHandler.getThing().getUID();

            client.getDevices().stream().filter(device -> !device.type.equals(BRIDGE_TYPE_ID)).forEach(device -> {
                ThingUID uid = new ThingUID(THING_TYPE_BLIND, bridgeUid, device.macAddress);

                String label = LABEL_BY_DEVICE_TYPE.get(device.type);

                // @formatter:off
                DiscoveryResult thing = DiscoveryResultBuilder
                        .create(uid)
                        .withBridge(bridgeUid)
                        .withProperties(Map.of("macAddress", device.macAddress, "type", device.type))
                        .withLabel(label != null ? label : String.format("Unknown device %s", device.type))
                        .build();
                // @formatter:on

                thingDiscovered(thing);
            });
        } catch (IOException e) {
            logger.error("Error on discovery {}", e.getMessage());
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }
}
