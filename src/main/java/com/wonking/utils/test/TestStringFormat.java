package com.wonking.utils.test;

/**
 * Created by wangke18 on 2018/6/8.
 */
public class TestStringFormat {
    public static void main(String[] args) {
        //testFormat();
        //满足需求即可，这种应用型方法不必深究
        testFloat();
    }

    private static void testFormat(){
        String abc="abcdefghijklmn";
        System.out.println(String.format("%s",abc));
        System.out.println(String.format("%-20s",abc));
    }

    //所有的格式化字符串格式如下：
    //   %[index$][标识][最小宽度]转换符
    //   %[index$][标识]*[最小宽度][.精度]转换符
    //  一律以%开头，[]中括号的表示可选项，否则必填
    //  [index$]一般不用写，主要是用来选择第几个参数的，从1开始
    //  标识，一般用来，当长度小于规定长度时的填充策略，比如填空格，填0，填正负号什么的
    //  最小宽度，直接写数字，目前不知道怎么限制最大宽度
    //  .精度，限制浮点数的小数位数，如.2，只显示2位小数，自动四舍五入
    //  要想显示%，需要%%才能表示显示一个%
    private static void testFloat(){
        float f=0.123446F;
        System.out.println(String.format("%.2f%%",f*100));
    }
}
