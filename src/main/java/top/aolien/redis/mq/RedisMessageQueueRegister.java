package top.aolien.redis.mq;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RedisMessageQueueRegister implements ApplicationRunner, ApplicationContextAware {

    private final static String THREAD_PREFIX = "redismq-thread-";

    private final List<Thread> listenerQueue = new ArrayList<>();

    private final Set<String> registerQueueListener = new HashSet<>();

    private ApplicationContext applicationContext;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        List<RedisListenerMethod> candidates = RedisListenerAnnotationScanPostProcesser.getCandidates();
        for (RedisListenerMethod candidate : candidates) {
            registerQueueListener.add(candidate.getQueueName());
        }
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 启动redis消息队列监听器
        for (String listener : registerQueueListener) {
            Thread thread = new Thread(new Worker().setQueueName(listener));
            thread.setName(THREAD_PREFIX + listener);
            listenerQueue.add(thread);
            thread.start();
        }
    }

    private class Worker implements Runnable{
        private String queueName = "";

        @Override
        public void run() {
            if (StringUtils.isEmpty(queueName)) {
                return;
            }

            while (true) {
                try {
                    String msg = stringRedisTemplate.opsForList().leftPop(queueName, 0L, TimeUnit.MINUTES);
                    RedisMessage redisMessage = JSON.parseObject(msg, RedisMessage.class);

                    List<RedisListenerMethod> all = RedisListenerAnnotationScanPostProcesser.getCandidates();
                    ArrayList<RedisListenerMethod> canApplyList = new ArrayList<>();
                    for (RedisListenerMethod rlm : all) {
                        if (rlm.match(queueName)) {
                            canApplyList.add(rlm);
                        }
                    }

                    if (canApplyList.size() > 0) {
                        for (RedisListenerMethod rlm : canApplyList) {
                            Method targetMethod = rlm.getTargetMethod();
                            targetMethod.invoke(rlm.getBean(applicationContext), redisMessage);
                        }
                    }

                } catch (Throwable e) {
                    new RuntimeException("消息处理器创建运行异常", e);
                }
            }
        }

        public Worker setQueueName(String queueName) {
            this.queueName = queueName;
            return this;
        }
    }
}
