package com.iflytek.integrated.common.utils.ase;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchAlgorithmException;

public class CipherInstance {

    private CipherInstance(){}

    private static class CipherFactory{
        private static Cipher cipher;

        static {
            try {
                cipher = Cipher.getInstance(AseEnum.ECB_PKCS5PADDING.getEncryptType());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            }
        }
    }

    public static Cipher getInstance(){
        return CipherFactory.cipher;
    }
}
