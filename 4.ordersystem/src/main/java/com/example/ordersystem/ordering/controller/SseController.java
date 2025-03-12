package com.example.ordersystem.ordering.controller;

import com.example.ordersystem.ordering.dto.OrderListResDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class SseController {

//    사용자 연결정보를 변수로 관리
//    ConcurrentHashMap : thread-safe한 hashmap(동시성 이슈 발생 X)
    private final Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();

//    사용자의 서버 연결 요청을 통해 연결정보에 등록
    @GetMapping("/subscribe")
    public SseEmitter subscribe(){
//        연결 객체 생성
        SseEmitter sseEmitter = new SseEmitter(14400*60*1000L); //10일정도 emitter 유효시간 설정
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        emitterMap.put(email, sseEmitter);
        try{
            sseEmitter.send(SseEmitter.event().name("connect").data("연결완료"));
        }catch (IOException e){
            e.printStackTrace();
        }
        return sseEmitter;
    }

    @GetMapping("/unsubscribe")
    public void unSubscribe(){
//        연결 객체 생성
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        emitterMap.remove(email);
    }

//    특정 사용자에게 메시지(데이터) 발송
    public void publishMessage(OrderListResDto dto, String email){
        SseEmitter sseEmitter = emitterMap.get(email);
        try{
            sseEmitter.send(SseEmitter.event().name("ordered").data(dto));
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
