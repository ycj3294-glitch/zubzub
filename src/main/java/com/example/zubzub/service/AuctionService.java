package com.example.zubzub.service;

import com.example.zubzub.dto.BidHistoryCreateDto;
import com.example.zubzub.entity.Auction;
import com.example.zubzub.entity.BidHistory;
import com.example.zubzub.repository.AuctionRepository;
import com.example.zubzub.repository.BidHistoryRepository;
import jakarta.annotation.PostConstruct;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final BidHistoryRepository bidHistoryRepository;
    private final ConcurrentHashMap<Long, Auction> cache = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;

    @PostConstruct
    public void initCache() {
        auctionRepository.findAll().forEach(a -> cache.put(a.getId(), a));
        log.info("Auction cache initialized with {} items", cache.size());
    }

    public synchronized boolean placeBid(Long auctionId, BidHistoryCreateDto dto) {

        BidHistory bidHistory = convertDtoToEntity(dto);
        bidHistory.setBidTime(LocalDateTime.now());

        // 1. 상태 업데이트
        Auction auction = getAuctionById(auctionId);
        if (auction.getExtendedEndTime() == null) {
            auction.setExtendedEndTime(auction.getEndTime());
        }
        if (auction.getExtendedEndTime().isBefore(bidHistory.getBidTime()))
            return false;
        if (auction.getExtendedEndTime().minusMinutes(5).isBefore(bidHistory.getBidTime())) {
            auction.setExtendedEndTime(bidHistory.getBidTime().plusMinutes(5));
        }
        auction.setWinnerId(bidHistory.getMemberId());
        auction.setFinalPrice(bidHistory.getPrice());
        cache.put(auctionId, auction);
        log.info("Updated cache for auction {} with bid {}", auctionId, bidHistory);

        // 2. 브로드캐스트
        messagingTemplate.convertAndSend("/topic/auction." + auctionId, auction);
        log.info("Broadcasted auction {}", auctionId);

        // 3. 비동기 DB 저장
        saveBidHistoryAsync(bidHistory);

        return true;
    }

    // CREATE
    public Auction createAuction(Auction auction) {
        return auctionRepository.save(auction);
    }

    // READ (전체 조회)
    public List<Auction> getAllAuctions() {
        return auctionRepository.findAll();
    }

    // READ (단건 조회)
    public Auction getAuctionById(Long id) {
        Auction auction = cache.get(id);
        if (auction == null) {
            auction = auctionRepository.findById(id).orElseThrow(() -> new RuntimeException("Auction not found"));
            cache.put(id, auction);
        }
        return auction;
    }

    // UPDATE
    public Auction updateAuction(Long id, Auction updatedAuction) {
        return auctionRepository.findById(id)
                .map(auction -> {
                    auction.setCategory(updatedAuction.getCategory());
                    auction.setSellerId(updatedAuction.getSellerId());
                    auction.setItemName(updatedAuction.getItemName());
                    auction.setItemDesc(updatedAuction.getItemDesc());
                    auction.setStartPrice(updatedAuction.getStartPrice());
                    auction.setFinalPrice(updatedAuction.getFinalPrice());
                    auction.setItemImg(updatedAuction.getItemImg());
                    auction.setItemStatus(updatedAuction.getItemStatus());
                    auction.setStartTime(updatedAuction.getStartTime());
                    auction.setEndTime(updatedAuction.getEndTime());
                    auction.setExtendedEndTime(updatedAuction.getExtendedEndTime());
                    auction.setWinnerId(updatedAuction.getWinnerId());
                    return auctionRepository.save(auction);
                })
                .orElseThrow(() -> new RuntimeException("Auction not found with id " + id));
    }

    // DELETE
    public void deleteAuction(Long id) {
        auctionRepository.deleteById(id);
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
                .auctionId((dto.getAuctionId()))
                .memberId(dto.getMemberId())
                .price(dto.getPrice())
                .build();
    }
}