package top.aolien.redis.mq.core;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.core.RedisTemplate;
import top.aolien.redis.mq.RedisListenerMethod;
import top.aolien.redis.mq.RedisMessage;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RedisMessageQueueRegister implements ApplicationRunner, ApplicationContextAware {

    private final Logger logger = LoggerFactory.getLogger(RedisMessageQueueRegister.class);

    private final static String THREAD_PREFIX = "redismq-thread-";

    private final List<Thread> listenerQueue = new ArrayList<>();

    private final Set<String> registerQueueListener = new HashSet<>();

    private ApplicationContext applicationContext;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void init() {
        List<RedisListenerMethod> candidates = RedisListenerAnnotationScanPostProcesser.getCandidates();
        for (RedisListenerMethod candidate : candidates) {
            registerQueueListener.add(candidate.getQueueName());
        }
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 初始化消息队列名称
        init();
        // 启动redis消息队列监听器
        for (String listener : registerQueueListener) {
            Thread thread = new Thread(new Worker().setQueueName(listener));
            thread.setName(THREAD_PREFIX + listener);
            listenerQueue.add(thread);
            thread.start();
            logger.info("启动消息队列监听器：【" + listener + "】");
        }
    }

    private class Worker implements Runnable{
        private String queueName = "";

        private List<RedisListenerMethod> canApplyList = new ArrayList<>();

        @Override
        public void run() {
            if (StringUtils.isEmpty(queueName)) {
                return;
            }

            while (true) {
                try {
                    RedisMessage msg = (RedisMessage) redisTemplate.opsForList().leftPop(queueName, 0L, TimeUnit.SECONDS);

                    if (canApplyList.size() > 0) {
                        for (RedisListenerMethod rlm : canApplyList) {
                            Method targetMethod = rlm.getTargetMethod();
                            if (rlm.getMethodParameterClassName().equals(RedisMessage.class.getName())) {
                                targetMethod.invoke(rlm.getBean(applicationContext), msg);
                            } else if (rlm.getMethodParameterClassName()
                                    .equalsIgnoreCase(msg.getData().getClass().getName())){
                                targetMethod.invoke(rlm.getBean(applicationContext), msg.getData());
                            } else {
                                throw new RuntimeException("消息队列【" + queueName + "】中的消息类型与"
                                        + targetMethod.getName() + "定义的类型不一致);");
                            }
                        }
                    }

                } catch (QueryTimeoutException e1) {
                    logger.warn(e1.getMessage());
                } catch (Throwable e) {
                    logger.error("redisMQ队列【" + queueName + "】消息处理时异常", e);
                }
            }
        }

        private void obtainCanApplyList() {
            if (canApplyList.size() <= 0) {
                List<RedisListenerMethod> all = RedisListenerAnnotationScanPostProcesser.getCandidates();
                for (RedisListenerMethod rlm : all) {
                    if (rlm.match(queueName)) {
                        canApplyList.add(rlm);
                    }
                }
            }
        }

        public Worker setQueueName(String queueName) {
            this.queueName = queueName;
            obtainCanApplyList();
            return this;
        }
    }
}
