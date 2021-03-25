package top.aolien.redis.mq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import top.aolien.redis.mq.core.RedisListenerAnnotationScanPostProcesser;
import top.aolien.redis.mq.core.RedisMessageQueueRegister;
import top.aolien.redis.mq.utils.RedisMQUtil;

@Configuration
@ConditionalOnBean(RedisConnectionFactory.class)
@ConditionalOnProperty(prefix = "redis.queue.listener", name = "enable", havingValue = "true", matchIfMissing = true)
public class RedisMQListenerAutoConfig {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Bean
    public RedisListenerAnnotationScanPostProcesser redisListenerAnnotationScanPostProcesser(){
        return new RedisListenerAnnotationScanPostProcesser();
    }

    @Bean
    public RedisMessageQueueRegister redisMessageQueueRegister(){
        return new RedisMessageQueueRegister();
    }

    @Bean
    public RedisMQUtil redisMQUtil() {
        return new RedisMQUtil(redisMQTemplate());
    }

    @Bean("redisMQTemplate")
    @ConditionalOnMissingBean(name = "redisMQTemplate")
    public RedisTemplate<String, Object> redisMQTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }
}
