package com.iflytek.integrated.common.utils.ase;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author
 * 加密，解密工具
 */
public class AesUtil {
    private static final Logger logger = LoggerFactory.getLogger(AesUtil.class);

    /**
     * 秘钥key
     */
    private static String KEY = "w5xv7[Nmc0Z/3U^X";

    private static final String UTF_8 = "utf-8";

    public static String encrypt(String content) {
        try {
            if(StringUtils.isEmpty(content)){
                return content;
            }
            byte[] bytes = content.getBytes(UTF_8);
            Cipher cipher = CipherInstance.getInstance();
            cipher.init(Cipher.ENCRYPT_MODE,new SecretKeySpec(KEY.getBytes(), "AES"));
            return Base64.encodeBase64String(cipher.doFinal(bytes));
        }
        catch (Exception e){
            logger.error("加密失败，message：" + e.getMessage());
            return "";
        }
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
