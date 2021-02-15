package top.aolien.redis.mq;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

public class RedisMQUtil {

    @Autowired
    private RedisTemplate redisTemplate;

    public void send(String queueName, Object msg) {
        msg = null == msg ? "" : msg;
        RedisMessage redisMessage = new RedisMessage();
        redisMessage.setQueueName(queueName);
        redisMessage.setData(msg);

        redisTemplate.opsForList().rightPush(queueName, redisMessage);
    }

}
