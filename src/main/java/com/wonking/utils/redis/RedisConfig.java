package com.wonking.utils.redis;

import com.wonking.utils.collection.Tuple;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;

import java.util.*;

/**
 * Created by wangke18 on 2018/5/18.
 * 读取redis.properties,并将所有正确配置的参数导入JedisPoolConfig中
 * 单机模式的配置，分片模式的配置，和集群模式的配置，都从这里获取
 */
public class RedisConfig {
    private static final int DEFAULT_PORT=6379;
    private static final int DEFAULT_TIME_OUT=10000;
    //整个配置类只读取一个配置文件，现在还没考虑存在多个配置文件（可能是为不同环境准备的）的情况
    private static ResourceBundle resource;

    static {
        //这里不能加.properties后缀，resourceBundle默认的文件后缀是且仅是这一个，所以不需要加，加了反而会找不到
        resource=ResourceBundle.getBundle("vars/redis");
    }

    public static JedisPoolConfig getConfig(){
        JedisPoolConfig config=new JedisPoolConfig();
        config.setMaxIdle(PropertyUtil.getIntOrDefault(Integer.parseInt(resource.getString("redis.maxIdle")), JedisPoolConfig.DEFAULT_MAX_IDLE));
        config.setMaxTotal(PropertyUtil.getIntOrDefault(Integer.parseInt(resource.getString("redis.maxTotal")), JedisPoolConfig.DEFAULT_MAX_TOTAL));
        config.setMaxWaitMillis(PropertyUtil.getLongOrDefault(Long.parseLong(resource.getString("redis.maxWaitTime")), JedisPoolConfig.DEFAULT_MAX_WAIT_MILLIS));
        //config.setMinIdle();
        config.setBlockWhenExhausted(false);
        config.setTestOnBorrow(false);
        config.setTestOnCreate(false);
        config.setTestOnReturn(false);
        config.setTestWhileIdle(false);
        return config;
    }

    public static String getHost(){
        return resource.getString("redis.url");
    }

    public static int getPort(){
        return Integer.parseInt(resource.getString("redis.port"));
    }

    public static List<JedisShardInfo> getShardedInfo(){
        List<JedisShardInfo> result=new ArrayList<>();
        List<Tuple<String,Integer>> hosts=getHostPort();
        int timeout=getTimeOut();
        hosts.forEach(t -> result.add(new JedisShardInfo(t.f, t.s, timeout)));
        return result;
    }

    public static HostAndPort getCluster(){
        Tuple<String,Integer> tuple=getHostPort().get(0);
        return new HostAndPort(tuple.f, tuple.s);
    }

    public static Set<HostAndPort> getClusters(){
        Set<HostAndPort> result=new HashSet<>();
        List<Tuple<String,Integer>> tuples=getHostPort();
        tuples.forEach(t -> result.add(new HostAndPort(t.f, t.s)));
        return result;
    }

    public static int getTimeOut(){
        return PropertyUtil.getIntOrDefault(resource.getString("redis.timeout"), DEFAULT_TIME_OUT);
    }

    @SuppressWarnings("unchecked")
    private static List<Tuple<String,Integer>> getHostPort(){
        String url=resource.getString("redis.url");
        String[] urls=url.split(";");
        List<Tuple<String,Integer>> result=new ArrayList<>();
        for(String s:urls){
            if(s.contains(":")){
                String[] tmp=s.trim().split(":");
                result.add(Tuple.tuple(tmp[0], Integer.parseInt(tmp[1])));
            }else {
                result.add(Tuple.tuple(s, DEFAULT_PORT));
            }
        }
        return result;
    }
}
