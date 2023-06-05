package com.cpiwx.mqttstarter.service;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public interface MqttMessageHandler {

    void handleMessage(String topic, MqttMessage message);
}
