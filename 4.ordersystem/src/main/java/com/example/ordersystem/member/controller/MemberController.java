package com.example.ordersystem.member.controller;

import com.example.ordersystem.common.auth.JwtTokenProvider;
import com.example.ordersystem.common.dto.LoginDto;
import com.example.ordersystem.member.domain.Member;
import com.example.ordersystem.member.dto.MemberResDto;
import com.example.ordersystem.member.dto.MemberSaveReqDto;
import com.example.ordersystem.member.dto.UserRefreshDto;
import com.example.ordersystem.member.service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/member")
public class MemberController {
    @Value("${jwt.secretKeyRt}")
    private String secretKeyRt;

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;

    public MemberController(MemberService memberService, JwtTokenProvider jwtTokenProvider, RedisTemplate<String, Object> redisTemplate) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/create")
    public ResponseEntity<?> save(@Valid @RequestBody MemberSaveReqDto memberSaveReqDto){
        Long memberId = memberService.save(memberSaveReqDto).getId();

        return new ResponseEntity<>(memberId, HttpStatus.CREATED);
    }

    @GetMapping("list")
    @PreAuthorize("hasRole('ADMIN')") //가장 편한 방법, 'ROLE_' 붙일 필요 없음, filter에서 예외 발생
    public ResponseEntity<?> list(){
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if(!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))){
//            throw new AccessDeniedException("권한 없음");
//        }

        List<MemberResDto> memberListResDtoList = memberService.findAll();
        return new ResponseEntity<>(memberListResDtoList, HttpStatus.OK);
    }

    @PostMapping("/doLogin")
    public ResponseEntity<?> doLogin(@RequestBody LoginDto loginDto){
//        email, password 검증
        Member member = memberService.login(loginDto);

//        토큰 생성 및 검증
        String jwtToken = jwtTokenProvider.createToken(member.getEmail(), member.getRole().toString());
        String refreshToken = jwtTokenProvider.createToken(member.getEmail(), member.getRole().toString());
        redisTemplate.opsForValue().set(member.getEmail(), refreshToken, 200, TimeUnit.DAYS);
        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id", member.getId());
        loginInfo.put("token", jwtToken);
        loginInfo.put("refreshToken", refreshToken);
        return new ResponseEntity<>(loginInfo, HttpStatus.OK);
    }

    @GetMapping("/myinfo")
    public ResponseEntity<?> myInfo(){
        MemberResDto memberResDto = memberService.myInfo();
        return new ResponseEntity<>(memberResDto, HttpStatus.OK);
    }

    //** 리프레시 토큰 발급
    @PostMapping("/refresh-token")
//   사용자가 만료된 AccessToken을 갱신할 때 호출하여 레디스에서 사용자의 RefreshToken을 검증하고 새로운 AccessToken을 지급
    public ResponseEntity<?> reGenerateAccessToken(@RequestBody UserRefreshDto dto) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKeyRt)
                .build()
                .parseClaimsJws(dto.getRefreshToken())
                .getBody();

        Object refreshTokenOfDto = redisTemplate.opsForValue().get(claims.getSubject());//레디스에서 키값(loginId)에 해당하는 밸류 가지고옴
        if (refreshTokenOfDto == null || !refreshTokenOfDto.toString().equals(dto.getRefreshToken())) {//레디스에 해당 키가 없거나 레디스에 있는 값과 일치하지 않으면 accessToken재발급 불가
            return new ResponseEntity<>("cannot recreate accessToken", HttpStatus.BAD_REQUEST);
        }

        String token = jwtTokenProvider.createRefreshToken(claims.getSubject(), claims.get("role").toString());
        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("token", token);
        return new ResponseEntity<>("accessToken is recreated", HttpStatus.CREATED);
    }
}
