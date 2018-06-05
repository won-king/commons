package com.wonking.utils.redis.jediscluster;

import redis.clients.jedis.JedisCluster;

/**
 * Created by wangke18 on 2018/5/25.
 */
public interface ClusterRedisExecutor<T> {
    T execute(JedisCluster jedis);
}
