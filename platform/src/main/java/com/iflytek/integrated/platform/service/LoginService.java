package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.dto.LoginDto;
import com.iflytek.integrated.platform.utils.JwtTokenUtils;
import com.iflytek.medicalboot.core.dto.Response;
import com.iflytek.medicalboot.core.exception.MedicalBusinessException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sun.misc.BASE64Encoder;

import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

/**
 * 登录管理
 * @Author ganghuang6
 * @Date 2021/7/12
 */
@Slf4j
@Api(tags = "登录管理")
@RestController
@RequestMapping("/{version}/pt/loginManage")
public class LoginService {

    public static final String ERROR_CODE_VERIFY_CODE_NEED = "100001";
    public static final String ERROR_CODE_VERIFY_CODE_ERROR = "100002";
    public static final String SERVICE_NAME = "IntegratedPlatform";
//    public static final String SALT = "sjdjpt";

    public static final Integer PWD_ERROR_COUNT = 3;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private CaptchaService captchaService;

    @Value("${jwt.username}")
    private String username;

    @Value("${jwt.password}")
    private String password;

    @ApiOperation(value = "登录获取token", notes = "5分钟内密码错误3次会需要输入验证码 HttpCode=400 需要检查返回body code=100001为需要输入验证码 code=100002为验证码错误 验证码有效期60秒")
    @PostMapping("/login")
    public ResultDto<String> userLogin(@RequestBody LoginDto dto){
        //不同登录类型错误分开
        checkLoginErrorCount(dto);

        String name = dto.getUsername();
        //用户名密码校验
        if(name.equals(username)){
//            String password = LoginConstant.map.get(username);
//            String passwordMd5 = cipherWithMd5(password);
//            String sha256 = sha256Digest(passwordMd5+SALT);
            if(!dto.getPassword().equals(password)){
                String cacheCountKey = buildLoginCountCacheKey(name);
                String loginCountStr = stringRedisTemplate.opsForValue().get(cacheCountKey);
                int loginCount = 1;
                if (StringUtils.hasText(loginCountStr)) {
                    loginCount = Integer.parseInt(loginCountStr) + 1;
                    stringRedisTemplate.opsForValue().set(cacheCountKey, loginCount + "");
                }else {
                    stringRedisTemplate.opsForValue().set(cacheCountKey, loginCount + "", 300L, TimeUnit.SECONDS);
                }

                if (loginCount >= PWD_ERROR_COUNT) {
                    checkImageVerifyCode(dto);
                }

                throw new MedicalBusinessException("用户名密码不正确");
            }
        }else{
            throw new MedicalBusinessException("用户名密码不正确");
        }
        stringRedisTemplate.delete(buildLoginCountCacheKey(name));
        //生成访问nginx接口token
//        String usernameAndPassword = username + ":" + LoginConstant.map.get(username);
//        String token = getToken(usernameAndPassword);
//        String base64Token = "Basic " + token;
        String token = JwtTokenUtils.createToken(name);
        String jwtToken = "Bearer " + token;

        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE,"登录成功!", jwtToken);
    }

    public String getToken(String src)  {
        BASE64Encoder encoder =new BASE64Encoder();
        return encoder.encode(src.getBytes());
    }

    private String cipherWithMd5(String pwd) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] srcBytes = pwd.getBytes();
            md5.update(srcBytes);
            byte[] resultBytes = md5.digest();
            return Hex.encodeHexString(resultBytes);
        } catch (Exception var4) {
            return null;
        }
    }

    private String sha256Digest(String str) {
        return digest(str, "SHA-256", "UTF-8");
    }

    private String digest(String str, String alg, String encoding) {
        try {
            byte[] data = str.getBytes(encoding);
            MessageDigest md = MessageDigest.getInstance(alg);
            return Hex.encodeHexString(md.digest(data));
        } catch (Exception var5) {
            throw new RuntimeException("digest fail!", var5);
        }
    }

    public String buildLoginCountCacheKey(String username) {
        return SERVICE_NAME + ":LoginCount:" + username;
    }

    /**
     * 登录失败次数校验 密码校验之前
     *  惩时 + 验证码校验功能
     * @param dto
     */
    public void checkLoginErrorCount(LoginDto dto) {
        String cacheCountKey = buildLoginCountCacheKey(dto.getUsername());

        String loginCountStr = stringRedisTemplate.opsForValue().get(cacheCountKey);
        int loginCount = 0;
        if (StringUtils.hasText(loginCountStr)) {
            loginCount = Integer.parseInt(loginCountStr);
        }
        if (loginCount < PWD_ERROR_COUNT) {
            return;
        }
        //开启验证码时提示验证码登录
        checkImageVerifyCode(dto);
    }

    private void checkImageVerifyCode(LoginDto dto) {
        // 检查图片验证码
        if (StringUtils.isEmpty(dto.getEncryptedMeta()) || StringUtils.isEmpty(dto.getVerifyCode())) {
            throw new MedicalBusinessException(new Response(ERROR_CODE_VERIFY_CODE_NEED,
                    "登录失败次数过多 请输入验证码", "登录失败次数过多 请输入验证码", captchaService.get()));
        }
        if (!captchaService.validate(dto.getEncryptedMeta(), dto.getVerifyCode())) {
            throw new MedicalBusinessException(new Response(ERROR_CODE_VERIFY_CODE_ERROR,
                    "验证码错误请重新输入", "验证码错误请重新输入", captchaService.get()));
        }
    }
}
