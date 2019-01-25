package com.wonking.test;

/**
 * Created by wangke18 on 2019/1/24.
 */
public interface FilterChain {
    void doFilter(Request request, Response response);
}
