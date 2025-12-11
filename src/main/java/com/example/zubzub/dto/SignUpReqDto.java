package com.example.zubzub.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor  // 매개변수가 없는 기본 생성자 생성, 내부적으로 JSON 역직렬화 시 사용됨
public class SignUpReqDto {
    private String email;
    private String pwd;
    private String name;
}
