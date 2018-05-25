package com.wonking.utils.redis.singlejedis;

import com.wonking.utils.redis.PropertyUtil;
import com.wonking.utils.redis.RedisConfig;
import com.wonking.utils.redis.RedisExecutor;
import com.wonking.utils.thread.ThreadUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ResourceBundle;

/**
 * Created by wangke18 on 2018/5/18.
 * redis客户端有三种方式连接redis服务器</br>
 * 1.jedis/jedispool，服务器是单例，单进程。pool只是对连接方式的一个优化，保持一个长连接的连接池，而不必每次重新建立连接
 * 2.shardedJedis/shardedJedisPool，为了解决巨量数据的单机存储能力不足问题，对数据进行分片存储，
 *   原理是对key进行分片，算出应该落在哪台机器上，客户端不必关心数据存储在哪台机器上，分片由底层完成
 *   需要与下面的cluster进行区别，sharded服务器之间是不进行通信的
 *   也不存在主从备份，某一台的机器挂了，数据就丢了，对于这部分数据的请求返回结果为空
 *   但是你感应不到机器挂了，因为采用一致性哈希，新数据会自动存储到无故障的机器上
 * 3.jedisCluster，这是一个真正的redis集群，主从备份支持，一台机器挂了，对服务无影响
 *
 * 对于1、2两种，这里只实现池化的工具
 */
public class RedisUtil {

    private static JedisPoolConfig config;
    private static Jedis clients;
    //因为是单机部署，所以连接池里的所有jedis实例，都是指向的同一个redis服务器，唯一的区别是，是通过不同的连接连上去的
    //如果不使用连接池，只使用单个jedis实例，所有线程需要串行使用这个资源
    //再如果允许无限创建jedis实例，这又会造成redis服务器的性能下降，所以需要使用连接池
    private static JedisPool jedisPool;
    private static RedisUtil instance=new RedisUtil();

    static {
        //这里不能加.properties后缀，resourceBundle默认的文件后缀是且仅是这一个，所以不需要加，加了反而会找不到
        /*ResourceBundle resource=ResourceBundle.getBundle("vars/redis");

        config=new JedisPoolConfig();
        config.setMaxIdle(PropertyUtil.getIntOrDefault(Integer.parseInt(resource.getString("redis.maxIdle")), JedisPoolConfig.DEFAULT_MAX_IDLE));
        config.setMaxTotal(PropertyUtil.getIntOrDefault(Integer.parseInt(resource.getString("redis.maxTotal")), JedisPoolConfig.DEFAULT_MAX_TOTAL));
        config.setMaxWaitMillis(PropertyUtil.getLongOrDefault(Long.parseLong(resource.getString("redis.maxWaitTime")), JedisPoolConfig.DEFAULT_MAX_WAIT_MILLIS));
        config.setBlockWhenExhausted(false);
        config.setTestOnBorrow(false);
        config.setTestOnCreate(false);
        config.setTestOnReturn(false);
        config.setTestWhileIdle(false);
        String url=resource.getString("redis.url");
        int port=Integer.parseInt(resource.getString("redis.port"));*/
        jedisPool=new JedisPool(RedisConfig.getConfig(), RedisConfig.getHost(), RedisConfig.getPort());
    }

    private RedisUtil(){
    }

    private void init(){
    }

    private Jedis getInstance(){
        return jedisPool.getResource();
    }

    //redis一系列的操作需要一种能传递动作的功能，并且这动作所需要的参数类型和个数各不相同
    //目前Java里面能够传递动作的方法有多种，设计模式，抽象方法，接口+匿名函数等
    //但是结合第二个条件--参数各异，所以设计模式和抽象方法都行不通，因为他们都是方法，这样参数就固定死了
    //我们需要找到一种方法让动作中可以随意使用各种参数，所以很显然，只能用类来传递
    //而且这个类具有不同的行为，那么这个类只能是接口
    //那么接口怎么设计呢?
    //首先确定，方法的返回值是不固定的，所以想到需要泛型
    //然后，方法需要一个固定的参数jedis实例，用来执行redis操作，至此，这个接口的轮廓已经很清晰了

    //问题来了，连接池内部是怎么判断连接是否处于空闲状态的？
    //因为现在发现，如果每次用完后不关闭连接，超过maxTotal的并发数都扛不住了

    private <T> T execute(RedisExecutor<T> executor){
        //这里每一次会自动选择空闲状态的jedis连接
        Jedis jedis=null;
        try{
            jedis=getInstance();
            return executor.execute(jedis);
        }finally {
            //这里官方已经不建议显式调用资源归还方法了，而是直接用下面的关闭方法来关闭
            //因为returnResource方法被集成到close一起了，close里面不是关闭资源，而是进行资源回收动作
            //并且会判断这个资源是否已损坏，损坏则进行清理，未损坏则再次丢进连接池
            //而且需要注意，这个close方法是必须调用的，否则每一次操作取出来的实例都不会被回收，即使事实上它已经处于空闲状态
            //jedisPool.returnResource(jedis);
            if(jedis!=null){
                jedis.close();
            }
        }
    }

    //data structure--string begin
    public String set(String key, String value){
        /*Jedis jedis=getInstance();
        return jedis.set(key, value);*/
        return execute(jedis -> jedis.set(key, value));
    }

    public String get(String key){
        /*Jedis jedis=getInstance();
        return jedis.get(key);*/
        return execute(jedis -> jedis.get(key));
    }

    public Long del(String... keys){
        /*Jedis jedis=getInstance();
        return jedis.del(key);*/
        return execute(jedis -> jedis.del(keys));
    }
    //data structure--string end

    public boolean exists(String key){
        /*Jedis jedis=getInstance();
        return jedis.exists(key);*/
        return execute(jedis -> jedis.exists(key));
    }

    public Long expire(String key, int second){
        /*Jedis jedis=getInstance();
        return jedis.expire(key, second);*/
        return execute(jedis -> jedis.expire(key, second));
    }

    public static void main(String[] args) {
        /*ThreadUtil executor=ThreadUtil.getFixedExecutor(20);
        for(int i=0;i<5;++i){
            final int j=i;
            executor.submitTask(()->instance.set("key"+j, "value"+j));
        }
        for(int i=0;i<5;++i){
            final int j=i;
            executor.submitTask(()-> System.out.println("key"+j+"->"+instance.get("key"+j)));
        }*/
        /*for(int i=0;i<5;++i){
            final int j=i;
            executor.submitTask(()->instance.expire("key"+j, 1));
        }*/
        /*ThreadUtil.sleep(1);
        for(int i=0;i<5;++i){
            final int j=i;
            executor.submitTask(()-> System.out.println("key"+j+" exists->"+instance.exists("key"+j)));
        }
        executor.shutdown();*/
    }

}
