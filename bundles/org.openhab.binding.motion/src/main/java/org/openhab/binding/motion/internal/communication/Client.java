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

import static org.openhab.binding.motion.internal.MotionBindingConstants.PORT_RECEIVE;
import static org.openhab.binding.motion.internal.MotionBindingConstants.PORT_SEND;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.motion.internal.exceptions.EncryptionFailed;
import org.openhab.binding.motion.internal.models.Command;
import org.openhab.binding.motion.internal.models.CommandType;
import org.openhab.binding.motion.internal.models.Device;
import org.openhab.binding.motion.internal.models.DeviceControlMessage;
import org.openhab.binding.motion.internal.models.DeviceStatusResponse;
import org.openhab.binding.motion.internal.models.ListDevicesResponse;
import org.openhab.binding.motion.internal.models.Message;
import org.openhab.binding.motion.internal.models.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link Client} implements client functions to communicate with the Motion hub.
 *
 * @author Jan Wendland - Initial contribution
 */
@NonNullByDefault
public class Client {
    private final DatagramSocket socket;
    private final InetAddress serverAddress;
    private final Encryptor encryptor;

    private final Gson gson = new GsonBuilder().create();

    private final Logger logger = LoggerFactory.getLogger(Client.class);

    public Client(String hostname, Encryptor encryptor) throws SocketException, UnknownHostException {
        socket = new DatagramSocket(null);
        socket.setReuseAddress(true);
        socket.setSoTimeout(5000);

        SocketAddress socketAddress = new InetSocketAddress(PORT_RECEIVE);
        socket.bind(socketAddress);

        serverAddress = InetAddress.getByName(hostname);
        this.encryptor = encryptor;
    }

    public List<Device> getDevices() throws IOException {
        Message message = new Message(MessageType.LIST_DEVICES);
        ListDevicesResponse response = request(message, ListDevicesResponse.class);

        return response.data;
    }

    public Optional<Device> getDevice(String macAddress) throws IOException {
        List<Device> devices = getDevices();
        return devices.stream().filter(device -> device.macAddress.equals(macAddress)).findFirst();
    }

    public DeviceStatusResponse up(Device device) throws IOException, EncryptionFailed {
        return sendCommand(device, Command.fromType(CommandType.UP));
    }

    public DeviceStatusResponse down(Device device) throws IOException, EncryptionFailed {
        return sendCommand(device, Command.fromType(CommandType.DOWN));
    }

    public DeviceStatusResponse status(Device device) throws IOException, EncryptionFailed {
        synchronized (this) {
            return sendCommand(device, Command.fromType(CommandType.STATUS));
        }
    }

    public DeviceStatusResponse stop(Device device) throws IOException, EncryptionFailed {
        return sendCommand(device, Command.fromType(CommandType.STOP));
    }

    public DeviceStatusResponse moveTo(Device device, Number targetPosition) throws IOException, EncryptionFailed {
        return sendCommand(device, Command.fromTargetPosition(targetPosition));
    }

    private DeviceStatusResponse sendCommand(Device device, Command command) throws IOException, EncryptionFailed {
        DeviceControlMessage message = new DeviceControlMessage(device);

        message.command = command;
        message.token = getAccessToken();
        logger.debug("Sending message {}", gson.toJson(message));

        DeviceStatusResponse response = request(message, DeviceStatusResponse.class);

        logger.debug("Received response {}", gson.toJson(response));
        return response;
    }

    private String getAccessToken() throws IOException, EncryptionFailed {
        Message message = new Message(MessageType.LIST_DEVICES);
        ListDevicesResponse response = request(message, ListDevicesResponse.class);

        try {
            return encryptor.encrypt(response.token);
        } catch (Exception e) {
            throw new EncryptionFailed(e.getMessage());
        }
    }

    private <T> T request(Object message, Class<T> responseClass) throws IOException {
        byte[] buffer = gson.toJson(message).getBytes(StandardCharsets.UTF_8);

        DatagramPacket request = new DatagramPacket(buffer, buffer.length, serverAddress, PORT_SEND);
        socket.send(request);

        byte[] responseBuffer = new byte[20480];
        DatagramPacket response = new DatagramPacket(responseBuffer, responseBuffer.length);
        socket.receive(response);

        return gson.fromJson(new String(response.getData(), 0, response.getLength()), responseClass);
    }

    public void dispose() {
        socket.close();
    }
}
