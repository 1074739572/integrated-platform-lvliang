package com.iflytek.integrated.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties("metrics")
public class MetricsConfig {
    private List<Metrics> list;

    @Data
    public static class Metrics {
        private String id;
        private String url;
    }
}
