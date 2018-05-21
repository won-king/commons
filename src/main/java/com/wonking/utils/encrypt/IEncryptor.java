package com.wonking.utils.encrypt;

/**
 * Created by kewangk on 2017/10/11.
 */
public interface IEncryptor {
    String encrypt(String message);
    String decrypt(String message);
}
