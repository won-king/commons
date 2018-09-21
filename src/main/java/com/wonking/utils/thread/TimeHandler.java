package com.wonking.utils.thread;

import com.wonking.utils.date.DateUtil;

/**
 * Created by wangke18 on 2018/9/20.
 */
public class TimeHandler implements Invocation {
    @Override
    public Object invoke(Object... args) {
        System.out.println(DateUtil.getDateTime());
        return 0;
    }
}
