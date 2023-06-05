package com.cpiwx.mqttstarter.config;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;


@Slf4j
public class CustomMqttClient {
    private MqttClient mqttClient;
    private MqttConnectOptions options;
    private MqttProperties properties;
    private MqttCallback mqttCallback;

    public CustomMqttClient(MqttClient mqttClient, MqttConnectOptions options, MqttProperties properties, MqttCallback mqttCallback) {
        this.mqttClient = mqttClient;
        this.options = options;
        this.properties = properties;
        this.mqttCallback = mqttCallback;
    }


    @PostConstruct
    public void init() {
        mqttClient.setCallback(mqttCallback);
        try {
            mqttClient.connect(options);
            String topic = properties.getTopic();
            if (StrUtil.isNotBlank(topic)) {
                String[] split = topic.split(StrUtil.COMMA);
                for (String t : split) {
                    this.subscribe(t, 0);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 订阅主题
     *
     * @param topic 主题
     * @param qos   QoS 0，最多交付一次。
     *              QoS 1，至少交付一次。
     *              QoS 2，只交付一次。
     */
    public boolean subscribe(String topic, int qos) {
        try {
            mqttClient.subscribe(topic, qos);
            return true;
        } catch (MqttException e) {
            log.error("订阅主题失败！", e);
        }
        return false;
    }

    /**
     * 取消订阅主题
     *
     * @param topic 主题名称
     */
    public void unsubscribe(String topic) {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.unsubscribe(topic);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        } else {
            log.error("取消订阅失败！");
        }
    }

    /**
     * 发布，默认qos为0，非持久化
     *
     * @param pushMessage 消息 内容
     * @param topic       主题
     */
    public void publish(String pushMessage, String topic) {
        publish(pushMessage, topic, 0, false);
    }

    /**
     * 发布消息
     *
     * @param pushMessage 消息内容
     * @param topic       主题
     * @param qos         QoS
     * @param retained:留存
     */
    public void publish(String pushMessage, String topic, int qos, boolean retained) {
        MqttMessage message = new MqttMessage();
        message.setPayload(pushMessage.getBytes());
        message.setQos(qos);
        message.setRetained(retained);
        MqttTopic mqttTopic = mqttClient.getTopic(topic);
        if (null == mqttTopic) {
            throw new RuntimeException("publish topic is not exist");
        }
        MqttDeliveryToken token;// Delivery:配送
        synchronized (this) {// 注意：这里一定要同步，否则，在多线程publish的情况下，线程会发生死锁，分析见文章最后补充
            try {
                token = mqttTopic.publish(message);// 也是发送到执行队列中，等待执行线程执行，将消息发送到消息中间件
                token.waitForCompletion(1000L);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    @PreDestroy
    public void destroy() {
        if (null != mqttClient) {
            String topic = properties.getTopic();
            if (StrUtil.isNotBlank(topic)) {
                String[] split = topic.split(StrUtil.COMMA);
                for (String t : split) {
                    try {
                        log.info("unsubscribe topic = {}", t);
                        mqttClient.unsubscribe(t);
                    } catch (Exception e) {
                        log.error("unsubscribing topic error", e);
                    }
                }
                try {
                    if (mqttClient.isConnected()) {
                        mqttClient.disconnect();
                    }
                    mqttClient.close();
                } catch (MqttException e) {
                    log.error("close mqtt client error", e);
                }
            }
        }
    }

}
