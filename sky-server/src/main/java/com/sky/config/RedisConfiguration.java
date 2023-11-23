package com.sky.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfiguration {
    @Bean
    //     用于操作redis的模板类,封装了 Redis 的常用操作方法，如存储、读取、删除等。
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate redisTemplate = new RedisTemplate();
        //设置连接工厂对象
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        //设置key的序列化器 jdkRedisSerializer(), 这样key会以字符串形式存储到redis中
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        ValueOperations valueOperations = redisTemplate.opsForValue();

        return redisTemplate;
    }
}
