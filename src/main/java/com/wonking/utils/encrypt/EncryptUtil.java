package com.wonking.utils.encrypt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Created by kewangk on 2017/10/10.
 * 这个其实叫签名工具比较好，因为MD5、SHA1等都是消息摘要(message digest)算法
 */
public class EncryptUtil {

    public static String getBase64(byte[] source){
        return new String(Base64.getEncoder().encode(source));
    }

    //BASE64编码的MD5加密字节流不太常用
    public static String getMD5WithBase64(String message){
        return getBase64(getMD5(message));
    }

    //将加密后的byte[]字节流转换为16进制表示的字符串(最常用的做法)
    //1位变2位,即1个byte变成2个16进制数
    public static String toHex(byte[] source){
        char[] chars=new char[2*source.length];
        for(int i=0;i<source.length;++i){
            chars[2*i]=Character.forDigit((source[i] & 240)>>4, 16);
            chars[2*i+1]=Character.forDigit((source[i] & 15),16);
        }
        return new String(chars);
    }
    //将16进制字符串转化为byte[]数组,其实就是上面的方法的逆运算
    /*public static byte[] hexToBytes(String hexString){
        //这里为什么要限制长度为偶数，因为16进制字符串是1位扩2位来的，所以长度一定是偶数
        if(hexString==null || "".equals(hexString) || hexString.length()%2!=0){
            return null;
        }
        char[] chars=hexString.toCharArray();
        byte[] bytes=new byte[hexString.length()/2];
        for(int i=0;i<bytes.length;++i){
            int n=Character.digit(chars[2*i],16) <<4;
            n+=Character.digit(chars[2*i+1],16);
            bytes[i]=(byte) (n & 0xff);
        }
        return bytes;
    }*/

    public static byte[] getMD5(String message){
        return getEncryptedBytes(message,"MD5");
    }

    //最常见的获取MD5算法(将MD5加密后的字节流转换为16进制的字符,固定长度32位)
    //16位加密其实就是取32位加密后的(8,24)
    public static String getMD5WithHex(String message){
        return toHex(getMD5(message));
    }

    //获取16位的MD5，默认是32位的
    public static String getHexMD5WithHex(String message){
        return getMD5WithHex(message).substring(8,24);
    }

    public static byte[] getEncryptedBytes(String message, String algorithm){
        try {
            MessageDigest md=MessageDigest.getInstance(algorithm);
            md.update(message.getBytes());
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    //这里算法名传"SHA"或"SHA-1",结果都是一样
    public static byte[] getSHA(String message){
        return getEncryptedBytes(message, "SHA-1");
    }

    public static String getSHAWithHex(String message){
        return toHex(getSHA(message));
    }

    public static void main(String[] args){
        String[] strings=new String[]{"wangke","zdd520","123456","1434846599","000000","Vicky"};
        for(String s:strings){
            String hexString=getMD5WithHex(s);
            System.out.println(s+"->"+hexString);
        }
        //System.out.println(Character.digit('a',16));
    }
}
