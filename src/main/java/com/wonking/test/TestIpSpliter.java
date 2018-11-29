package com.wonking.test;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangke18 on 2018/11/29.
 * 今天偶然想起当初面试进来的时候面试官给的第一道题，一时兴起，就花了5分钟写了一个比较粗暴的解法<br/>
 * 思路既成，一路写下来没有障碍，特此记录。
 */
public class TestIpSpliter {

    public static void main(String[] args) {
        String s1="11223344";
        String s2="1234511211";
        isIp(s2);
        //isIp(s1);
    }

    public static void isIp(String string){
        if(StringUtils.isBlank(string) || string.length()<4){
            return;
        }
        List<String> ipList=new ArrayList<>();
        int i,j,k;
        for(i=1;i<4;++i){
            for(j=i+1;j<=i+3;++j){
                if(j>=string.length()){
                    break;
                }
                for(k=j+1;k<=j+3;++k){
                    if(k>=string.length()){
                        break;
                    }
                    String s0=string.substring(0,i);
                    String s1=string.substring(i,j);
                    String s2=string.substring(j,k);
                    String s3=string.substring(k,string.length());
                    if(correctIp(s0,s1,s2,s3)){
                        ipList.add(buildIp(s0,s1,s2,s3));
                    }
                }
            }
        }
        for(String ip:ipList){
            System.out.println(ip);
        }
    }

    private static String buildIp(String... nums){
        return nums[0]+"."+nums[1]+"."+nums[2]+"."+nums[3];
    }

    private static boolean correctIp(String... nums){
        for(String s:nums){
            if(!goodOne(s)){
                return false;
            }
        }
        return true;
    }

    private static boolean goodOne(String s){
        try {
            Integer i=Integer.parseInt(s);
            return i<=255 && i>-1;
        }catch (Exception e){
            return false;
        }
    }
}
