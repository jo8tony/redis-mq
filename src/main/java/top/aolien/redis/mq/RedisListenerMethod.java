package top.aolien.redis.mq;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;

public class RedisListenerMethod {
    private String queueName;

    private Object bean;

    private String beanName;

    private Method targetMethod;


    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public Method getTargetMethod() {
        return targetMethod;
    }

    public void setTargetMethod(Method targetMethod) {
        this.targetMethod = targetMethod;
    }

    public Object getBean(ApplicationContext applicationContext) {
        if (bean == null) {
            synchronized (this) {
                if (bean == null) {
                    bean = applicationContext.getBean(beanName);
                    if (bean == null) {
                        throw new RuntimeException("获取包含@RedisLister[" + targetMethod.getName() + "]方法的Bean实例失败");
                    }
                }
            }
        }
        return bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }

    public boolean match(String queueName) {
        return StringUtils.equals(this.queueName, queueName);
    }
}
