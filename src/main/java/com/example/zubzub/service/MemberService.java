package com.example.zubzub.service;

import com.example.zubzub.dto.LoginMemberDto;
import com.example.zubzub.dto.MemberResDto;
import com.example.zubzub.dto.MemberSignupReqDto;
import com.example.zubzub.dto.MemberUpdateReqDto;
import com.example.zubzub.entity.Member;
import com.example.zubzub.repository.MemberRepository;
import com.example.zubzub.security.JwtUtil;
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
public class MemberService { // 인터페이스 없이 바로 서비스 클래스

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    // 임시 저장용 맵 (이건 나중에 Redis 등으로 바꿀 수도 있어)
    private final Map<String, MemberSignupReqDto> pendingMembers = new HashMap<>();
    private final Map<String, String> pendingPasswordResets = new HashMap<>();

    /* =========================
       1. 회원가입 및 인증
       ========================= */
    public void savePendingMember(MemberSignupReqDto req) {
        pendingMembers.put(req.getEmail(), req);
    }

    @Transactional
    public void activateMember(String email) {
        MemberSignupReqDto req = pendingMembers.remove(email);
        if (req != null) completeSignup(req);
    }

    @Transactional
    public void completeSignup(MemberSignupReqDto req) {
        Member member = new Member();
        member.setEmail(req.getEmail());
        member.setPwd(passwordEncoder.encode(req.getPwd()));
        member.setName(req.getName());
        member.setNickname(req.getNickname());
        member.setCredit(0);
        member.setAdmin(false);
        member.setMemberStatus("ACTIVE");
        memberRepository.save(member);
    }

    /* =========================
       2. 로그인 및 JWT 발급
       ========================= */
    public MemberResDto login(String email, String rawPwd) {
        Member member = memberRepository.findByEmail(email);
        if (member == null || !"ACTIVE".equals(member.getMemberStatus())) return null;
        if (!passwordEncoder.matches(rawPwd, member.getPwd())) return null;
        return new MemberResDto(member);
    }

    public LoginMemberDto loginWithPwd(String email, String rawPwd) {
        MemberResDto res = login(email, rawPwd);
        if (res == null) return null;

        String acc = JwtUtil.generateLoginToken(res.getEmail(), res.getId(), res.isAdmin());
        String ref = JwtUtil.generateRefreshToken(res.getEmail(), res.getId(), res.isAdmin());
        return new LoginMemberDto(res.getId(), res.getEmail(), res.getName(), res.getNickname(), acc, ref);
    }

    public LoginMemberDto loginWithJwt(Authentication authentication) {
        if (authentication == null) return null;
        Member member = (Member) authentication.getPrincipal();
        String acc = JwtUtil.generateLoginToken(member.getEmail(), member.getId(), member.isAdmin());
        return new LoginMemberDto(member.getId(), member.getEmail(), member.getName(), member.getNickname(), acc, "");
    }

    /* =========================
       3. 조회 (Read)
       ========================= */
    public MemberResDto getById(Long id) {
        return new MemberResDto(memberRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("회원 없음")));
    }

    public List<MemberResDto> list() {
        return memberRepository.findAll().stream()
                .map(MemberResDto::new)
                .collect(Collectors.toList());
    }

    public boolean isEmailExists(String email) { return memberRepository.existsByEmail(email); }
    public boolean isNicknameExists(String nickname) { return memberRepository.existsByNickname(nickname); }

    /* =========================
       4. 수정 및 삭제 (Update & Delete)
       ========================= */
    @Transactional
    public boolean update(MemberUpdateReqDto req, Long id) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        // Dirty Checking 활용 (필요한 값만 셋팅)
        if (req.getNickname() != null) member.setNickname(req.getNickname());
        if (req.getName() != null) member.setName(req.getName());
        return true;
    }

    @Transactional
    public void updateStatus(Long id, boolean active) {
        Member member = memberRepository.findById(id).orElseThrow();
        member.setMemberStatus(active ? "ACTIVE" : "SUSPENDED");
    }

    @Transactional
    public boolean delete(Long id) {
        Member member = memberRepository.findById(id).orElseThrow();
        member.setMemberStatus("DELETE_REQ");
        return true;
    }

    @Transactional
    public void addPoint(Long memberId, int credit) {
        Member member = memberRepository.findById(memberId).orElseThrow();
        member.setCredit(member.getCredit() + credit);
    }

    /* =========================
       5. 비밀번호 관련
       ======================== */
    public boolean checkPassword(Long id, String rawPassword) {
        Member member = memberRepository.findById(id).orElseThrow();
        return passwordEncoder.matches(rawPassword, member.getPwd());
    }

    public String sendPasswordResetCode(String email) {
        if (!isEmailExists(email)) return null;
        String code = String.format("%06d", new Random().nextInt(999999));
        pendingPasswordResets.put(email, code);
        mailService.sendVerificationEmailHtml(email, code);
        return code;
    }

    @Transactional
    public boolean resetPassword(String email, String code, String newPassword) {
        String savedCode = pendingPasswordResets.get(email);
        if (savedCode == null || !savedCode.equals(code)) return false;

        Member member = memberRepository.findByEmail(email);
        member.setPwd(passwordEncoder.encode(newPassword));
        pendingPasswordResets.remove(email);
        return true;
    }
    public List<MemberResDto> getAll() {
        return list(); // 이미 만들어둔 list()를 그대로 실행
    }

    public boolean chargeCoin(Long id, int coin) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("회원 없음"));
        member.setCredit(member.getCredit() + coin);
        memberRepository.save(member);
        return true;

    }
}
