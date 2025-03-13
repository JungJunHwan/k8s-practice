package com.example.ordersystem.member.controller;

import com.example.ordersystem.common.auth.JwtTokenProvider;
import com.example.ordersystem.common.dto.LoginDto;
import com.example.ordersystem.member.domain.Member;
import com.example.ordersystem.member.dto.MemberResDto;
import com.example.ordersystem.member.dto.MemberSaveReqDto;
import com.example.ordersystem.member.service.MemberService;
import jakarta.validation.Valid;
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

@RestController
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    public MemberController(MemberService memberService, JwtTokenProvider jwtTokenProvider) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
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
        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id", member.getId());
        loginInfo.put("token", jwtToken);
        return new ResponseEntity<>(loginInfo, HttpStatus.OK);
    }

    @GetMapping("/myinfo")
    public ResponseEntity<?> myInfo(){
        MemberResDto memberResDto = memberService.myInfo();
        return new ResponseEntity<>(memberResDto, HttpStatus.OK);
    }
}
