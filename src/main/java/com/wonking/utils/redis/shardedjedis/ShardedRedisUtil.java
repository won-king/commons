package com.wonking.utils.redis.shardedjedis;

import com.wonking.utils.redis.RedisConfig;
import com.wonking.utils.thread.ThreadUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import java.util.Collections;

/**
 * Created by wangke18 on 2018/5/18.
 */
public class ShardedRedisUtil {
    private static ShardedJedisPool jedisPool;
    private static final ShardedRedisUtil instance=new ShardedRedisUtil();

    static {
        jedisPool=new ShardedJedisPool(RedisConfig.getConfig(),RedisConfig.getShardedInfo());
    }

    public static ShardedRedisUtil getRedis(){
        return instance;
    }

    private ShardedRedisUtil(){}

    private ShardedJedis getInstance(){
        return jedisPool.getResource();
    }

    private <T> T execute(ShardedRedisExecutor<T> executor){
        //jdk1.7 try-with-resources写法，jedis.close方法会自动调用
        try(ShardedJedis jedis=getInstance()){
            return executor.execute(jedis);
        }
    }

    //测试给定key落在哪个分片上
    private void testSharded(String key){
        ShardedJedis shardedJedis=getInstance();
        Jedis jedis=shardedJedis.getShard(key);
        JedisShardInfo info=shardedJedis.getShardInfo(key);
        System.out.println("---"+key+"---");
        System.out.println("host->"+info.getHost());
        System.out.println("port->"+info.getPort());
        System.out.println("timeout->"+info.getSoTimeout());
    }

    /**
     * 最新的set api，作为替代setnx的一种存值方法
     * @param key 键
     * @param value 值
     * @param notExists 存值的策略命令， NX代表不存在的时候写，XX代表存在的情况下才写，仅有两种取值
     * @param expire 过期时间单位， PX-milliseconds，EX-seconds
     * @param expireTime 过期时间
     * @return
     */
    public String set(String key, String value, String notExists, String expire, int expireTime){
        return execute(jedis -> jedis.set(key, value, notExists, expire, expireTime));
    }

    public String set(String key, String value){
        return execute(jedis -> jedis.set(key, value));
    }

    /**
     *
     * @param key
     * @param value
     * @param timeout 超时时间-s
     * @return 成功返回1，失败返回0
     */
    public Long setnx(String key,String value, int timeout){
        return execute(jedis -> jedis.setnx(key, value)==1 ? jedis.expire(key, timeout):0);
    }

    public String get(String key){
        return execute(jedis -> jedis.get(key));
    }

    public Long del(String key){
        return execute(jedis -> jedis.del(key));
    }

    //这里必须用基本类型的包装类型，否则execute返回null，拆箱会报错
    public Boolean exists(String key){
        return execute(jedis -> jedis.exists(key));
    }

    public Long expire(String key, int time){
        return execute(jedis -> jedis.expire(key, time));
    }

    public Long eval(String script, String key, String value){
        Jedis redis=getInstance().getShard(key);
        return (Long) redis.eval(script, Collections.singletonList(key), Collections.singletonList(value));
    }

    public static void main(String[] args) {
        /*String key="key";
        for(int i=0;i<5;++i){
            instance.testSharded(key+i);
        }
        ThreadUtil executor=ThreadUtil.getFixedExecutor(20);
        for(int i=0;i<5;++i){
            final int j=i;
            executor.submitTask(()->instance.set(key+j, "value"+j));
        }
        for(int i=0;i<5;++i){
            final int j=i;
            executor.submitTask(()-> System.out.println(key+j+"->"+instance.get(key+j)));
        }
        for(int i=0;i<5;++i){
            final int j=i;
            executor.submitTask(()-> System.out.println(key+j+" exists->"+instance.exists(key+j)));
        }
        executor.shutdown();*/
    }

}
