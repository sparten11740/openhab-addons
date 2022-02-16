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
package org.openhab.binding.motion.internal.communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MulticastListener} listens to a multicast group and notifies subscribers.
 *
 * @author Jan Wendland - Initial contribution
 */
@NonNullByDefault
public class MulticastListener {

    @Nullable
    private MulticastSocket socket;

    private final List<Consumer<String>> consumers = new ArrayList<>();
    private final InetAddress group;
    private final int port;

    private final byte[] buffer = new byte[20480];

    private final ScheduledExecutorService scheduler;

    private final Logger logger = LoggerFactory.getLogger(MulticastListener.class);

    private boolean listening = true;

    public MulticastListener(String address, int port, ScheduledExecutorService scheduler) throws UnknownHostException {
        this.scheduler = scheduler;
        this.group = Inet4Address.getByName(address);
        this.port = port;
    }

    public Runnable subscribe(Consumer<String> callback) {
        synchronized (MulticastListener.class) {
            consumers.add(callback);

            if (consumers.size() == 1) {
                listen();
            }

            return () -> {
                consumers.remove(callback);

                if (consumers.isEmpty()) {
                    stopListening();
                }
            };
        }
    }

    public void stopListening() {
        synchronized (MulticastSocket.class) {
            this.listening = false;
            if (!socket.isClosed()) {
                try {
                    socket.leaveGroup(group);
                } catch (IOException ignore) {
                }
                socket.close();
            }
            logger.debug("Stopped listening on UDP multicasts at {}:{}", group, port);
        }
    }

    private void listen() {
        try {
            socket = new MulticastSocket(port);
            socket.joinGroup(group);
            logger.debug("Starting to listen on UDP multicasts at {}:{}", group, port);
        } catch (IOException e) {
            logger.error("Failed to open multicast socket: {}", e.getMessage());
            return;
        }

        scheduler.execute(() -> {
            while (this.listening) {
                try {
                    DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                    socket.receive(response);

                    consumers.forEach(
                            consume -> consume.accept(new String(response.getData(), 0, response.getLength())));
                } catch (IOException e) {
                    logger.debug("Failed to retrieve multicast {}", e.getMessage());
                }
            }
        });
    }
}
