package top.aolien.redis.mq;

import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

@ToString
public class RedisMessage<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private String queueName;

    private T data;

    private LocalDateTime createTime;

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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
