package com.wonking.utils.encrypt;

import com.wonking.utils.encode.EncodeUtil;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Created by kewangk on 2017/11/12.
 */
public class SignUtil {

    private static final String ALGORITHM_DSA="DSA";
    private static final String SIGN_ALGORITHM_DSA="DSA";

    //加密算法和签名算法，加密算法生成key，签名算法用于生成signature,二者成对出现
    private static final String ALGORITHM_RSA="RSA";
    private static final String SIGN_ALGORITHM_RSA="MD5withRSA";

    private Signature signature;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    private SignUtil(){
        try {
            KeyPairGenerator kpg=KeyPairGenerator.getInstance(ALGORITHM_DSA);
            kpg.initialize(512);
            KeyPair keyPair=kpg.generateKeyPair();
            privateKey=keyPair.getPrivate();
            publicKey=keyPair.getPublic();
            signature=Signature.getInstance(SIGN_ALGORITHM_DSA);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private SignUtil(String algorithm) throws NoSuchAlgorithmException{
        KeyFactory keyFactory=KeyFactory.getInstance(algorithm);
        try {
            privateKey=generatePrivateKye(keyFactory);
            publicKey=generatePublicKey(keyFactory);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    private PrivateKey generatePrivateKye(KeyFactory keyFactory) throws InvalidKeySpecException{
        //通过IO或什么方式读取到公钥的字符串,再按照某种方式解码
        String privateKeys="";
        byte[] privateKey= EncodeUtil.base64Decode(privateKeys);
        PKCS8EncodedKeySpec keySpec=new PKCS8EncodedKeySpec(privateKey);
        return keyFactory.generatePrivate(keySpec);
    }
    private PublicKey generatePublicKey(KeyFactory keyFactory) throws InvalidKeySpecException{
        //通过IO或什么方式读取到公钥的字符串,再按照某种方式解码
        String publicKeys="";
        byte[] publicKey=EncodeUtil.base64Decode(publicKeys);
        X509EncodedKeySpec keySpec=new X509EncodedKeySpec(publicKey);
        return keyFactory.generatePublic(keySpec);
    }

    public String sign(byte[] message) {
        try {
            signature.initSign(privateKey);
            signature.update(message);
            return EncodeUtil.base64Encode(signature.sign());
        } catch (InvalidKeyException | SignatureException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public boolean verify(byte[] message, String sign) {
        try {
            signature.initVerify(publicKey);
            signature.update(message);
            return signature.verify(EncodeUtil.base64Decode(sign));
        } catch (InvalidKeyException | SignatureException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static SignUtil getInstance(){
        return new SignUtil();
    }

    public static void main(String[] arg){
        SignUtil signUtil=SignUtil.getInstance();
        String message="wonking";
        String encrypt=signUtil.sign(message.getBytes());
        System.out.println("encrypt->"+encrypt);
        System.out.println(signUtil.verify(message.getBytes(), encrypt));
    }
}
