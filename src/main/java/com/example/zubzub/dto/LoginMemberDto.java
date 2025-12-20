    package com.example.zubzub.dto;


    import lombok.*;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
    public class LoginMemberDto {
        private Long id;
        private String email;
        private String name;
        private String nickname;
        private String accessToken;
        private String refreshToken;
    }


