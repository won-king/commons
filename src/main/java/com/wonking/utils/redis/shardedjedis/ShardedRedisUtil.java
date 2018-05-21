package com.wonking.utils.redis.shardedjedis;

import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

/**
 * Created by wangke18 on 2018/5/18.
 */
public class ShardedRedisUtil {
    private ShardedJedis jedis;
    private ShardedJedisPool jedisPool;
}
