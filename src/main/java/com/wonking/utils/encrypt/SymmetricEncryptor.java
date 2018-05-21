package com.wonking.utils.encrypt;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;

/**
 * Created by kewangk on 2017/10/11.
 * 对称加密算法(DES, AES, DESede(3DES, DES到AES的过渡性算法))
 */
public class SymmetricEncryptor extends AbstractEncryptor {
    private SecretKey secretKey;

    private static SymmetricEncryptor singleDES=null;
    private static SymmetricEncryptor singleAES=null;

    private static final String PASSWORD="ZDD520";

    private SymmetricEncryptor(String algorithm){
        super(algorithm);
        generateKey(algorithm);
    }

    //这种是系统随机生成密钥key
    private void generateKey(String s){
        try {
            //这种方法可以自己指定密钥
            //secretKey=new SecretKeySpec(PASSWORD.getBytes(), "DES");
            KeyGenerator keyGenerator=KeyGenerator.getInstance(s);
            //这种方法每次产生的密钥是随机的
            secretKey=keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    //这种是自己指定密钥key
    //这里之所以传入byte[]数组是因为key可能是用 base64或 hex编码之后得到的,这个控制权交给调用方
    private void generateWithKey(String algorithm, byte[] key){
        secretKey=new SecretKeySpec(key,algorithm);
    }

    public static SymmetricEncryptor generateDESEncryptor(){
        if(singleDES==null){
            synchronized (SymmetricEncryptor.class){
                if(singleDES==null){
                    singleDES=new SymmetricEncryptor("DES");
                }
            }
        }
        return singleDES;
    }
    public static SymmetricEncryptor generateAESEncryptor(){
        if(singleAES==null){
            synchronized (SymmetricEncryptor.class){
                if(singleAES==null){
                    singleAES=new SymmetricEncryptor("AES");
                }
            }
        }
        return singleAES;
    }

    public static void main(String[] args){
        //SymmetricEncryptor des=generateDESEncryptor();
        SymmetricEncryptor des=generateAESEncryptor();
        String message="wangkefafasfasdfasdf";
        String code=des.encrypt(message);
        System.out.println("code->"+code);
        String decode=des.decrypt(code);
        System.out.println("decode->"+decode);
    }

    @Override
    public String encrypt(String message) {
        byte[] bytes=encryptToBytes(message, secretKey);
        return toHex(bytes);
    }

    @Override
    public String decrypt(String message) {
        byte[] bytes=hexToBytes(message);
        byte[] decodedBytes=decryptToBytes(bytes, secretKey);
        return new String(decodedBytes);
    }

}
