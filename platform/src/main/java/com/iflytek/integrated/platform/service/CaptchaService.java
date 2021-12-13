package com.iflytek.integrated.platform.service;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import io.swagger.annotations.Api;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.imageio.ImageIO;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

/**
 * @Author ganghuang6
 * @Date 2021/7/12
 */
@RestController
@Slf4j
@Api(tags = "验证码服务")
@RequestMapping("/{version}/pt/loginManage")
public class CaptchaService {

    public static final String SECRET_KEY = "2GB3EfhlUjJ4vFVMusiNxktHAnZQKawP";
    public static final Long TIMEOUT_SECONDS = 300L;

    @Autowired
    private DefaultKaptcha defaultKaptcha;

    @GetMapping("/captcha")
    public Captcha get() {
        return get(TIMEOUT_SECONDS);
    }

    public Captcha get(long timeoutSeconds) {
        Captcha captcha = new Captcha();
        try {
        	int fontSize = defaultKaptcha.getConfig().getTextProducerFontSize();
        	log.info("------------fontsize:" + fontSize);
        	Font[] fonts = defaultKaptcha.getConfig().getTextProducerFonts(fontSize);
        	if(fonts != null && fonts.length > 0) {
        		for(Font f : fonts) {
        			log.info("------------fontName:" + f.getFontName());
        		}
        	}
        }catch(Exception e) {
        	log.error("==========getfontsinfoerror" , e);
        }
        //获取验证码字符串
        String captchaText = defaultKaptcha.createText();
        BufferedImage bufferedImage = defaultKaptcha.createImage(captchaText);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(bufferedImage, "jpg", baos);
            baos.flush();
            captcha.setBase64Image(Base64Utils.encodeToString(baos.toByteArray()));

            String meta = (System.currentTimeMillis() + timeoutSeconds * 1000) + ":" + captchaText;
            String encryptedMeta = Base64Utils.encodeToString(encryptDes(meta.getBytes(),
                    SECRET_KEY, StringUtils.leftPad(captchaText, 8)
            ));
            captcha.setEncryptedMeta(encryptedMeta);
        } catch (Exception e) {
            log.error("生成验证码错误", e);
        }
        return captcha;
    }

    public boolean validate(String encryptedMeta, String captchaText) {
        try {
            byte[] data = decryptDes(Base64Utils.decodeFromString(encryptedMeta),
                    SECRET_KEY, StringUtils.leftPad(captchaText, 8));
            String meta = new String(data);
            if (meta.contains(":")) {
                String timestamp = meta.split(":")[0];
                String codeText = meta.split(":")[1];
                // 对比captchaText
                String codeTextStr = codeText.toLowerCase();
                String captchaTextStr = captchaText.toLowerCase();
                return new Long(timestamp) > System.currentTimeMillis() && codeTextStr.equals(captchaTextStr);
            }
        } catch (Exception e) {
            log.info("验证码校验错误 encryptedMeta:{} captchaText:{}", encryptedMeta, captchaText);
            return false;
        }
        return false;
    }

    public byte[] encryptDes(byte[] plainData, String key, String iv) throws Exception {
        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        DESKeySpec desKeySpec = new DESKeySpec(key.getBytes());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv.getBytes()));
        return cipher.doFinal(plainData);
    }

    public byte[] decryptDes(byte[] plainData, String key, String iv) throws Exception {
        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        DESKeySpec desKeySpec = new DESKeySpec(key.getBytes("UTF-8"));
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv.getBytes()));
        return cipher.doFinal(plainData);
    }

    @Data
    public static class Captcha {
        private String encryptedMeta;
        private String base64Image;
    }
}
