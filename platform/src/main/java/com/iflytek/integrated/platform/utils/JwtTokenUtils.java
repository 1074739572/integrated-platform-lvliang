package com.iflytek.integrated.platform.utils;

import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.utils.DateUtils;
import com.iflytek.integrated.platform.common.Constant;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
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

    // 创建token
    public static String createToken(String username, String expiration) {
        return Jwts.builder()
                .signWith(SignatureAlgorithm.HS256, SECRET)//签发算法及密钥
                .claim("user", username)
//                .claim("password", password)
                .setIssuer(ISS)//签发者
                .setSubject(SUBJECT)//主题
                .setIssuedAt(new Date())//签发时间
                .setExpiration(new Date(System.currentTimeMillis() + Long.parseLong(expiration) * 1000))//过期时间
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
     * @return
     */
    public static boolean isExpiration(String token){
        try{
            return parseJWT(token).getExpiration().before(new Date());
        }catch (Exception e) {
            return true;
        }
    }

    /**
     * 刷新token过期时长
     * @param username
     * @param token
     * @param expiration
     * @return
     * @throws Exception
     */
    public static String refreshExp(String username,String token,long expiration) throws Exception {
        try {
            Claims claims = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();
            if(!username.equals(claims.get("user"))){
                throw new Exception("访问token无效");
            }
            //未过期
            return "";
        }catch (ExpiredJwtException e){
            //已过期，重新生成
            return Jwts.builder()
                    .signWith(SignatureAlgorithm.HS256, SECRET)//签发算法及密钥
                    .claim("user", username)
                    .setIssuer(ISS)//签发者
                    .setSubject(SUBJECT)//主题
                    .setIssuedAt(new Date())//签发时间
                    .setExpiration(new Date(System.currentTimeMillis() +  expiration))//过期时间
                    .compact();
        }catch (Exception e){
            throw new Exception("访问token无效");
        }
    }


    public static void main(String[] args) {
        try{
//            String token = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyIjoiYWRtaW4iLCJpc3MiOiJpZmx5dGVrIiwic3ViIjoic2pkanB0IiwiaWF0IjoxNjQ4NzIyOTM4LCJleHAiOjE2NDg4MDkzMzh9.KlduQIprQ09lpHPhFU0T7u0Kd5Kj60s-ZAE5tMkTgxg";
            String token = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyIjoiYWRtaW4iLCJpc3MiOiJpZmx5dGVrIiwic3ViIjoic2pkanB0IiwiaWF0IjoxNjQ5MjA2OTc3LCJleHAiOjE2NDk2Mzg5Nzd9.ZNUhnIKThyIXQ0i3ouwJfqzpJOv8evyqJ7Bw4KeRar8";
            token=token.substring(7);
//            Date exp = parseJWT(token).getExpiration();
//            System.out.println("exp:"+DateUtils.parseString(exp,DateUtils.STANDARD_FORMAT));

            String lastToken = "";
            if(JwtTokenUtils.isExpiration(token)){
                long exp = 86400l * 1000 * 5;
                lastToken = JwtTokenUtils.refreshExp("admin", token, exp);
            }
            if(lastToken == ""){
                //未过期，不创建新token
                System.out.println("未过期");
            }else{
                //已过期，返回新的token
                System.out.println("已刷新");
                System.out.println("token:"+lastToken);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
