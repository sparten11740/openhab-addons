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

import static org.openhab.binding.motion.internal.MotionBindingConstants.CHANNEL_CONTROL;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.motion.internal.communication.Client;
import org.openhab.binding.motion.internal.models.Device;
import org.openhab.binding.motion.internal.models.DeviceStatusResponse;
import org.openhab.binding.motion.internal.things.bridge.BridgeConfiguration;
import org.openhab.binding.motion.internal.things.bridge.BridgeHandler;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BlindHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jan Wendland - Initial contribution
 */
@NonNullByDefault
public class BlindHandler extends BaseThingHandler {

    public static final int UPDATE_STATE_INTERVAL_SECONDS = 60;
    private final Logger logger = LoggerFactory.getLogger(BlindHandler.class);

    @Nullable
    private Client client;
    @Nullable
    private Device blind;

    @Nullable
    private BridgeConfiguration bridgeConfiguration;

    @Nullable
    private BridgeHandler bridgeHandler;

    public BlindHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        String macAddress = getConfig().as(BlindConfiguration.class).macAddress;
        Bridge bridge = getBridge();

        if (bridge == null) {
            return;
        }

        BridgeHandler handler = (BridgeHandler) bridge.getHandler();
        this.bridgeHandler = handler;

        if (handler == null) {
            return;
        }

        bridgeConfiguration = handler.config;

        client = handler.getClient();

        if (client == null) {
            return;
        }

        scheduler.execute(() -> {
            try {
                Optional<Device> device = client.getDevice(macAddress);

                if (device.isEmpty()) {
                    updateStatus(ThingStatus.OFFLINE);
                    return;
                }

                blind = device.get();

                updateStatus(ThingStatus.ONLINE);
                synchronizeState();
            } catch (IOException e) {
                logger.error("Failed to communicate with blind: {}", e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        });
    }

    private @Nullable Instant receivedCommandAt;

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Blind got command: {} and ChannelUID: {} ", command.toFullString(),
                channelUID.getIdWithoutGroup());

        Device blind = this.blind;
        Client client = this.client;

        if (client == null || blind == null) {
            return;
        }

        try {
            receivedCommandAt = Instant.now();
            switch (channelUID.getIdWithoutGroup()) {
                case CHANNEL_CONTROL:
                    if (command instanceof PercentType) {
                        PercentType percent = ((PercentType) command);
                        client.moveTo(blind, percent);
                        break;
                    }

                    if (command instanceof RefreshType) {
                        DeviceStatusResponse response = client.status(blind);
                        updateState(CHANNEL_CONTROL,
                                QuantityType.valueOf(response.data.currentPosition, Units.PERCENT));
                        break;
                    }

                    if (command instanceof Number) {
                        client.moveTo(blind, (Number) command);
                        break;
                    }

                    if (command instanceof StopMoveType) {
                        StopMoveType stopMove = (StopMoveType) command;
                        if (StopMoveType.STOP.equals(stopMove)) {
                            client.stop(blind);
                            break;
                        }

                        logger.debug("Received unknown StopMove command MOVE");
                        break;
                    }

                    if (command instanceof UpDownType) {
                        UpDownType upDown = (UpDownType) command;
                        if (UpDownType.UP.equals(upDown)) {
                            client.up(blind);
                            break;
                        }

                        client.down(blind);
                        break;
                    }

                    logger.debug("Received unknown command {}", command.toFullString());
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            logger.debug("Failed to execute command {}: {}", command.toFullString(), e.getMessage());
        }
    }

    @Nullable
    private ScheduledFuture<?> stateUpdateJob;

    ;

    private void synchronizeState() {
        if (bridgeConfiguration != null && bridgeConfiguration.pushUpdates) {
            subscribeToStateUpdates();
            return;
        }

        stateUpdateJob = scheduler.scheduleWithFixedDelay(this::updateState, 10, UPDATE_STATE_INTERVAL_SECONDS,
                TimeUnit.SECONDS);
    }

    @Nullable
    private Runnable unsubscribeFromStateUpdates;

    private void subscribeToStateUpdates() {
        Device blind = this.blind;
        BridgeHandler handler = this.bridgeHandler;

        if (blind == null || handler == null) {
            return;
        }

        BlindStatusSubscriber subscriber = new BlindStatusSubscriber(blind.macAddress, (status) -> {
            logger.debug("Updating blind {} position to {}", blind.macAddress, status.currentPosition);
            updateState(CHANNEL_CONTROL, QuantityType.valueOf(status.currentPosition, Units.PERCENT));
        });
        unsubscribeFromStateUpdates = handler.subscribeToStateChanges(subscriber);
    }

    private void updateState() {
        synchronized (BlindHandler.class) {
            try {
                Client client = this.client;
                Device blind = this.blind;
                Instant receivedCommandAt = this.receivedCommandAt;

                if (client == null || blind == null) {
                    return;
                }

                if (receivedCommandAt != null && Duration.between(receivedCommandAt, Instant.now()).toSeconds() < 30) {
                    return;
                }

                DeviceStatusResponse response = client.status(blind);
                updateState(CHANNEL_CONTROL, QuantityType.valueOf(response.data.currentPosition, Units.PERCENT));
            } catch (Exception e) {
                logger.trace("Failed to synchronize state {}", e.getMessage());
            }
        }
    }

    @Override
    public void dispose() {
        if (stateUpdateJob != null) {
            stateUpdateJob.cancel(true);
        }

        if (unsubscribeFromStateUpdates != null) {
            unsubscribeFromStateUpdates.run();
        }

        super.dispose();
    }
}
