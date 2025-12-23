package com.example.zubzub.service.impl;

import com.example.zubzub.dto.LoginMemberDto;
import com.example.zubzub.dto.MemberResDto;
import com.example.zubzub.dto.MemberSignupReqDto;
import com.example.zubzub.dto.MemberUpdateReqDto;
import com.example.zubzub.entity.Auction;
import com.example.zubzub.entity.Member;
import com.example.zubzub.repository.MemberRepository;
import com.example.zubzub.security.JwtUtil;
import com.example.zubzub.service.MailService;
import com.example.zubzub.service.MemberService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j

public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    // 이메일 인증 대기 회원
    private final Map<String, MemberSignupReqDto> pendingMembers = new HashMap<>();

    // 비밀번호 재설정 대기
    private final Map<String, String> pendingPasswordResets = new HashMap<>();

    /* =========================
       회원가입 (이메일 인증)
       ========================= */


    @Override
    public void savePendingMember(MemberSignupReqDto req) {
        pendingMembers.put(req.getEmail(), req);
    }

    @Transactional
    @Override
    public void activateMember(String email) {

        MemberSignupReqDto req = pendingMembers.remove(email);
        if (req == null) return;

        Member member = new Member();
        member.setEmail(req.getEmail());
        member.setPwd(passwordEncoder.encode(req.getPwd()));
        member.setName(req.getName());
        member.setNickname(req.getNickname());

        // member.setAddr(...)
        // member.setProfileImg(...)

        member.setCredit(0);
        member.setAdmin(false);
        member.setMemberStatus("ACTIVE");

        memberRepository.save(member);
    }
    @Transactional
    @Override
    public void completeSignup(MemberSignupReqDto req) {
        Member member = new Member();
        member.setEmail(req.getEmail());
        member.setPwd(passwordEncoder.encode(req.getPwd()));
        member.setNickname(req.getNickname());
        member.setName(req.getName());
        member.setMemberStatus("ACTIVE");
        member.setAdmin(false);
        memberRepository.save(member);
    }


    /* =========================
       로그인
       ========================= */

    @Override
    public MemberResDto login(String email, String rawPwd) {
        log.info("로우패스워드 : {}", rawPwd);

        Member member = memberRepository.findByEmail(email);
        if (member == null) return null;
        log.info("멤버는있음 : {}", member);

        // 🔥 상태 체크
        if (!"ACTIVE".equals(member.getMemberStatus())) {
            return null;
        }
        if (!passwordEncoder.matches(rawPwd, member.getPwd())) {
            return null;
        }

        return new MemberResDto(member);
    }

    public LoginMemberDto loginWithJwt(Authentication authentication) {
        if(authentication == null) return null;

        // principal에서 유저 정보 꺼내기
        Member member = (Member) authentication.getPrincipal();

        String accessToken = JwtUtil.generateLoginToken(member.getEmail(), member.getId(), member.isAdmin());
        String refreshToken = "";

        // DTO 반환 (쿠키는 컨트롤러에서 설정)
        return new LoginMemberDto(member.getId(), member.getEmail(), member.getName(), member.getNickname(), accessToken, refreshToken);
    }

    public LoginMemberDto loginWithPwd(String email, String rawPwd) {
        MemberResDto member = login(email, rawPwd);
        if (member == null) return null;

        String accessToken = JwtUtil.generateLoginToken(member.getEmail(), member.getId(), member.isAdmin());
        String refreshToken = JwtUtil.generateRefreshToken(member.getEmail(), member.getId(), member.isAdmin());

        // DTO 반환 (쿠키는 컨트롤러에서 설정)
        return new LoginMemberDto(member.getId(), member.getEmail(), member.getName(), member.getNickname(), accessToken, refreshToken);
    }




    /* =========================
       조회
       ========================= */

    @Override
    public MemberResDto getById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원"));
        return new MemberResDto(member);
    }

    @Override
    public MemberResDto getByEmail(String email) {
        Member member = memberRepository.findByEmail(email);
        if (member == null) throw new IllegalArgumentException("존재하지 않는 회원");
        return new MemberResDto(member);
    }

    @Override
    public List<MemberResDto> list() {
        return memberRepository.findAll()
                .stream()
                .map(MemberResDto::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<MemberResDto> getAll() {
        return list();
    }

    /* =========================
       회원 정보 수정
       ========================= */

    /* =========================
       삭제 (Soft Delete)
       ========================= */

    @Override
    public boolean delete(Long id) {

        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원"));

        member.setMemberStatus("DELETE_REQ");
        memberRepository.save(member);
        return true;
    }



    /* =========================
       포인트
       ========================= */

    @Override
    public void addPoint(Long memberId, int point) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원"));

        member.setCredit(member.getCredit() + point);
        memberRepository.save(member);
    }

    /* =========================
       비밀번호 검증 / 재설정
       ========================= */

    @Override
    public boolean checkPassword(Long id, String rawPassword) {

        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원"));

        return passwordEncoder.matches(rawPassword, member.getPwd());
    }

    @Override
    public String sendPasswordResetCode(String email) {

        Member member = memberRepository.findByEmail(email);
        if (member == null) return null;

        String code = String.format("%06d", new Random().nextInt(999999));
        pendingPasswordResets.put(email, code);
        mailService.sendVerificationEmailHtml(email, code);

        return code;
    }

    @Override
    public boolean resetPassword(String email, String code, String newPassword) {

        String savedCode = pendingPasswordResets.get(email);
        if (savedCode == null || !savedCode.equals(code)) {
            return false;
        }

        Member member = memberRepository.findByEmail(email);
        member.setPwd(passwordEncoder.encode(newPassword));
        memberRepository.save(member);

        pendingPasswordResets.remove(email);
        return true;
    }
    /* =========================
   중복 체크
   ========================= */


    @Override
    public boolean update(MemberUpdateReqDto req, Long id) {
        return false;
    }

    @Override
    public boolean update(MemberSignupReqDto req, Long id) {
        return false;
    }

    @Override
    public boolean isEmailExists(String email) {
        return memberRepository.existsByEmail(email);
    }

    @Override
    public boolean isNicknameExists(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

/* =========================
   관리자 상태 변경
   ========================= */

    @Override
    public void updateStatus(Long id, boolean active) {

        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원"));

        if (active) {
            member.setMemberStatus("ACTIVE");
        } else {
            member.setMemberStatus("SUSPENDED");
        }

        memberRepository.save(member);
    }

}

