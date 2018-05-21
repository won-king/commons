package com.wonking.utils.encrypt;

import java.security.*;

/**
 * Created by kewangk on 2017/10/11.
 * 非对称(Asymmetric)加密算法(RSA, ECC)
 * 注意将RSA与DSA区分开来，RSA是加密算法(也可做签名用)，DSA如其名字，是数字签名算法,不可解密,只能验证
 */
public class AsymmetricEncryptor extends AbstractEncryptor {

    private PublicKey publicKey;
    private PrivateKey privateKey;

    private KeyPair generateKeyPair(String algorithm){
        try {
            KeyPairGenerator kpg=KeyPairGenerator.getInstance(algorithm);
            kpg.initialize(512);
            return kpg.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private AsymmetricEncryptor(String algorithm){
        super(algorithm);
        KeyPair keyPair=generateKeyPair(algorithm);
        publicKey=keyPair.getPublic();
        privateKey=keyPair.getPrivate();
        /*System.out.println(new BASE64Encoder().encodeBuffer(publicKey.getEncoded()));
        System.out.println(new BASE64Encoder().encodeBuffer(privateKey.getEncoded()));
        System.out.println(DatatypeConverter.printBase64Binary(publicKey.getEncoded()));
        System.out.println(DatatypeConverter.printBase64Binary(privateKey.getEncoded()));*/
    }

    public static AsymmetricEncryptor generateRSAEncryptor(){
        return new AsymmetricEncryptor("RSA");
    }

    //DSA不是用Cipher加解密的，是用Signature
    /*public static AsymmetricEncryptor generateDSAEncryptor(){
        return new AsymmetricEncryptor("DSA");
    }*/

    public static void main(String[] args){
        AsymmetricEncryptor encryptor=generateRSAEncryptor();
        //AsymmetricEncryptor encryptor=generateDSAEncryptor();
        String message="wangke";
        String code=encryptor.encrypt(message);
        System.out.println("code->"+code);
        String decode=encryptor.decrypt(code);
        System.out.println("decode->"+decode);
    }

    @Override
    public String encrypt(String message) {
        byte[] bytes=encryptToBytes(message, publicKey);
        return toHex(bytes);
    }

    @Override
    public String decrypt(String message) {
        byte[] bytes=hexToBytes(message);
        byte[] decodedBytes=decryptToBytes(bytes, privateKey);
        return new String(decodedBytes);
    }

}
