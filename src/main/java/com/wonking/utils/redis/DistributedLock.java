package com.wonking.utils.redis;

import com.wonking.utils.redis.shardedjedis.ShardedRedisUtil;
import com.wonking.utils.thread.ThreadUtil;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by wangke18 on 2018/5/30.
 * redis实现的分布式锁
 * 这里需要确定的是，分布式是采用分片的分布式还是redis集群做分布式
 * 这里先做基于分片集群的分布式锁
 *
 * 分布式锁的结构设计：
 * 1.key-value形式，key作为唯一的互斥资源
 *   -有人可能会说，key是唯一的,那value不就没意义了吗
 *   -value的意义在于，解铃还需系铃人，用户解锁时的key-value对必须和锁定的时候完全一致，否则不予解锁
 *   -设想这样一个场景，用户A用key1-value1锁住了key1这个资源，并且设定了过期时间
 *   -但是A在释放锁之前挂了，key1过期了之后，用户B又用key1-value2锁住了key1
 *   -这时候A又恢复了，企图释放锁，但这时这个key1已经不属于A了，A无权释放
 *   -所以判断A是否能释放锁，就需要用value来做对比了，value的意义就体现在这里
 *   -这里还有个问题，如果B设置的值刚好也是value1，那这种情况下，A即使无权释放，但也可以释放，就会出问题了
 *   -所以设置value有一个原则，一般要取任何时刻，任何环境下的唯一值，比如说UUID。
 *   -现在的http方式通信中，一般会给每一个请求一个traceId，也可以作为value
 *
 *   网上有一种说法是value存锁的到期时间，是另一种设计思路，但是这种方案的前提条件是所有客户端的时钟系统是同步的，这无法确保
 *   但此问题也有解，就是客户端统一获取redis服务器时间，这又增加了实现的复杂度，所以干脆换一种思路
 *
 * 2.必须设置expire time，否则如果某一个用户先锁定了这个key，然后它挂了，没有及时释放锁，那么这个资源将永远处于锁定状态，谁也无法使用
 * 3.解锁，就是直接删除（del）这个key
 */
public class DistributedLock {
    private static final String LOCK_SUCCESS="OK";
    //写入的行为，只有当key存在才写入
    private static final String SET_NOT_EXISTS="NX";
    //key过期时间单位-second
    private static final String SET_WITH_EXPIRE_TIME="EX";

    //这里有个bug，就是如果del的时候，key过期了，返回值是0，但其实最终这个key还是释放了(前提是你关心release返回值的情况下它才能叫BUG)
    //不过在大多时候，我们并不关心release操作的结果，因为只要不是执行抛异常，这个key一定会失效
    private static final String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
    private static final Long RELEASE_SUCCESS_CODE=1L;

    private static ShardedRedisUtil redis=ShardedRedisUtil.getRedis();
    private static ThreadLocal<String> lock=new ThreadLocal<>();

    //这里的expire是key的过期时间
    public static boolean tryLock(String key, String value, int expire){
        return LOCK_SUCCESS.equals(redis.set(key,value, SET_NOT_EXISTS, SET_WITH_EXPIRE_TIME, expire));
    }

    /**
     * 带超时时间的锁操作,是指尝试获取锁操作的最长时间
     * @param key
     * @param value
     * @param expire key过期时间
     * @param timeout 尝试获取锁操作的最长时间
     * @return 是否成功获取到锁
     */
    public static boolean tryLockWithTimeout(String key, String value, int expire, int timeout){
        long endTime=System.currentTimeMillis()+timeout*1000;
        while (System.currentTimeMillis() < endTime){
            if(tryLock(key, value, expire)){
                return true;
            }
            //不能在给定超时时间内无限次重试，所以要将超时时间划分成多个时间片，每个时间片内只试一次
            ThreadUtil.sleepMills(10);
        }
        return false;
    }

    //带重试次数的锁操作，经实践检验，这种锁失败的策略实际使用价值不大
    //因为每一次重试之间没有间隔，而重试次数一般不会太大，这导致执行一次和执行多次几乎没有区别，因为都是在一瞬间完成的
    //而在这么短的时间内，锁被释放的几率非常小
    //所以建议一般情况下使用tryLockWithTimeout
    public static boolean tryLockWithRetryTime(String key, String value, int expire, int retryTimes){
        for(int i=0; i<retryTimes+1; ++i){
            if(tryLock(key, value, expire)){
                return true;
            }
        }
        return false;
    }

