package com.wonking.utils.encode;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.util.Base64;

/**
 * Created by kewangk on 2017/10/12.
 * 编解码工具<br>
 * 编码就是将byte[]数组按照某种规则转化为ASCII字符,便于显示或存储<br>
 * 解码就是将编码后的字符串还原成byte[]数组
 */
public class EncodeUtil {

    // java.util提供的base64加解密工具，速度最快，但不会换行
    // 此外还有这几种，DatatypeConverter,commons-codec
    public static String base64Encode(String message){
        return Base64.getEncoder().encodeToString(message.getBytes());
    }

    public static String base64Encode(byte[] bytes){
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static byte[] base64Decode(String base64String){
        return Base64.getDecoder().decode(base64String.getBytes());
        //return new String(bytes);
    }

    public static String base64MimeEncode(byte[] message){
        return Base64.getMimeEncoder().encodeToString(message);
    }
    public static byte[] base64MimeDecode(String base64String){
        return Base64.getMimeDecoder().decode(base64String);
    }

    //sun.misc的base64编码工具，每行超过76个字符会换行,但是速度比较慢
    public static String sunBase64Encode(String message){
        return new BASE64Encoder().encodeBuffer(message.getBytes());
    }
    public static String sunBase64Decode(String base64String){
        try {
            byte[] bytes=new BASE64Decoder().decodeBuffer(base64String);
            return new String(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //将byte[]数组编码成16进制字符串
    //1位变2位,即1个byte变成2个16进制数
    public static String hexEncode(byte[] source){
        char[] chars=new char[2*source.length];
        for(int i=0;i<source.length;++i){
            chars[2*i]=Character.forDigit((source[i] & 240)>>4, 16);
            chars[2*i+1]=Character.forDigit((source[i] & 15),16);
        }
        return new String(chars);
    }

    //将16进制字符串解码还原成byte[]数组
    public static byte[] hexDecode(String hexString){
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
    }

}
