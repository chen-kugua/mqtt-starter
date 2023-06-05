package com.cpiwx.mqttstarter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mqtt")
@Data
public class MqttProperties {
    private String host;

    private String username;

    private String password;

    private String topic;

    private String clientId;
    /**
     * 单位为秒
     */
    private Integer timeout;
    /**
     * 单位为秒
     */
    private Integer keepAlive;
}
