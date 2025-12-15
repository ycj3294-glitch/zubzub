package com.example.zubzub.service.impl;

import com.example.zubzub.dto.MemberResDto;
import com.example.zubzub.dto.MemberSignupReqDto;
import com.example.zubzub.entity.Member;
import com.example.zubzub.repository.MemberRepository;
import com.example.zubzub.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class MemberServiceImpl implements MemberService {


    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final Map<String, MemberSignupReqDto> pendingMembers = new HashMap<>();

    @Override
    public void savePendingMember(MemberSignupReqDto req) {
        pendingMembers.put(req.getEmail(), req); // 임시 저장 (메모리)
    }

    @Override
    public void activateMember(String email) {
        MemberSignupReqDto req = pendingMembers.remove(email);
        if(req != null) {
            Member member = new Member();
            member.setEmail(req.getEmail());
            member.setPwd(passwordEncoder.encode(req.getPwd()));
            member.setNickname(req.getNickname());
            member.setGrade(req.getGrade());
            member.setProfileImg(req.getProfileImg());
            memberRepository.save(member);
        }
    }
    @Override
    public List<MemberResDto> getAll() {
        return list();
    }

    @Override
    public boolean isNicknameExists(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    @Override
    public boolean isEmailExists(String email) {
        return memberRepository.existsByEmail(email);
    }

    // 회원가입
    @Override
    public Long signup(MemberSignupReqDto req) {

        if (memberRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        if (memberRepository.existsByNickname(req.getNickname())) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }

        String encodedPwd = passwordEncoder.encode(req.getPwd());

        Member member = new Member();
        member.setEmail(req.getEmail());
        member.setPwd(encodedPwd);
        member.setNickname(req.getNickname());
        member.setCredit(0);

        return memberRepository.save(member).getId();
    }

    // 로그인
    @Override
    public MemberResDto login(String email, String rawPwd) {
        Member member = memberRepository.findByEmail(email);

        if (member == null) return null;

        if (!passwordEncoder.matches(rawPwd, member.getPwd())) {
            return null;
        }

        return new MemberResDto(member);
    }

    @Override
    public MemberResDto getByEmail(String email) {
        Member member = memberRepository.findByEmail(email);
        if (member == null) throw new IllegalArgumentException("존재하지 않는 회원입니다.");
        return new MemberResDto(member);
    }

    @Override
    public MemberResDto getById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
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
    public boolean delete(Long id) {
        if (!memberRepository.existsById(id)) {
            throw new IllegalArgumentException("존재하지 않는 회원입니다.");
        }
        memberRepository.deleteById(id);
        return true;
    }

    @Override
    public boolean update(MemberSignupReqDto req, Long id) {

        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        if (req.getEmail() != null) member.setEmail(req.getEmail());
        if (req.getNickname() != null) member.setNickname(req.getNickname());
        if (req.getPwd() != null && !req.getPwd().isEmpty()) {
            member.setPwd(passwordEncoder.encode(req.getPwd()));
        }

        memberRepository.save(member);
        return true;
    }

    @Override
    public void addPoint(Long memberId, int point) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        member.setCredit(member.getCredit() + point);
        memberRepository.save(member);
    }
    @Override
    public boolean checkPassword(Long id, String rawPassword) {

        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        return passwordEncoder.matches(rawPassword, member.getPwd());
    }
    @Override
    public void setActive(Long id, boolean active) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        member.setActive(active);
        memberRepository.save(member);
    }
    @Override
    public void updateStatus(Long id, boolean active) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        member.setActive(active);
        memberRepository.save(member);
    }



}

