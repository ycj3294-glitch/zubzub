package com.example.zubzub.service;

import com.example.zubzub.entity.Auction;
import com.example.zubzub.repository.AuctionRepository;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuctionService {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private class AuctionState {
        private Long auctionId;
        private Long currentBid;
        private LocalDateTime endDate;
    }

    private final AuctionRepository auctionRepository;
    private final ConcurrentHashMap<Long, AuctionState> cache = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    public void sampleTest(){

        // 샘플 경매 하나 생성
        AuctionState auction = new AuctionState(1L, 1000L, LocalDateTime.now().plusMinutes(1));
        cache.put(auction.getAuctionId(), auction);

        // 5초마다 캐시 변경 이벤트 발생
        scheduler.scheduleAtFixedRate(this::simulateBid, 5, 1, TimeUnit.SECONDS);
    }

    private void simulateBid() {
        log.info("아무거나");
        AuctionState auction = cache.get(1L);
        if (auction == null) return;

        // currentBid 증가
        long newBid = auction.getCurrentBid() + 100;
        auction.setCurrentBid(newBid);

        // endDate 5초 연장
        auction.setEndDate(LocalDateTime.now().plusSeconds(30));

        // 캐시 갱신
        cache.put(auction.getAuctionId(), auction);

        // WebSocket 브로드캐스트
        messagingTemplate.convertAndSend("/topic/auction." + auction.getAuctionId(), auction);
    }


    // 해당 회원의 판매 내역 리스트
    public List<Auction> sellList(Long id) {
        List<Auction> sellList = auctionRepository.findBySellerId(id);
        return sellList;
    }

    // 해당 회원의 낙찰 내역 리스트
    public List<Auction> winnerList(Long id) {
        List<Auction> winnerList = auctionRepository.findByWinnerId(id);
        return winnerList;
    }

}
