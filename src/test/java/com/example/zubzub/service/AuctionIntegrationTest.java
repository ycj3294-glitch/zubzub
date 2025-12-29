package com.example.zubzub.service;

import com.example.zubzub.dto.BidHistoryCreateDto;
import com.example.zubzub.entity.Auction;
import com.example.zubzub.entity.AuctionStatus;
import com.example.zubzub.entity.AuctionType;
import com.example.zubzub.entity.Member;
import com.example.zubzub.repository.AuctionRepository;
import com.example.zubzub.repository.MemberRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
class AuctionIntegrationTest {

    @Autowired
    private AuctionBidService auctionBidService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AuctionRepository auctionRepository;

    @Test
    @DisplayName("메이저 경매 입찰 성공 - 최고가 갱신 및 크레딧 잠금 확인")
    void placeBid_success_integration() {
        // [given] 1. 판매자 생성 (Auction 생성을 위해 필수)
        Member seller = new Member();
        seller.setEmail("seller@test.com");
        seller.setPwd("1234");
        seller.setName("판매자");
        seller.setNickname("sellerNick");
        seller.setAddr("테스트 주소");
        memberRepository.save(seller);

        // [given] 2. 입찰자 생성
        Member bidder = new Member();
        bidder.setEmail("bidder@test.com");
        bidder.setPwd("1234");
        bidder.setName("이현수");
        bidder.setNickname("bidderNick");
        bidder.setAddr("테스트 주소2");
        bidder.setCredit(10000); // 넉넉하게 1만 포인트 부여
        memberRepository.save(bidder);

        // [given] 3. 경매 생성 (Auction 엔티티의 빌더 사용)
        Auction auction = Auction.builder()
                .seller(seller)
                .itemName("테스트 상품")
                .itemDesc("상품 설명입니다.")
                .category("가전")
                .auctionType(AuctionType.MAJOR)
                .auctionStatus(AuctionStatus.ACTIVE)
                .startPrice(1000)
                .minBidUnit(500)
                .bidCount(0)
                .startTime(LocalDateTime.now().minusHours(1))
                .endTime(LocalDateTime.now().plusHours(1))
                .finalPrice(5000) // 현재 최고가 5,000원
                .build();
        auctionRepository.save(auction);

        // [when] 입찰자가 7,000원으로 입찰 시도
        BidHistoryCreateDto dto = new BidHistoryCreateDto();
        dto.setBidderId(bidder.getId());
        dto.setPrice(7000);

        boolean result = auctionBidService.placeBid(auction.getId(), dto);

        // [then]
        assertTrue(result, "입찰 결과는 true여야 합니다.");

        // 데이터 재조회 후 검증
        Auction updatedAuction = auctionRepository.findById(auction.getId()).orElseThrow();
        Member updatedBidder = memberRepository.findById(bidder.getId()).orElseThrow();

        assertEquals(7000, updatedAuction.getFinalPrice(), "최고가가 7000원으로 업데이트되어야 합니다.");
        assertEquals(bidder.getId(), updatedAuction.getWinner().getId(), "낙찰자가 입찰자로 변경되어야 합니다.");
        assertEquals(7000, updatedBidder.getLockedCredit(), "입찰자의 잠금 크레딧이 7000원이어야 합니다.");
    }
}