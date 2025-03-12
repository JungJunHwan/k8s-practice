package com.example.ordersystem.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;



@Component
public class JwtTokenProvider {
    @Value("${jwt.expiration}")
    private int expiration;
    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.expirationRt}")
    private int expirationRt;
    @Value("${jwt.secretKeyRt}")
    private String secretKeyRt;

    private Key ENCRYPT_SECRET_KEY;
    private Key ENCRYPT_RT_SECRET_KEY;

    //    생성자가 호출되고, 스프링 빈이 만들어진 직후에 아래 메서드 바로 샐행하는 어노테이션
    @PostConstruct
    public void init(){
        ENCRYPT_SECRET_KEY = new SecretKeySpec(java.util.Base64.getDecoder().decode(secretKey), SignatureAlgorithm.HS512.getJcaName());
        ENCRYPT_RT_SECRET_KEY = new SecretKeySpec(java.util.Base64.getDecoder().decode(secretKey), SignatureAlgorithm.HS512.getJcaName());
    }


    public String createToken(String email, String role){
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role",role); //롤뿐만아니라 네임,등등 마음대로 넣을 수 있다. 그러면 그걸 프론트엔드에서 사용할 수 있음 토큰에서 꺼내서

        Date now = new Date();

        //        claims는 사용자정보(payload정보)
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt((now))
                .setExpiration(new Date(now.getTime()+expiration*60*1000L)) //밀리초단위로 넣어야함.여기는 30분세팅(30*60*1000밀리초), 편의상 30을 .yml설정에서 가지고 온 것임
                .signWith(ENCRYPT_SECRET_KEY) //암호화된 키를 매개변수로 요구
                .compact();
        return token;
    }

    public String createRefreshToken(String email, String role){
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role",role); //롤뿐만아니라 네임,등등 마음대로 넣을 수 있다. 그러면 그걸 프론트엔드에서 사용할 수 있음 토큰에서 꺼내서

        Date now = new Date();

        //        claims는 사용자정보(payload정보)
        String refreshToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt((now))
                .setExpiration(new Date(now.getTime()+expirationRt*60*1000L)) //밀리초단위로 넣어야함.여기는 30분세팅(30*60*1000밀리초), 편의상 30을 .yml설정에서 가지고 온 것임
                .signWith(ENCRYPT_RT_SECRET_KEY) //암호화된 키를 매개변수로 요구
                .compact();
        return refreshToken;
    }



}