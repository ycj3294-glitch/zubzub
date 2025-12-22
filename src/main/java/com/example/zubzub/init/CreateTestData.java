package com.example.zubzub.init;

import com.example.zubzub.entity.Auction;
import com.example.zubzub.entity.AuctionStatus;

import com.example.zubzub.entity.Member;

import com.example.zubzub.repository.AuctionRepository;
import com.example.zubzub.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
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

    private final Random random = new Random();

    @Override
    public void run(String... args) throws Exception {

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
            member.setCredit(random.nextInt(5000)); // 0 ~ 4999
            member.setAdmin(i == 1); // 첫 번째 회원은 관리자
            member.setMemberStatus("ACTIVE");

            members.add(member);
        }
        memberRepository.saveAll(members);
        System.out.println("✅ 회원 10명 생성 완료");

        // ===============================
        // 2️⃣ 경매 20~30개 생성
        // ===============================
        List<Auction> auctions = new ArrayList<>();
        String[] categories = {"ELECTRONICS", "BOOK", "GAME", "FURNITURE", "CLOTHES"};
        String[] items = {"아이폰", "닌텐도", "책", "의자", "셔츠", "노트북", "마우스"};

        for (int i = 1; i <= 25; i++) { // 25개 생성
            Auction auction = Auction.builder()
                    .auctionType("BID")
                    .category(categories[random.nextInt(categories.length)])
                    .sellerId(members.get(random.nextInt(members.size())).getId())
                    .itemName(items[random.nextInt(items.length)] + " " + i)
                    .itemDesc("설명 " + i)
                    .startPrice(10000 + random.nextInt(990000)) // 10,000 ~ 1,000,000
                    .auctionStatus(AuctionStatus.values()[random.nextInt(AuctionStatus.values().length)])
                    .startTime(LocalDateTime.now().minusHours(random.nextInt(48))) // 0~48시간 전
                    .endTime(LocalDateTime.now().plusHours(random.nextInt(72))) // 0~72시간 후
                    .build();

            auctions.add(auction);
        }
        auctionRepository.saveAll(auctions);

        System.out.println("✅ 경매 25개 랜덤 생성 완료");
        System.out.println("✅ 테스트 데이터 입력 완료!");
    }
}
