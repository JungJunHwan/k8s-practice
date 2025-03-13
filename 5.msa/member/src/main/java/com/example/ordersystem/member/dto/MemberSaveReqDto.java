package com.example.ordersystem.member.dto;

import com.example.ordersystem.member.domain.Member;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class MemberSaveReqDto {
    private String name;
    private String email;
    private String password;

    public Member toEntity(String encodedPassword){
        return Member.builder()
                .name(this.name)
                .email(this.email)
                .password(encodedPassword)
                .build();
    }
}
