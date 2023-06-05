package com.cpiwx.mqttstarter.config;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.cpiwx.mqttstarter.service.MqttMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.annotation.Resource;

@EnableConfigurationProperties({MqttProperties.class})
@ConditionalOnClass(MqttClient.class)
@Slf4j
public class MqttAutoConfig {
    @Resource
    private MqttProperties mqttProperties;

    @Bean
    @ConditionalOnMissingBean(MqttClient.class)
    public MqttClient mqttClient() throws MqttException {
        String clientId = StrUtil.isNotBlank(mqttProperties.getClientId()) ? mqttProperties.getClientId() : IdUtil.simpleUUID();
        return new MqttClient(mqttProperties.getHost(), clientId, new MemoryPersistence());
    }


    @Bean
    @ConditionalOnMissingBean(MqttConnectOptions.class)
    public MqttConnectOptions mqttConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(mqttProperties.getUsername());
        options.setPassword(mqttProperties.getPassword().toCharArray());
        options.setConnectionTimeout(mqttProperties.getTimeout());
        options.setKeepAliveInterval(mqttProperties.getKeepAlive());
        // 设置客户端和服务器是否应在重新启动和重新连接时记住状态。
        // 如果设置为false，则客户端和服务器将在重新启动客户端、服务器和连接时保持状态。当状态保持时：
        // 即使重新启动客户端、服务器或连接，消息传递也将可靠地满足指定的QOS。
        // 服务器会将订阅视为持久订阅。
        // 如果设置为true，则客户端和服务器将不会在重新启动客户端、服务器或连接时保持状态。这意味着
        // 如果重新启动客户端、服务器或连接，则无法维持向指定QOS的消息传递
        // 服务器将订阅视为非持久订阅
        // 参数：
        // cleanSession–设置为True以启用cleanSession
        options.setCleanSession(false);
        // 自动重连
        options.setAutomaticReconnect(true);
        return options;
    }

    @Bean
    @ConditionalOnClass(MqttMessageHandler.class)
    @ConditionalOnMissingBean(MqttMessageHandler.class)
    public MqttMessageHandler mqttMessageHandler() {
        return (topic, message) -> log.info("messageHandler:{}--{}", topic, new String(message.getPayload()));
    }

    @Bean
    @ConditionalOnMissingBean(MqttCallback.class)
    public MqttCallback mqttCallback(MqttMessageHandler messageHandler, MqttClient mqttClient) {
        return new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                log.error("mqtt连接断开", cause);
                if (!mqttClient.isConnected()) {
                    log.info("重连...");
                    try {
                        mqttClient.reconnect();
                    } catch (MqttException e) {
                        log.error("重连失败...");
                    }
                }
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                log.info("topic:{}--收到消息:{}", topic, new String(message.getPayload()));
                messageHandler.handleMessage(topic, message);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                log.info("deliveryComplete...");
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(CustomMqttClient.class)
    public CustomMqttClient customMqttClient(MqttClient mqttClient, MqttConnectOptions options, MqttCallback callback) {
        log.info("初始化CustomMqttClient...");
        return new CustomMqttClient(mqttClient, options, mqttProperties, callback);
    }


}
