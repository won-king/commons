package com.wonking.utils.encrypt;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kewangk on 2017/10/11.
 * DSA数字签名算法，属于非对称加密算法,也分公钥和私钥。但是算法只是用来做签名用的，使用私钥加密，公钥只能验证，不能解密出明文
 */
public class DSAEncryptor {
    //不仅可以使用DSA算法，同样也可以使用RSA算法做数字签名
    /*public static final String KEY_ALGORITHM = "RSA";
    public static final String SIGNATURE_ALGORITHM = "MD5withRSA";*/

    private static final String KEY_ALGORITHM = "DSA";
    private static final String SIGNATURE_ALGORITHM = "DSA";

    private static final String DEFAULT_SEED = "$%^*%^()(HJG8awfjas7"; //默认种子
    private static final String PUBLIC_KEY = "DSAPublicKey";
    private static final String PRIVATE_KEY = "DSAPrivateKey";

    public static void main(String[] args) throws Exception {
        String str = "!@#$!#^$#&ZXVDF呆军工路爱着你*()_+";
        byte[] data = str.getBytes();

        Map<String, Object> keyMap = initKey();// 构建密钥
        PublicKey publicKey = (PublicKey) keyMap.get(PUBLIC_KEY);
        PrivateKey privateKey = (PrivateKey) keyMap.get(PRIVATE_KEY);
        System.out.println("私钥format：" + privateKey.getFormat());
        System.out.println("公钥format：" + publicKey.getFormat());


        // 产生签名
        String sign = sign(data, getPrivateKey(keyMap));

        // 验证签名
        boolean verify1 = verify("aaa".getBytes(), getPublicKey(keyMap), sign);
        System.err.println("经验证,数据和签名匹配:" + verify1);

        boolean verify = verify(data, getPublicKey(keyMap), sign);
        System.err.println("经验证,数据和签名匹配:" + verify);
    }

    /**
     * 生成密钥
     *
     * @param seed 种子
     * @return 密钥对象
     * @throws Exception
     */
    public static Map<String, Object> initKey(String seed) throws Exception {
        System.out.println("生成密钥");

        KeyPairGenerator keygen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.setSeed(seed.getBytes());
        //Modulus size must range from 512 to 1024 and be a multiple of 64
        keygen.initialize(640, secureRandom);

        KeyPair keys = keygen.genKeyPair();
        PrivateKey privateKey = keys.getPrivate();
        PublicKey publicKey = keys.getPublic();

        Map<String, Object> map = new HashMap<String, Object>(2);
        map.put(PUBLIC_KEY, publicKey);
        map.put(PRIVATE_KEY, privateKey);
        return map;
    }

    /**
     * 生成默认密钥
     *
     * @return 密钥对象
     * @throws Exception
     */
    public static Map<String, Object> initKey() throws Exception {
        return initKey(DEFAULT_SEED);
    }

    /**
     * 取得私钥
     *
     * @param keyMap
     * @return
     * @throws Exception
     */
    public static String getPrivateKey(Map<String, Object> keyMap) throws Exception {
        Key key = (Key) keyMap.get(PRIVATE_KEY);
        return encryptBASE64(key.getEncoded()); //base64加密私钥
    }

    /**
     * 取得公钥
     *
     * @param keyMap
     * @return
     * @throws Exception
     */
    public static String getPublicKey(Map<String, Object> keyMap) throws Exception {
        Key key = (Key) keyMap.get(PUBLIC_KEY);
        return encryptBASE64(key.getEncoded()); //base64加密公钥
    }

    /**
     *  用私钥对信息进行数字签名
     * @param data  加密数据
     * @param privateKey 私钥-base64加密的
     * @return
     * @throws Exception
     */
    public static String sign(byte[] data, String privateKey) throws Exception {
        System.out.println("用私钥对信息进行数字签名");

        byte[] keyBytes = decryptBASE64(privateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory factory = KeyFactory.getInstance(KEY_ALGORITHM);
        PrivateKey priKey = factory.generatePrivate(keySpec);//生成 私钥

        //用私钥对信息进行数字签名
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(priKey);
        signature.update(data);
        return encryptBASE64(signature.sign());
    }

    /**
     * BASE64Encoder 加密
     * @param data 要加密的数据
     * @return 加密后的字符串
     */
    private static String encryptBASE64(byte[] data) {
        /*BASE64Encoder encoder = new BASE64Encoder();
        String encode = encoder.encode(data);
        return encode;*/
        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * BASE64Decoder 解密
     * @param data 要解密的字符串
     * @return 解密后的byte[]
     * @throws Exception
     */
    private static byte[] decryptBASE64(String data) throws Exception {
        /*BASE64Decoder decoder = new BASE64Decoder();
        byte[] buffer = decoder.decodeBuffer(data);
        return buffer;*/
        return Base64.getDecoder().decode(data);
    }

    /**
     * 校验数字签名
     * @param data 加密数据
     * @param publicKey
     * @param sign 数字签名
     * @return
     * @throws Exception
     */
    public static boolean verify(byte[] data, String publicKey, String sign) throws Exception {
        byte[] keyBytes = decryptBASE64(publicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PublicKey pubKey = keyFactory.generatePublic(keySpec);

        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(pubKey);
        signature.update(data);

        return signature.verify(decryptBASE64(sign)); //验证签名
    }
}
