package com.example.zubzub.service;

import com.example.zubzub.dto.MemberResDto;
import com.example.zubzub.dto.MemberSignupReqDto;

import java.util.List;

public interface MemberService {

    boolean checkPassword(Long id, String rawPassword);
    boolean isNicknameExists(String nickname);
    boolean isEmailExists(String email);

    Long signup(MemberSignupReqDto req);
    MemberResDto login(String email, String rawPwd);

    MemberResDto getByEmail(String email);
    MemberResDto getById(Long id);

    List<MemberResDto> list(); // 기존 전체 조회
    List<MemberResDto> getAll(); // 관리자용 전체 조회 (list() 재사용 가능)

    boolean delete(Long id);
    boolean update(MemberSignupReqDto req, Long id);

    void setActive(Long id, boolean active);
    void updateStatus(Long id, boolean active); // 추가!!

    void addPoint(Long memberId, int point);
    // 회원가입 임시 저장
    void savePendingMember(MemberSignupReqDto req);

    // 인증 완료 후 DB에 최종 저장/활성화
    void activateMember(String email);
    // 이메일로 인증 코드 발송 후 Map에 저장
    String sendPasswordResetCode(String email);

    // Map에 있는 코드 확인 후 비밀번호 재설정
    boolean resetPassword(String email, String code, String newPassword);


}


