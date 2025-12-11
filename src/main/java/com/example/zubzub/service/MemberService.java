package com.example.zubzub.service;

import com.example.zubzub.dto.MemberResDto;
import com.example.zubzub.dto.MemberSignupReqDto;

import java.util.List;

public interface MemberService {

    boolean isNicknameExists(String nickname);
    boolean isEmailExists(String email);

    Long signup(MemberSignupReqDto req);
    MemberResDto login(String email, String rawPwd);

    MemberResDto getByEmail(String email);
    MemberResDto getById(Long id);

    List<MemberResDto> list();

    boolean delete(Long id);
    boolean update(MemberSignupReqDto req, Long id);

    void addPoint(Long memberId, int point);
}
