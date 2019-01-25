package com.wonking.test;

import org.springframework.context.ApplicationContextAware;

/**
 * Created by wangke18 on 2019/1/24.
 * 功能描述：过滤器链其实做的是这样一件事，将实际的执行逻辑(过滤器链的终端)，及其系列过滤器组合起来
 *          比如说tomcat源码中实现了FilterChain的ApplicationFilterChain，这个里面包装了Servlet，和一个Filter数组
 *          servlet就是终端执行逻辑，Filter数组就是过滤器链
 *          过滤器链内部需要实现的功能主要包括，过滤器的添加及扩容，将请求与过滤器的调用完美组合起来(并且保证过滤器的调用顺序)
 *
 *  以下示例，就是一个典型的过滤器链的基本模型，包含一个过滤器数组，一个终端执行逻辑，一个游标指示当前过滤器的执行位置
 *
 *  博客里都说，tomcat中的FilterChain实现类是采用单例模式，但如果是这样则无法保证pos的线程安全
 *  有人说，可能是对filterChain进行了池化处理，这样一来，就很容易理解，为什么tomcat会存在请求量的并发上限
 *  因为每一个请求来了，都要经过整个过滤器链，而这些链对象是存储在对象池中，数量存在上限
 *
 *  另外，终端逻辑一般也是采用单例模式的，就像servlet一样，单例多线程的，所以终端逻辑内部也要保证线程安全
 */
public class ApplicationFilterChain implements FilterChain {
    private Filter[] filters=new Filter[0];

    private Bidder bidder;

    //负载因子
    private static final float LOAD_FACTOR=0.75F;

    private int n;
    private int pos;

    public static ApplicationFilterChain createFilterChain(Bidder bidder){
        return null;
    }

    @Override
    public void doFilter(Request request, Response response) {
        if(pos<n){
            Filter filter=filters[pos++];
            try{
                filter.doFilter(request, response, this);
            }catch (Exception e){
                //这里应该包装一下，提示是过滤器异常
                throw new RuntimeException(e);
            }
            return;
        }
        //所有的过滤器执行完毕，开始执行终端业务逻辑
        try{
            bidder.bid(request, response);
        }catch (Exception e){
            //这里应该包装一下，提示是业务异常
            throw new RuntimeException(e);
        }finally {
            //当一个处理链路结束时需要做的一些动作
        }
    }

    //释放所有过滤器，但是内存空间不释放，以便进行重用
    public void release(){
        for(int i=0;i<filters.length;++i){
            filters[i]=null;
        }
        n=0;
        pos=0;
    }

    public void addFilter(Filter filter){
        for(Filter filter1:filters){
            if(filter==filter1){
                return;
            }
        }
        //这里参考HashMap的扩容机制，超过负载因子，则按倍数扩容
        if((double)n/(double)filters.length > LOAD_FACTOR){
            Filter[] newFilters=new Filter[filters.length*4/3];
            System.arraycopy(filters, 0, newFilters, 0, n);
            filters=newFilters;
        }
        filters[n++]=filter;
    }

    public void reuse(){
        pos=0;
    }
}
