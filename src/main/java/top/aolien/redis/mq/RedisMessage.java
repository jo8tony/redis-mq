package top.aolien.redis.mq;

import java.io.Serializable;
import java.time.LocalDateTime;

public class RedisMessage<T> implements Serializable {

    private static final long serialVersionUID = 42L;

    private String queueName;

    private T data;

    private LocalDateTime creaetTime;

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public LocalDateTime getCreaetTime() {
        return creaetTime;
    }

    public void setCreaetTime(LocalDateTime creaetTime) {
        this.creaetTime = creaetTime;
    }
}
