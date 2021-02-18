package top.aolien.redis.mq.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import top.aolien.redis.mq.RedisMessage;

import java.time.LocalDateTime;

public class RedisMQUtil {

    @Autowired
    private RedisTemplate redisTemplate;

    public void send(String queueName, Object msg) {
        msg = null == msg ? "" : msg;
        RedisMessage redisMessage = new RedisMessage();
        redisMessage.setQueueName(queueName);
        redisMessage.setCreaetTime(LocalDateTime.now());
        redisMessage.setData(msg);

        redisTemplate.opsForList().rightPush(queueName, redisMessage);
    }

}
