package com.huangqq.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
@ComponentScan(basePackages = "com.huangqq")
@PropertySource("classpath:config.properties")
public class BeanConfig {

    @Bean
    public JedisPoolConfig getJedisPoolConfig(@Value("${redis.maxTotal}") int maxTotal,
                                              @Value("${redis.maxIdle}") int maxIdle,
                                              @Value("${redis.minIdle}") int minIdle,
                                              @Value("${redis.maxWaitMillis}") long maxWaitMillis,
                                              @Value("${redis.testOnBorrow}") boolean testOnBorrow,
                                              @Value("${redis.testOnReturn}") boolean testOnReturn){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(maxTotal);
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMinIdle(minIdle);
        jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
        jedisPoolConfig.setTestOnBorrow(testOnBorrow);
        jedisPoolConfig.setTestOnReturn(testOnReturn);
        return jedisPoolConfig;
    }

    @Bean
    public JedisPool getJedisPool(@Value("${redis.hostName}") String hostName,
                                  @Value("${redis.port}") int port,
                                  @Value("${redis.timeout}") int timeout,
                                  @Value("${redis.password}") String password,
                                  JedisPoolConfig jedisPoolConfig){
        JedisPool jedisPool = new JedisPool(jedisPoolConfig, hostName, port, timeout, password);
        return jedisPool;
    }

}
