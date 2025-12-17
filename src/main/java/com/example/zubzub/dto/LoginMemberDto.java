    package com.example.zubzub.dto;


    import lombok.*;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
    public class LoginMemberDto {
        private Long id;
        private String email;
        private String pwd;
        private String nickname;
    }
