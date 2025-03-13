package com.example.ordersystem.common.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.xml.transform.OutputKeys;

@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;

    //    redis에 접근하기 위한 접근(connection)객체가 먼저 필요하다.
    @Bean
    @Qualifier("rtdb") //이 어노테이션을 통해 어떤 목적에서 레디스를 사용하는 건지 알 수 있음.
    public RedisConnectionFactory redisConnectionFactory(){
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
//          configuration에 연결 정보를 넣어줘야 함 ->그래서 위에서 .yml에서 관련 값가지고 옴
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(0);
        return new LettuceConnectionFactory(configuration);

    }

    //    redis에 저장할 key, value의 타입지정한 템플릿 객체 생성
//     redisTemplate이라는 메서드가 config전체에 1개는 있어야함
    @Bean
    @Qualifier("rtdb")
    public RedisTemplate<String, Object> redisTemplate(@Qualifier("rtdb") RedisConnectionFactory redisConnectionFactory){
//          redisConnectionFactory는 여러개가 있으니까 그중에 멀쓸지 알려주는 어노테이션 Qulifier
//          redistemplate도 여러개가 있을 수 있으니까 그 중에 멀쓸지 알려주는 어노테이션인 Qulifier를 쓴다
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }



    @Bean
    @Qualifier("stockinvetory") //이 어노테이션을 통해 어떤 목적에서 레디스를 사용하는 건지 알 수 있음.
    public RedisConnectionFactory stockRedisConnectionFactory(){
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
//          configuration에 연결 정보를 넣어줘야 함 ->그래서 위에서 .yml에서 관련 값가지고 옴
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(1);
        return new LettuceConnectionFactory(configuration);

    }

    //    redis에 저장할 key, value의 타입지정한 템플릿 객체 생성
    @Bean
    @Qualifier("stockinventory")
    public RedisTemplate<String, String> stockRedisTemplate(@Qualifier("stockinvetory") RedisConnectionFactory redisConnectionFactory){
//          redisConnectionFactory는 여러개가 있으니까 그중에 멀쓸지 알려주는 어노테이션 Qulifier
//          redistemplate도 여러개가 있을 수 있으니까 그 중에 멀쓸지 알려주는 어노테이션인 Qulifier를 쓴다
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }


}