package com.wonking.utils.collection;

/**
 * Created by wangke18 on 2019/3/1.
 */
public interface Translator<O,V> {
    V translate(O model);
}
