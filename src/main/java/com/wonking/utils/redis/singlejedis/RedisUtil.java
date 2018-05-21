package com.wonking.utils.redis.singlejedis;

import com.wonking.utils.redis.PropertyUtil;
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
 */
public class RedisUtil {

    private static JedisPoolConfig config;
    private static Jedis clients;
    private static JedisPool jedisPool;

    static {
        ResourceBundle resource=ResourceBundle.getBundle("vars/redis.properties");

        config=new JedisPoolConfig();

        config.setMaxIdle(PropertyUtil.getIntOrDefault((Integer) resource.getObject("redis.maxIdle"), JedisPoolConfig.DEFAULT_MAX_IDLE));
        config.setMaxTotal(PropertyUtil.getIntOrDefault((Integer) resource.getObject("redis.maxTotal"), JedisPoolConfig.DEFAULT_MAX_TOTAL));
        config.setMaxWaitMillis(PropertyUtil.getLongOrDefault((Long) resource.getObject("redis.maxWaitMillis"), JedisPoolConfig.DEFAULT_MAX_WAIT_MILLIS));
        //config.setMinIdle();
        config.setBlockWhenExhausted(false);
        config.setTestOnBorrow(false);
        config.setTestOnCreate(false);
        config.setTestOnReturn(false);
        config.setTestWhileIdle(false);
        String url="";
        int port=2196;
        jedisPool=new JedisPool(config, url, port);
    }

    private RedisUtil(){

    }

    private void init(){

    }

}
