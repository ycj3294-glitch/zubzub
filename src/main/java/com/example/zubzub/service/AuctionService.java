package com.example.zubzub.service;

import com.example.zubzub.dto.AuctionCreateDto;
import com.example.zubzub.entity.Auction;
import com.example.zubzub.mapper.AuctionMapper;
import com.example.zubzub.repository.AuctionRepository;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final AuctionSchedulerService auctionSchedulerService;
    private final ConcurrentHashMap<Long, Auction> cache = new ConcurrentHashMap<>();

    // CREATE
    public Boolean createAuction(AuctionCreateDto dto) {
        Auction auction = AuctionMapper.convertAuctionDtoToEntity(dto);
        auction.setItemStatus("경매대기");
        Auction savedAuction = auctionRepository.save(auction);
        try {
            auctionSchedulerService.scheduleAuctionStart(savedAuction);
            auctionSchedulerService.scheduleAuctionEnd(savedAuction);
        } catch (SchedulerException e) {
            log.error("타이머 지정 실패 : {}", e.getMessage());
            auctionRepository.deleteById(savedAuction.getId());
            return false;
        }
        return true;
    }

    // READ (전체 조회)
    public List<Auction> getAllAuctions() {
        return auctionRepository.findAll();
    }

    // READ (단건 조회)
    public Auction getAuctionById(Long id) {
        Auction auction = cache.get(id);
        if (auction == null) {
            try {
                auction = auctionRepository.findById(id).orElseThrow(() -> new RuntimeException("Auction not found"));
            } catch (RuntimeException e){
                log.info("auction 조회 오류 : {}", e.getMessage());
                return null;
            }
            cache.put(id, auction);
        }
        return auction;
    }

    // cache UPDATE
    public Boolean updateAuction(Long id, Auction auction) {
        cache.put(id, auction);
        return true;
    }

    // DB UPDATE
    public Auction endAuction(Long id) {
        Auction auction = cache.get(id);
        return auctionRepository.save(auction);
    }

    // DELETE
    public void deleteAuction(Long id) {
        auctionRepository.deleteById(id);
    }
}