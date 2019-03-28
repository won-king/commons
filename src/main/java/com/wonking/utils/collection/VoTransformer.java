package com.wonking.utils.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by wangke18 on 2019/3/1.
 */
public class VoTransformer {
    private static final int MAX_MISSING_NUM=99;

    /**
     * model->VO转换器，实现在一次遍历过程中，对列表数据的类型转换，数据汇总，缺失数据补空
     * 重点在于一次遍历，所以需要这么多参数，并且都是泛型+函数式接口，有点函数式编程的味道了
     * @param models 表记录实体
     * @param s 累加器
     * @param producer 空记录制造器
     * @param indicator 下标指示器，指示正确顺序的下标辅助对象
     * @param <V> 转换的结果类型
     * @param <O> 转换的原类型
     * @param <S> 累加器的结果类型
     * @param <K> 下标辅助类型
     * @return
     */
    public static <V,O,S,K> Tuple<List<V>,S> transform(List<O> models,
                                                       Accumulator<S,O> s,
                                                       Indicator<K,O> indicator,
                                                       EmptyProducer<O,K> producer,
                                                       Translator<O,V> translator){
        Iterator<O> iterator=models.iterator();
        S sum=null;
        K k=indicator.initK();
        List<V> vos=new ArrayList<>(models.size());
        while (iterator.hasNext()){
            O model=iterator.next();
            int missing=0;
            while (!indicator.isExpected(k,model)){
                O expected=producer.produce(k,model);
                sum=s.accumulate(sum, expected);
                k=indicator.nextK(k);
                vos.add(translator.translate(expected));
                if((++missing)>MAX_MISSING_NUM){
                    throw new RuntimeException("连续缺失量超出最大范围");
                }
            }
            sum=s.accumulate(sum, model);
            vos.add(translator.translate(model));
            k=indicator.nextK(k);
        }
        return Tuple.tuple(vos, sum);
    }

    public static void main(String[] args) {
        List<Integer> integers= Arrays.asList(1,3,5,7,9);
        //将偶数补齐的例子
        Tuple<List<Integer>,Integer> tuple=transform(integers, new Accumulator<Integer, Integer>() {
            @Override
            public Integer accumulate(Integer init, Integer value) {
                if (init == null) {
                    return value;
                }
                return init + value;
            }
        }, new Indicator<Integer, Integer>() {
            @Override
            public Integer initK() {
                return 1;
            }

            @Override
            public boolean isExpected(Integer integer, Integer o) {
                return integer.intValue() == o.intValue();
            }

            @Override
            public Integer nextK(Integer o) {
                return o + 1;
            }
        }, new EmptyProducer<Integer, Integer>() {
            @Override
            public Integer produce(Integer integer, Integer mold) {
                return integer;
            }
        }, new Translator<Integer, Integer>() {
            @Override
            public Integer translate(Integer model) {
                return model;
            }
        });
        System.out.println(integers);
    }
}
