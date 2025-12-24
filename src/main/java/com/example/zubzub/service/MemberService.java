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
        // 1. 회원 존재 여부 확인
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        // 2. 닉네임 수정 (값이 들어왔을 때만)
        if (req.getNickname() != null && !req.getNickname().isEmpty()) {
            member.setNickname(req.getNickname());
        }

        // 3. 이름 수정 (필요하다면)
        if (req.getName() != null && !req.getName().isEmpty()) {
            member.setName(req.getName());
        }

        // 4. 비밀번호 수정 (중요!)
        // 프론트에서 'pwd'라는 이름으로 보낸다면 DTO에도 pwd가 있어야 합니다.
        if (req.getPwd() != null && !req.getPwd().isEmpty()) {
            // 암호화해서 저장 (인코딩 필수)
            member.setPwd(passwordEncoder.encode(req.getPwd()));
        }

        // Dirty Checking으로 인해 별도의 save 호출 없이도 트랜잭션 종료 시 DB에 반영됩니다.
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

        // 🔍 로그 추가: 코드가 왜 안 맞는지 눈으로 확인
        log.info("[비번재설정] 입력코드: {}, 저장된코드: {}", code, savedCode);

        if (savedCode == null || !savedCode.equals(code)) {
            log.error("❌ 인증번호 불일치로 실패!");
            return false;
        }

        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            log.error("❌ 유저를 찾을 수 없음: {}", email);
            return false;
        }

        // ✅ 암호화 저장
        member.setPwd(passwordEncoder.encode(newPassword));
        memberRepository.save(member); // 명시적으로 저장 명령 내리기

//        pendingPasswordResets.remove(email);
        log.info("✅ 비밀번호 변경 성공! 이제 로그인 해보세요.");
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
