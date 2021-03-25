## RedisMQ简介
这是一个使用基于Redis列表数据类型实现的具有**消息队列**功能的项目，该项目建立在Spring Boot框架之上，通过Spring提供的RedisTemplate功能访问Redis服务，并利用Spring Boot自动配置的功能可以方便的注入其他项目中使用。
使用可参考博客：[https://blog.csdn.net/qq_29550537/article/details/113846700](https://blog.csdn.net/qq_29550537/article/details/113846700)
## 如何使用
##### 1.克隆并打包
首先，你需要将项目克隆到本地，并且通过maven打包并发布到本地maven库中。
```bash
# 克隆项目
git clone https://github.com/jo8tony/redis-mq.git
# 在项目根目录下运行下面命令打包发布
mvn install
```
##### 2.引入到项目
接下来需要这自己的Spring Boot项目中映入该工程的jar包。这里我们创建一个测试用的Spring Boot工程redis-mq-test。
```xml
<dependency>
    <groupId>top.aolien</groupId>
    <artifactId>redismq-spring-boot-starter</artifactId>
    <version>1.0.1</version>
</dependency>
```
##### 3.添加配置
redis-mq依赖一旦引入到项目中便具有了它提供的消息队列功能，接下来需要添加一些redis相关的基本配置。redis-mq使用的是Spring提供的RedisTemplate的功能，所以参照该功能相关配置即可。
```yml
spring:
  redis:
    host: 127.0.0.1
    port: 6379
```
PS：如果关闭Redis消息队列的功能可以删除上面的jar包依赖，或者在配置文件中添加配置
```yml
redis:
  queue:
    listener:
      enable: false  #true表示开启redis消息队列监听功能
```

##### 4.消息生产者示例
在测试工程redis-mq-test中创建普通java类Student，用于作为redis消息传递，当然也可以任意类，但是该类需要**实现序列化接口Serializable**

```java
@Data
@Accessors(chain = true)
@ToString
public class Student implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String name;
    private String sex;
    private Integer age;
}
```
然后创建一个TestController类，最作为web项目的接口用于发送给redis消息测试。
PS：redis消息发送的方法是`RedisMQSender.send(参数1，参数2)`方法，参数1是String类型的消息队列名称，参数2是实现序列化接口的你想传递的任意数据。

```java
@RestController
public class TestController {

    @Autowired
    private RedisMQSender redisMQSender;

    @RequestMapping("/send/msg")
    public String sendQueueMessage1(){
        Student student = new Student()
                .setId(88888888L)
                .setName("jo8tony")
                .setSex("男")
                .setAge(18);
        redisMQSender.send("queue-1", student);
        return "SUCCESS";
    }

    @RequestMapping("/send/msg2")
    public String sendQueueMessage2(){
        Student student = new Student()
                .setId(99999999L)
                .setName("小红")
                .setSex("女")
                .setAge(20);
        redisMQSender.send("queue-2", student);
        return "SUCCESS";
    }

}
```

##### 5.消息消费者示例
创建一个RedisListenerContainer类用于定义redis队列消息监听处理方法。
PS： 实现redis队列监听只需在Spring容器所管理的Bean中的方法上添加注解`@RedisListener(参数1)`，参数1是一个String类型的队列名称并且不能为空，表示该方法你需要处理的哪个队列的消息。注意被@RedisListener修饰的方法只能包含一个参数，这个参数的可以一个`top.aolien.redis.mq.RedisMessage`类型的参数，也可以是你需要传递的直接消息类型，例如这里的Student。

```java
@Component
public class RedisListenerContainer {

    @RedisListener("queue-1")
    public void dealRedisMessage0(RedisMessage msg) {
        System.out.println("dealRedisMessage0收到queue-1队列消息: " + msg.toString());
    }

    @RedisListener("queue-1")
    public void dealRedisMessage1(Student student) {
        System.out.println("dealRedisMessage1收到queue-1队列消息: " + student.toString());
    }

    @RedisListener("queue-2")
    public void dealRedisMessage2(RedisMessage<Student> msg) {
        System.out.println("dealRedisMessage2收到queue-2队列消息: " + msg.toString());
    }
}
```
**注意：**你可以定义多个带有@RedisListener(参数1)注解的方法，并且参数1相同，注意如果这样该队列中的同一消息会被这些方法重复消费。分布式集群环境中不同程序监听同一队列，同一条消息只会被其中一个程序上的所有监听该队列的方法消费。

##### 6.测试
完成以上步骤后启动测试项目redis-mq-test，可以看到如下日志，表示消息队列监听已启动成功
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210218220504578.png)
然后在浏览器中输入`http://localhost:8080/send/msg`地址，向队列queue-1中发送一条学生信息的消息，观察控制台结果：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210218222030869.png)
这里应为定义了两个queue-1队列的处理方法dealRedisMessage0和方法dealRedisMessage1，所以消息被这两个方法重复消费了。

接着在浏览器中输入`http://localhost:8080/send/msg2`地址，向队列queue-2中发送一条学生信息的消息，观察控制台结果：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210218222429978.png)
