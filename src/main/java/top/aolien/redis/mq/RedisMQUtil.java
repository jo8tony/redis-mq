package top.aolien.redis.mq;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

public class RedisMQUtil {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public void send(String queueName, Object msg) {
        msg = null == msg ? "" : msg;
        RedisMessage redisMessage = new RedisMessage();
        redisMessage.setQueueName(queueName);
        redisMessage.setData(msg);

        String message = JSON.toJSONString(redisMessage);

        stringRedisTemplate.opsForList().rightPush(queueName, message);
    }

}
