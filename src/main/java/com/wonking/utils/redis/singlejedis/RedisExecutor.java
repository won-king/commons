package com.wonking.utils.redis.singlejedis;

import redis.clients.jedis.Jedis;

/**
 * Created by wangke18 on 2018/5/22.
 */
public interface RedisExecutor<T> {
    T execute(Jedis jedis);
}
