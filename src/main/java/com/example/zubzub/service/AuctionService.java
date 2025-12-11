package com.example.zubzub.service;

import com.example.zubzub.dto.BidHistoryCreateDto;
import com.example.zubzub.dto.CurrentBidResponseDto;
import com.example.zubzub.entity.BidHistory;
import com.example.zubzub.repository.BidHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionService {

    private final BidHistoryRepository bidHistoryRepository;
    private final ConcurrentHashMap<Long, BidHistory> cache = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;


    public CurrentBidResponseDto getCurrentBid(Long itemId) {
        BidHistory bidHistory = cache.get(itemId);
        if (bidHistory == null) {
            throw new NoSuchElementException("No bid found for item " + itemId);
        }
        return new CurrentBidResponseDto(
                bidHistory.getBidPrice(),
                bidHistory.getBidTime()
        );
    }


    public void placeBid(Long itemId, BidHistoryCreateDto dto) {
        BidHistory bidHistory = convertDtoToEntity(dto);
        bidHistory.setBidTime(LocalDateTime.now());
        bidHistory.setItemId(itemId);

        // 1. 상태 업데이트
        cache.put(itemId, bidHistory);
        log.info("Updated cache for item {} with bid {}", itemId, bidHistory);

        // 2. 브로드캐스트
        messagingTemplate.convertAndSend("/topic/auction." + itemId, bidHistory);
        log.info("Broadcasted bid for item {}", itemId);

        // 3. 비동기 DB 저장
        saveBidHistoryAsync(bidHistory);
    }

    @Async
    @Retryable(
            retryFor = { RuntimeException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public void saveBidHistoryAsync(BidHistory bidHistory) {

        log.info("Saving bid history: {}", bidHistory);
        bidHistoryRepository.save(bidHistory);
    }

    private BidHistory convertDtoToEntity(BidHistoryCreateDto dto) {
        return BidHistory.builder()
                .memberId(dto.getMemberId())
                .bidPrice(dto.getBidPrice())
                .build();
    }
}