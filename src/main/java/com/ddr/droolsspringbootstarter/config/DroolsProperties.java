package com.ddr.droolsspringbootstarter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "drools")
@Data
@Component
public class DroolsProperties {
    private String rulePath = "rules/";
}
