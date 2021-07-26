package com.iflytek.integrated.platform.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

/**
 * @Author ganghuang6
 * @Date 2021/7/22
 */
public class JwtTokenUtils {

    private static final String SECRET = "integrated-platform";
    private static final String ISS = "iflytek";
    private static final String SUBJECT = "sjdjpt";

    //默认token有限期为1天
    public static final Long EXPIRATION = 60*60*24*1L;

    // 创建token
    public static String createToken(String username) {
        return Jwts.builder()
                .signWith(SignatureAlgorithm.HS256, SECRET)//签发算法及密钥
                .claim("user", username)
//                .claim("password", password)
                .setIssuer(ISS)//签发者
                .setSubject(SUBJECT)//主题
                .setIssuedAt(new Date())//签发时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION * 1000))//过期时间
                .compact();
    }

    /**
     * 解析jwt
     * @param jsonWebToken
     * @return
     */
    public static Claims parseJWT(String jsonWebToken) throws Exception {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(jsonWebToken).getBody();
            return claims;
        } catch (Exception e){
            throw new Exception("访问token无效");
        }
    }

    /**
     * 从token中获取用户名
     * @param token
     * @return
     */
    public static Object getUsername(String token) throws Exception {
        return parseJWT(token).get("user");
    }

    /**
     * token是否过期
     * @param token
     * @param base64Security
     * @return
     */
    public static boolean isExpiration(String token, String base64Security) throws Exception {
        return parseJWT(token).getExpiration().before(new Date());
    }

}
