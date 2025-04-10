package com.example.ordersystem.member.domain;

import com.example.ordersystem.common.domain.BaseTimeEntity;
import com.example.ordersystem.member.dto.MemberResDto;
import com.example.ordersystem.ordering.domain.Ordering;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@Getter
@AllArgsConstructor
@Builder
@Entity
@ToString
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    @OneToMany(mappedBy = "member")
    private List<Ordering> orderingList;

    public MemberResDto resDtoFromEntity(){
        return MemberResDto.builder()
                .id(this.id)
                .name(this.name)
                .email(this.email)
                .orderCount(this.orderingList.size())
                .build();
    }
}
