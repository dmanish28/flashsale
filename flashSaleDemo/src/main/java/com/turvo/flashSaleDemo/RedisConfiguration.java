package com.turvo.flashSaleDemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@EnableCaching
@ComponentScan("com.turvo.flashSaleDemo")
public class RedisConfiguration {

    @Autowired
    RedisProperties redisProperties;

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
    	RedisStandaloneConfiguration redisStandaloneConfiguration = 
    			new RedisStandaloneConfiguration(redisProperties.getHost(), redisProperties.getPort());
        return new JedisConnectionFactory(redisStandaloneConfiguration);
    }
     
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        return template;
    }
}
