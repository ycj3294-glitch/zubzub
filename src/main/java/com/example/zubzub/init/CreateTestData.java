package com.example.zubzub.init;

import com.example.zubzub.dto.AuctionCreateDto;
import com.example.zubzub.dto.AuctionResDto;
import com.example.zubzub.entity.AuctionType;
import com.example.zubzub.entity.Member;
import com.example.zubzub.repository.AuctionRepository;
import com.example.zubzub.repository.MemberRepository;
import com.example.zubzub.service.AuctionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Transactional
@Component
@RequiredArgsConstructor
public class CreateTestData implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final AuctionRepository auctionRepository;
    private final AuctionService auctionService;
    private final PasswordEncoder passwordEncoder;

    private final Random random = new Random();

    @Override
    public void run(String... args) throws Exception {

        Member me = new Member();
        me.setEmail("dfgr56@naver.com");
        me.setPwd(passwordEncoder.encode("!Q2w3e4r"));
        me.setName("이용현");
        me.setNickname("이용현");
        me.setAddr("서울시 강남구");
        me.setCredit(50000000 + random.nextInt(5000));
        me.setAdmin(false);
        me.setMemberStatus("ACTIVE");
        memberRepository.save(me);

        Member adme = new Member();
        adme.setEmail("dfgr567@naver.com");
        adme.setPwd(passwordEncoder.encode("!Q2w3e4r"));
        adme.setName("이용현1");
        adme.setNickname("이용현1");
        adme.setAddr("서울시 강남구");
        adme.setCredit(50000000 + random.nextInt(5000));
        adme.setAdmin(true);
        adme.setMemberStatus("ACTIVE");
        memberRepository.save(adme);

        Member ad = new Member();
        ad.setEmail("ycj3294@naver.com");
        ad.setPwd(passwordEncoder.encode("aA1!23456"));
        ad.setName("양찬종");
        ad.setNickname("Y");
        ad.setAddr("서울시");
        ad.setCredit(50000000 + random.nextInt(5000));
        ad.setAdmin(true);
        ad.setMemberStatus("ACTIVE");
        memberRepository.save(ad);

        Member as = new Member();
        as.setEmail("bugsteam9912@gmail.com");
        as.setPwd(passwordEncoder.encode("aA1!23456"));
        as.setName("이현수");
        as.setNickname("현현현현");
        as.setAddr("서울시");
        as.setCredit(50000000 + random.nextInt(5000));
        as.setAdmin(true);
        as.setMemberStatus("ACTIVE");
        memberRepository.save(as);

        // ===============================
        // 1️⃣ 회원 10명 생성
        // ===============================
        List<Member> members = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Member member = new Member();
            member.setEmail("user" + i + "@example.com");
            member.setPwd("password" + i);
            member.setName("회원" + i);
            member.setNickname("nick" + i);
            member.setAddr("서울시 강남구 " + i + "번지");
            member.setCredit(random.nextInt(5000));
            member.setAdmin(i == 1);
            member.setMemberStatus("ACTIVE");
            members.add(member);
        }
        memberRepository.saveAll(members);
        memberRepository.flush();

        System.out.println("✅ 회원 10명 생성 완료");

        // ===============================
        // 2️⃣ 경매 생성
        // ===============================
        int auctionCount = 0;
        String[] categories = {"ELECTRONICS", "BOOK", "GAME", "FURNITURE", "CLOTHES"};
        String[] items = {"아이폰", "닌텐도", "책", "의자", "셔츠", "노트북", "마우스"};

        LocalDateTime today = LocalDateTime.now();

        // 🔹 하루에 프리미엄 3개씩, 5일치 생성
        for (int d = 0; d < 5; d++) {

            // 해당 날짜의 시작 (00:00:00)
            LocalDateTime dayStart = today.minusDays(d)
                    .withHour(0).withMinute(0).withSecond(0);

            // 해당 날짜의 끝 (23:59:59)
            LocalDateTime dayEnd = dayStart
                    .withHour(23).withMinute(59).withSecond(59);

            for (int i = 1; i <= 3; i++) {

                // startTime은 0시 ~ 21시 사이 (2시간 경매 보장)
                int startHour = random.nextInt(22); // 0~21
                LocalDateTime startTime = dayStart.plusHours(startHour);

                LocalDateTime endTime = startTime.plusHours(2); // 항상 같은 날 안

                AuctionCreateDto dto = AuctionCreateDto.builder()
                        .auctionType(AuctionType.MAJOR)
                        .category(categories[random.nextInt(categories.length)])
                        .sellerId(1 + random.nextLong(members.size()))
                        .itemName("프리미엄 " + items[random.nextInt(items.length)] + " " + i)
                        .itemDesc("설명 " + i)
                        .startPrice(10000 + random.nextInt(99000))
                        .minBidUnit(100)
                        .itemImg("http://placehold.co/600x400")
                        .startTime(startTime)
                        .endTime(endTime)
                        .build();

                AuctionResDto resDto = auctionService.createAuction(dto);
                auctionService.approveAuction(resDto.getId());
                auctionService.setTime(resDto.getId(), startTime, endTime);

                auctionCount++;
            }
        }

        // 🔹 일반 경매 15개 랜덤 생성
        for (int i = 1; i <= 15; i++) {

            LocalDateTime baseDay = today.minusDays(random.nextInt(5))
                    .withHour(0).withMinute(0).withSecond(0);

            LocalDateTime startTime = baseDay.plusHours(random.nextInt(24));
            LocalDateTime endTime = startTime.plusHours(1 + random.nextInt(72)); // 최소 1시간

            AuctionCreateDto dto = AuctionCreateDto.builder()
                    .auctionType(AuctionType.MINOR)
                    .category(categories[random.nextInt(categories.length)])
                    .sellerId(1 + random.nextLong(members.size()))
                    .itemName("일반 " + items[random.nextInt(items.length)] + " " + i)
                    .itemDesc("설명 " + i)
                    .startPrice(10000 + random.nextInt(99000))
                    .minBidUnit(100)
                    .itemImg("http://placehold.co/600x400") // 필요시 랜덤 이미지나 기본값 지정
                    .startTime(baseDay.minusHours(random.nextInt(24)))
                    .endTime(baseDay.plusHours(random.nextInt(72)))
                    .build();

            auctionService.createAuction(dto);

            auctionCount++;
        }

        System.out.println("✅ 경매 " + auctionCount + "개 생성 완료");
        System.out.println("✅ 테스트 데이터 입력 완료!");
    }
}
