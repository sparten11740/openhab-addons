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

import static org.openhab.binding.motion.internal.MotionBindingConstants.MULTICAST_ADDRESS;
import static org.openhab.binding.motion.internal.MotionBindingConstants.PORT_RECEIVE;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.motion.internal.MotionDiscoveryService;
import org.openhab.binding.motion.internal.communication.Client;
import org.openhab.binding.motion.internal.communication.Encryptor;
import org.openhab.binding.motion.internal.communication.MulticastListener;
import org.openhab.binding.motion.internal.models.DeviceStatusResponse;
import org.openhab.binding.motion.internal.things.StatusSubscriber;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link BridgeHandler} is responsible for communication with connected devices.
 *
 * @author Jan Wendland - Initial contribution
 */
@NonNullByDefault
public class BridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(BridgeHandler.class);
    private final Gson gson = new GsonBuilder().create();

    private @Nullable Client client;
    public @Nullable BridgeConfiguration config = getConfigAs(BridgeConfiguration.class);

    public BridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(MotionDiscoveryService.class);
    }

    public @Nullable Client getClient() {
        return client;
    }

    @Override
    public void initialize() {
        this.config = getConfigAs(BridgeConfiguration.class);

        String hostname = config.hostname.trim();
        if (hostname.isEmpty()) {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Required configuration parameter hostname missing");
            return;
        }

        String secret = config.secret.trim();
        if (secret.isEmpty()) {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Required configuration parameter secret missing");
            return;
        }

        scheduler.execute(() -> {
            try {
                client = new Client(hostname, new Encryptor(secret));
                client.getDevices();
                updateStatus(ThingStatus.ONLINE);
                synchronizeStates();
            } catch (IOException e) {
                logger.error("Failed to initialize motion client: {}", e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        });
    }

    private final List<StatusSubscriber> subscribers = new ArrayList<>();

    public Runnable subscribeToStateChanges(StatusSubscriber subscriber) {
        subscribers.add(subscriber);

        return () -> subscribers.remove(subscriber);
    }

    private @Nullable Runnable unsubscribeMulticastListener;

    private void synchronizeStates() {
        if (this.config != null && !this.config.pushUpdates) {
            return;
        }

        try {
            MulticastListener multicastListener = new MulticastListener(MULTICAST_ADDRESS, PORT_RECEIVE, scheduler);
            unsubscribeMulticastListener = multicastListener.subscribe(response -> {
                if (!response.contains("\"msgType\":\"Report\"")) {
                    return;
                }

                DeviceStatusResponse message = gson.fromJson(response, DeviceStatusResponse.class);

                if (message == null) {
                    return;
                }

                subscribers.stream().filter(subscriber -> subscriber.accepts(message))
                        .forEach(subscriber -> subscriber.consume(message.data));
            });
        } catch (UnknownHostException e) {
            logger.error("Failed to initialize multicast listener {}", e.getMessage());
        }
    }

    @Override
    public void dispose() {
        if (client != null) {
            client.dispose();
        }

        if (unsubscribeMulticastListener != null) {
            unsubscribeMulticastListener.run();
        }

        super.dispose();
    }
}
