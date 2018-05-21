package com.wonking.utils.encrypt;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

/**
 * Created by kewangk on 2017/10/11.
 */
public abstract class AbstractEncryptor implements IEncryptor {
    private Cipher cipher;

    AbstractEncryptor(String algorithm){
        initCipher(algorithm);
    }
    private void initCipher(String s){
        try {
            cipher=Cipher.getInstance(s);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    protected String toHex(byte[] bytes){
        char[] chars=new char[2*bytes.length];
        for(int i=0;i<bytes.length;++i){
            chars[2*i]=Character.forDigit((bytes[i] & 240)>>4, 16);
            chars[2*i+1]=Character.forDigit((bytes[i] & 15),16);
        }
        return new String(chars);
    }
    public static byte[] hexToBytes(String hexString){
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

    byte[] encryptToBytes(String message, Key key){
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(message.getBytes());
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }
    byte[] decryptToBytes(byte[] bytes, Key key){
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(bytes);
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }
}
