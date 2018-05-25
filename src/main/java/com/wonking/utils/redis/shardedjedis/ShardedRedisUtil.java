package com.wonking.utils.redis.shardedjedis;

import com.wonking.utils.redis.RedisConfig;
import com.wonking.utils.thread.ThreadUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

/**
 * Created by wangke18 on 2018/5/18.
 */
public class ShardedRedisUtil {
    private static ShardedJedisPool jedisPool;
    private static final ShardedRedisUtil instance=new ShardedRedisUtil();

    static {
        jedisPool=new ShardedJedisPool(RedisConfig.getConfig(),RedisConfig.getShardedInfo());
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

    public String set(String key, String value){
        return execute(jedis -> jedis.set(key, value));
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

    public static void main(String[] args) {
        String key="key";
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
        executor.shutdown();
    }

}
