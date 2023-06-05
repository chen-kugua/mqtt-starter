package com.cpiwx.mqttstarter;

import cn.hutool.core.util.StrUtil;
import com.cpiwx.mqttstarter.ano.EnableMqtt;
import com.cpiwx.mqttstarter.config.CustomMqttClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@SpringBootApplication
@RestController
@EnableMqtt
public class MqttStarterApplication {
    @Resource
    private CustomMqttClient customMqttClient;

    public static void main(String[] args) {
        SpringApplication.run(MqttStarterApplication.class, args);
    }

    @RequestMapping("/")
    public String index() {
        return "ok";
    }

    @RequestMapping("/send")
    public String send(String message) {
        if (StrUtil.isNotBlank(message)) {
            customMqttClient.publish(message, "/test/temp");
        }
        return message;
    }

}
