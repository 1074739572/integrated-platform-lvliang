package com.iflytek.integrated.common.utils.ase;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AesUtil {

    @Value("${aes.key:w5xv7[Nmc0Z/3U^X}")
    private static String KEY;

    private static final String UTF_8 = "utf-8";

    public static String encrypt(String content) throws Exception {
        if(StringUtils.isEmpty(content)){
            return content;
        }
        byte[] bytes = content.getBytes(UTF_8);
        Cipher cipher = CipherInstance.getInstance();
        cipher.init(Cipher.ENCRYPT_MODE,new SecretKeySpec(KEY.getBytes(), "AES"));
        return Base64.encodeBase64String(cipher.doFinal(bytes));
    }

    public static String decrypt(String content) throws Exception {
        if(StringUtils.isEmpty(content)){
            return content;
        }
        byte[] bytes = Base64.decodeBase64(content);
        Cipher cipher = CipherInstance.getInstance();
        cipher.init(Cipher.DECRYPT_MODE,new SecretKeySpec(KEY.getBytes(), "AES"));
        return new String(cipher.doFinal(bytes),UTF_8);
    }

}
