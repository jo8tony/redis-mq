package top.aolien.redis.mq.utils;

import org.springframework.data.redis.core.RedisTemplate;
import top.aolien.redis.mq.RedisMessage;

import java.io.Serializable;
import java.time.LocalDateTime;

public class RedisMQUtil {

    private RedisTemplate redisTemplate;

    public RedisMQUtil(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void send(String queueName, Serializable msg) {
        msg = (null == msg ? "" : msg);
        RedisMessage redisMessage = new RedisMessage();
        redisMessage.setQueueName(queueName);
        redisMessage.setCreaetTime(LocalDateTime.now());
        redisMessage.setData(msg);

        redisTemplate.opsForList().rightPush(queueName, redisMessage);
    }

}
