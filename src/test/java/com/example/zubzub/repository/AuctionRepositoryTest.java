package com.example.zubzub.repository;

import com.example.zubzub.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class AuctionRepositoryTest {

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("상태별 경매 조회 테스트")
    void findByAuctionStatus_test() {
        // given
        Member seller = new Member();
        seller.setEmail("seller@test.com");
        seller.setNickname("seller");
        seller.setPwd("1234");
        memberRepository.save(seller);

        Auction auction = Auction.builder()
                .seller(seller)
                .itemName("상품")
                .itemDesc("설명")
                .category("카테고리")
                .auctionType(AuctionType.MINOR)
                .auctionStatus(AuctionStatus.PENDING) // 승인 대기 상태로 저장
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusDays(1))
                .build();
        auctionRepository.save(auction);

        // when
        List<Auction> pendingAuctions = auctionRepository.findByAuctionStatus(AuctionStatus.PENDING);

        // then
        assertFalse(pendingAuctions.isEmpty());
        assertEquals(AuctionStatus.PENDING, pendingAuctions.get(0).getAuctionStatus());
    }
}
