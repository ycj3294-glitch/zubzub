package com.example.zubzub.service;

import com.example.zubzub.entity.BidHistory;
import com.example.zubzub.repository.BidHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BidHistoryService {

    private final BidHistoryRepository bidHistoryRepository;

//    // Read (단건 조회)
//    public BidHistory findById(Long id) {
//        return bidHistoryRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Bid not found"));
//    }

    // 비동기 save 로직
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
}