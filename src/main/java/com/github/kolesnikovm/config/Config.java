package com.github.kolesnikovm.config;

import java.util.List;

public class Config {

    private MQConfig mqConfig;
    private List<Service> services;

    public MQConfig getMqConfig() {
        return mqConfig;
    }

    public void setMqConfig(MQConfig mqConfig) {
        this.mqConfig = mqConfig;
    }

    public List<Service> getServices() {
        return services;
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }
}
