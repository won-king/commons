package com.wonking.utils.redis.jediscluster;

import com.wonking.utils.redis.RedisConfig;
import redis.clients.jedis.JedisCluster;

/**
 * Created by wangke18 on 2018/5/18.
 */
public class JedisClusterUtil {
    private static JedisCluster cluster;

    static{
        //只需要添加一个实例，jedis会自动发现集群中其它节点
        cluster=new JedisCluster(RedisConfig.getCluster(), RedisConfig.getTimeOut());
        //cluster=new JedisCluster(RedisConfig.getClusters(), RedisConfig.getTimeOut());
    }

    private static <T> T execute(ClusterRedisExecutor<T> executor){
        return executor.execute(cluster);
    }

    public static String set(String key,String value){
        return execute(jedis -> jedis.set(key, value));
    }

    public static String get(String key){
        return execute(jedis -> jedis.get(key));
    }

    public static Long del(String key){
        return execute(jedis -> jedis.del(key));
    }

    public Boolean exists(String key){
        return execute(jedis -> jedis.exists(key));
    }

    public Long expire(String key, int time){
        return execute(jedis -> jedis.expire(key, time));
    }

    public static void main(String[] args) {

    }
}
