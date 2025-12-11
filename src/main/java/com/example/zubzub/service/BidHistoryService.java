package com.example.zubzub.service;

import com.example.zubzub.dto.BidHistoryCreateDto;
import com.example.zubzub.entity.BidHistory;
import com.example.zubzub.repository.BidHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BidHistoryService {

    private final BidHistoryRepository bidHistoryRepository;

    // Read (단건 조회)
    public BidHistory findById(Long id) {
        return bidHistoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bid not found"));
    }
}