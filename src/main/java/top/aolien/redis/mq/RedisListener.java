package top.aolien.redis.mq;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisListener {

    @AliasFor("queueName")
    String value() default "";

    @AliasFor("value")
    String queueName() default "";

}
