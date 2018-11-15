package com.expiryqueue.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.expiryqueue.service.ExpiryQueueServiceImpl.EXPIRY_MAP;

/**
 * Hazelcast configuration for Spring
 */
@Configuration
public class HazelcastConfig {

    private static final String HAZELCAST_INSTANCE = "hazelcast-instance";

    @Bean
    public Config hazelcast(){
        return new Config()
                .setInstanceName(HAZELCAST_INSTANCE)
                .addMapConfig(new MapConfig()
                        .setName(EXPIRY_MAP)
                        .setBackupCount(1)
                        .setStatisticsEnabled(true));
    }
}