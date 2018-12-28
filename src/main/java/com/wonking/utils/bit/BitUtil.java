package com.wonking.utils.bit;

/**
 * Created by wangke18 on 2018/12/28.
 */
public class BitUtil {
    /**
     * 将number第index位置为1
     * @param number
     * @param index 从右向左数，从1开始
     * @return
     */
    public static int setBit(int number, int index){
        if(index<1){
            throw new RuntimeException("wrong index "+index);
        }
        return number | (1 << (index-1));
    }

    /**
     * 获取number第index位的值
     * @param number
     * @param index 从右向左数，从1开始
     * @return
     */
    public static boolean getBit(int number, int index){
        if(index<1){
            throw new RuntimeException("wrong index "+index);
        }
        return ((number & (1 << (index-1))) != 0);
    }

    /**
     * 将number第index位置为0
     * @param number
     * @param index 从右向左数，从1开始
     * @return
     */
    public static int clearBit(int number, int index){
        if(index<1){
            throw new RuntimeException("wrong index "+index);
        }
        int mask = ~(1 << (index-1));
        return (number & (mask));
    }

    /**
     * 计算num包含1的位数
     * 思路：要想对1的个数进行计数，就要从右开始，计算所有1的个数，每计算一次，就把1清除掉，直到最终的数变成00000000
     * 关键代码：num&=(num-1)，这行的作用是，清除掉最右边一位的1
     * 比如说10101000,要想清除最右边的1
     * 就应该用 1010 1000 & 1010 0111=1010 0000，依次进行下去
     *
     * @param num
     * @return
     */
    public static int countBit(int num) {
        int count = 0;
        for (; num > 0; count++) {
            num &= (num - 1);
        }
        return count;
    }

    /**
     * 注：这个方法效率比较低，不要用
     * @param number
     * @return
     */
    private static int countBit1(int number){
        int count=0;
        while (number>0){
            if((number & 1) > 0){
                count++;
            }
            number = number>>1;
        }
        return count;
    }

    public static void main(String[] args) {
        long start=System.currentTimeMillis();
        int time=1000000;
        test1(time);
        System.out.println("cost->"+(System.currentTimeMillis()-start));
        //System.out.println();
        //System.out.println();
    }

    private static void test(int time){
        int count=time;
        while ((count--)>0){
            countBit(count);
        }
    }

    private static void test1(int time){
        int count=time;
        while ((count--)>0){
            countBit1(count);
        }
    }
}
