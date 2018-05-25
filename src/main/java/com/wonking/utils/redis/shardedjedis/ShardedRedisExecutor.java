package com.wonking.utils.redis.shardedjedis;

import redis.clients.jedis.ShardedJedis;

/**
 * Created by wangke18 on 2018/5/23.
 */
public interface ShardedRedisExecutor<T> {
    T execute(ShardedJedis jedis);
}
