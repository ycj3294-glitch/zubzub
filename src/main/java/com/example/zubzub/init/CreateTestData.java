package com.example.zubzub.init;

import com.example.zubzub.entity.Auction;
import com.example.zubzub.entity.AuctionStatus;
import com.example.zubzub.entity.AuctionType;
import com.example.zubzub.entity.Member;
import com.example.zubzub.repository.AuctionRepository;
import com.example.zubzub.repository.MemberRepository;
import com.example.zubzub.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Transactional
@Component
@RequiredArgsConstructor
public class CreateTestData implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final AuctionRepository auctionRepository;
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
        me.setCredit(random.nextInt(5000));
        me.setAdmin(false);
        me.setMemberStatus("ACTIVE");
        memberRepository.save(me);

        Member ad = new Member();
        ad.setEmail("ycj3294@naver.com");
        ad.setPwd(passwordEncoder.encode("aA1!23456"));
        ad.setName("양찬종");
        ad.setNickname("Y");
        ad.setAddr("서울시");
        ad.setCredit(random.nextInt(5000));
        ad.setAdmin(true);
        ad.setMemberStatus("ACTIVE");
        memberRepository.save(ad);

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
        System.out.println("✅ 회원 10명 생성 완료");

        // ===============================
        // 2️⃣ 경매 생성
        // ===============================
        List<Auction> auctions = new ArrayList<>();
        String[] categories = {"ELECTRONICS", "BOOK", "GAME", "FURNITURE", "CLOTHES"};
        String[] items = {"아이폰", "닌텐도", "책", "의자", "셔츠", "노트북", "마우스"};

        LocalDateTime today = LocalDateTime.now();

        // 🔹 하루에 프리미엄 3개씩, 5일치 생성
        for (int d = 0; d < 5; d++) {
            LocalDateTime dayStart = today.minusDays(d);
            LocalDateTime dayEnd = dayStart.withHour(23).withMinute(59).withSecond(59); // 그 날 끝

            for (int i = 1; i <= 3; i++) {
                // 시작 시간은 dayStart ~ dayEnd-2h 범위에서 랜덤
                long maxStartHour = Math.max(0, dayEnd.getHour() - 2); // 종료시간이 그 날을 넘지 않도록
                LocalDateTime startTime = dayStart.plusHours(random.nextInt((int) maxStartHour + 1));
                LocalDateTime endTime = startTime.plusHours(2); // 종료시간은 항상 2시간 차이

                Auction auction = Auction.builder()
                        .auctionType(AuctionType.MAJOR)
                        .category(categories[random.nextInt(categories.length)])
                        .seller(members.get(random.nextInt(members.size())))
                        .itemName("프리미엄 " + items[random.nextInt(items.length)] + " " + i)
                        .itemDesc("설명 " + i)
                        .startPrice(10000 + random.nextInt(990000))
                        .minBidUnit(100)
                        .auctionStatus(AuctionStatus.values()[random.nextInt(AuctionStatus.values().length)])
                        .startTime(startTime)
                        .endTime(endTime)
                        .build();

                auctions.add(auction);
            }
        }

        // 🔹 일반 경매 15개 랜덤 생성
        for (int i = 1; i <= 15; i++) {
            LocalDateTime randomDay = today.minusDays(random.nextInt(5));
            Auction auction = Auction.builder()
                    .auctionType(AuctionType.MINOR)
                    .category(categories[random.nextInt(categories.length)])
                    .seller(members.get(random.nextInt(members.size())))
                    .itemName("일반 " + items[random.nextInt(items.length)] + " " + i)
                    .itemDesc("설명 " + i)
                    .startPrice(10000 + random.nextInt(990000))
                    .minBidUnit(100)
                    .auctionStatus(AuctionStatus.values()[random.nextInt(AuctionStatus.values().length)])
                    .startTime(randomDay.minusHours(random.nextInt(24)))
                    .endTime(randomDay.plusHours(random.nextInt(72)))
                    .build();
            auctions.add(auction);
        }

        auctionRepository.saveAll(auctions);
        System.out.println("✅ 경매 " + auctions.size() + "개 생성 완료");
        System.out.println("✅ 테스트 데이터 입력 완료!");
    }
}
