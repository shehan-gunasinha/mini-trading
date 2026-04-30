package com.trading.orderservice.config;

import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.spring.embedded.provider.SpringEmbeddedCacheManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

@EnableCaching
@Configuration
public class InfinispanCacheConfig {

    @Value("${cache.orders.max-entries}")
    private long ordersMaxEntries;

    @Value("${cache.orders.ttl-seconds}")
    private long ordersTtl;

    @Value("${cache.orders-list.max-entries}")
    private long ordersListMaxEntries;

    @Value("${cache.orders-list.ttl-seconds}")
    private long ordersListTtl;

    @Bean
    public EmbeddedCacheManager embeddedCacheManager() {

        org.infinispan.configuration.global.GlobalConfiguration globalConfig =
                new GlobalConfigurationBuilder()
                        .nonClusteredDefault()
                        .build();

        org.infinispan.configuration.cache.Configuration ordersCache =
                new ConfigurationBuilder()
                        .clustering().cacheMode(CacheMode.LOCAL)
                        .memory()
                        .maxCount(ordersMaxEntries)
                        .expiration()
                        .lifespan(ordersTtl, TimeUnit.SECONDS)
                        .maxIdle(120, TimeUnit.SECONDS)
                        .statistics()
                        .enabled(true)
                        .build();

        org.infinispan.configuration.cache.Configuration ordersListCache =
                new ConfigurationBuilder()
                        .clustering().cacheMode(CacheMode.LOCAL)
                        .memory()
                        .maxCount(ordersListMaxEntries)
                        .expiration()
                        .lifespan(ordersListTtl, TimeUnit.SECONDS)
                        .maxIdle(30, TimeUnit.SECONDS)
                        .statistics()
                        .enabled(true)
                        .build();

        DefaultCacheManager cacheManager = new DefaultCacheManager(globalConfig);
        cacheManager.defineConfiguration("orders", ordersCache);
        cacheManager.defineConfiguration("orders-list", ordersListCache);

        return cacheManager;
    }

    // ✅ @Primary tells Spring: when multiple CacheManagers exist, use THIS one
    @Primary
    @Bean
    public CacheManager cacheManager(EmbeddedCacheManager embeddedCacheManager) {
        return new SpringEmbeddedCacheManager(embeddedCacheManager);
    }
}