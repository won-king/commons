package com.wonking.utils.collection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wangke18 on 2019/2/26.
 */
public class MapUtil {
    public static void main(String[] args) {
        List<String> strings=new ArrayList<>();
        strings.add("abcde");
        strings.add("test");
        strings.add("dbcda");
        strings.add("cadfasd");
        strings.add("dba");
        strings.add("asdgasd");
        Map<String,List<String>> map=map(strings, s->String.valueOf(s.charAt(0)),
                MapUtil.forListExtractor());
        System.out.println(map);
    }

    public static <K,S> ValueExtractor<K,List<S>,S> forListExtractor(){
        /*return new ValueExtractor<K, List<S>, S>() {
            @Override
            public List<S> extract(Map<K, List<S>> map, K k, S s) {
                List<S> exists=map.get(k);
                if(exists==null){
                    exists=new ArrayList<>();
                }
                exists.add(s);
                return exists;
            }
        };*/
        return (map,k,s)->{
            List<S> exists=map.get(k);
            if(exists==null){
                exists=new ArrayList<>();
            }
            exists.add(s);
            return exists;
        };
    }

    public static <K,V,S> Map<K,V> map(List<S> list, KeyExtractor<S,K> keyExtractor,
                                       ValueExtractor<K,V,S> valueExtractor){
        Map<K,V> map=new HashMap<>(list.size());
        for(S s:list){
            K k=keyExtractor.extract(s);
            V v=valueExtractor.extract(map, k, s);
            putIfAbsent(map, k, v);
        }
        return map;
    }

    public static <K,V> Map<K,V> map(List<V> list, KeyExtractor<V,K> extractor){
        Map<K,V> map=new HashMap<>(list.size());
        for(V v:list){
            map.put(extractor.extract(v), v);
        }
        return map;
    }

    public static <K,V> void putIfAbsent(Map<K,V> map, K key, V value){
        if(!map.containsKey(key)){
            map.put(key, value);
        }
    }
}
