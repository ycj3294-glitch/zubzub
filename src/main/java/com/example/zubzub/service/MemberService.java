package com.example.zubzub.service;

import com.example.zubzub.dto.MemberResDto;
import com.example.zubzub.dto.MemberSignupReqDto;
import com.example.zubzub.dto.MemberUpdateReqDto;

import java.util.List;

public interface MemberService {

    boolean update(MemberUpdateReqDto req, Long id);

    boolean update(MemberSignupReqDto req, Long id);

    /* =========================
           중복 체크
           ========================= */
    boolean isEmailExists(String email);
    boolean isNicknameExists(String nickname);

    /* =========================
       회원가입 (이메일 인증)
       ========================= */
    void savePendingMember(MemberSignupReqDto req);
    void activateMember(String email);

    /* =========================
       로그인
       ========================= */
    MemberResDto login(String email, String rawPwd);

    /* =========================
       조회
       ========================= */
    MemberResDto getById(Long id);
    MemberResDto getByEmail(String email);
    List<MemberResDto> list();
    List<MemberResDto> getAll(); // 관리자용 (list 재사용)

    /* =========================
           삭제 (Soft Delete)
           ========================= */
    boolean delete(Long id); // member_status = DELETE_REQ



    /* =========================
       크레딧
       ========================= */
    void addPoint(Long memberId, int credit);

    /* =========================
       비밀번호
       ========================= */
    boolean checkPassword(Long id, String rawPassword);
    String sendPasswordResetCode(String email);
    boolean resetPassword(String email, String code, String newPassword);
    void updateStatus(Long id, boolean active);
}