    //采用setnx方式实现的锁，这种方式已废弃，换用新的set api
    @Deprecated
    public static boolean tryLock1(String key, String value, int timeout){
        return RELEASE_SUCCESS_CODE.equals(redis.setnx(key, value, timeout));
    }

    //采用lua脚本执行删除操作，首先获取锁对应的value值，检查是否与传入value相等，如果相等则删除锁（解锁），否则do nothing
    //redis是一个单进程，单线程的服务，它内部采用队列来将并发操作转化为顺序操作
    //lua操作可以保证删除操作的原子性，整段lua代码将被当成一个命令去执行，并且直到eval命令执行完成，Redis才会执行其他命令
    //非原子性的场景是，当判断完内存值和给定值相等后，下一步准备执行del命令
    //但是此时key突然失效了，并且被另一个用户获取了，那么此时del命令就是非法的

    //释放锁操作要保证的是redis服务端执行的原子性，而不是客户端的原子性
    //如果你仅仅保证了自己编写的客户端操作的原子性(比如说在方法上加上synchronized限制)，那是不够的
    //因为不可能所有客户端都使用了你编写的API，而其他使用了非原子性API的客户端并发调用下可能破坏你的原子性
    //事实上，即使所有的客户端都使用你编写的API，但它们是分布式部署的，不同进程的客户端相互之间也会破坏原子性
    //假设这样一个场景，两个使用了你编写的API的客户端，分别部署在两台机器A,B上
    //其中A在执行解锁操作，此时判断到内存值与给定值相等，接下来执行del命令
    //对于进程A中的多个子线程来说，他们相互之间的解锁操作当然是互不干扰的(synchronized保证了这一点)
    //但是进程B里面的操作是自由的，假设key此时失效，并被进程B中的一个线程获取到了
    //此时进程A中的del命令就是非法的，key已经不属于它了
    //综上，只能保证redis服务端的释放原子性，而不能保证客户端的原子性
    //这里我们采用的是eval命令的方式，还有一种方式是，使用redis自带的Transaction来保证原子性
    //其他的实现得非常复杂的方式就不建议使用了，比如说释放锁的时候，再引入另一个锁来保证release操作的原子性，
    //但上述问题会无限递归存在下去，仍然问题重重
    public static boolean releaseLock(String key, String value){
        Long code=redis.eval(script, key, value);
        return RELEASE_SUCCESS_CODE.equals(code);
    }

    public static void main(String[] args) {
        ThreadUtil thread=ThreadUtil.getFixedExecutor(20);
        String key="wonking";
        String value="001";
        for(int i=0;i<10;++i){
            //这里的lock success和release success的次数不相等的测试结果是正确的
            //因为他锁定了5次，不代表就一定要成功释放5次
            //可能某一次release操作是在lock之前，那么就无key可释放，自然release失败
            //但存在一个定性关系，release成功次数 <= lock成功次数
            //因为你不可能都没锁成功就释放成功了
            thread.submitTask(new Task(key,value));
            thread.submitTask(new ReleaseTask(key, value));
        }
        thread.shutdown();
    }

    static class Task implements Runnable{
        private String key;
        private String value;
        private int number;
        private String name;
        private static AtomicInteger count=new AtomicInteger(0);
        public Task(String key, String value){
            this.key=key;
            this.value=value;
            number=count.incrementAndGet();
            name="task"+number;
        }

        @Override
        public void run() {
            if(tryLock(key,value, 60)){
            //if(tryLockWithTimeout(key, value, 1, 2)){
            //if(tryLockWithRetryTime(key, value, 1, 5)){
                System.out.println(name+" lock success");
            }else {
                System.out.println(name+" lock fail");
            }
        }
    }

    static class ReleaseTask implements Runnable{
        private String key;
        private String value;
        private int number;
        private String name;
        private static AtomicInteger count=new AtomicInteger(0);
        public ReleaseTask(String key, String value){
            this.key=key;
            this.value=value;
            number=count.incrementAndGet();
            name="releaseTask"+number;
        }
        @Override
        public void run() {
            if(releaseLock(key,value)){
                System.out.println(name+" release success");
            }else {
                System.out.println(name+" release fail");
            }
        }
    }
}
