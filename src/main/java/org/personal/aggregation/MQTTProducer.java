package org.personal.aggregation;

import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.personal.utils.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.TransferQueue;

public class MQTTProducer extends Producer implements MqttCallback{
    private final Logger LOGGER = LoggerFactory.getLogger(MQTTProducer.class);

    public MQTTProducer(TransferQueue<LogEntry> queue, String name, Deserializer deserializer) {
        super(queue, name, deserializer);
    }

    @Override
    public Optional<LogEntry> deserialize(String serializedBodyString) {
        try {
            return Optional.ofNullable(this.deserializer.deserialize(serializedBodyString));
        } catch (IOException e) {
            this.LOGGER.error("{}:  Encountered unrecoverable error -> \n {}", this.getName(), e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public void disconnected(MqttDisconnectResponse mqttDisconnectResponse) {
        this.LOGGER.error("{}:   Lost connection with server! \n {}", this.getName(), mqttDisconnectResponse.getReasonString());
    }

    @Override
    public void mqttErrorOccurred(MqttException e) {
        this.LOGGER.error("{}:  Error -> \n {}", this.getName(), e.getMessage());
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        byte[] payload = mqttMessage.getPayload();
        String serializedMessage = new String(payload, StandardCharsets.UTF_8);
        try {
            Optional<LogEntry> deserializedMessage = this.deserialize(serializedMessage);
            if (deserializedMessage.isPresent()) {
                produce(deserializedMessage.get());
            } else {
                this.LOGGER.warn("{}: Log body is empty!", this.getName());
            }
        } catch (InterruptedException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public void deliveryComplete(IMqttToken iMqttToken) {
        this.LOGGER.info("{}: Delivery callback triggered", this.getName());
    }

    @Override
    public void connectComplete(boolean b, String s) {
        this.LOGGER.info("{}: Connected to MQTT broker", this.getName());
    }

    @Override
    public void authPacketArrived(int i, MqttProperties mqttProperties) {
        this.LOGGER.info("{}: authPacketArrived callback triggered", this.getName());
    }
}
