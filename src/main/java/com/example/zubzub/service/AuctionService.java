package com.example.zubzub.service;

import com.example.zubzub.dto.AuctionCreateDto;
import com.example.zubzub.dto.AuctionResDto;
import com.example.zubzub.entity.Auction;
import com.example.zubzub.entity.AuctionStatus;
import com.example.zubzub.mapper.AuctionMapper;
import com.example.zubzub.repository.AuctionRepository;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final AuctionSchedulerService auctionSchedulerService;
    // 실시간성을 위한 캐시 사용
    private final ConcurrentHashMap<Long, Auction> cache = new ConcurrentHashMap<>();

    // CREATE
    public Boolean createAuction(AuctionCreateDto dto) {
        Auction auction = AuctionMapper.convertAuctionDtoToEntity(dto);
        // 경매생성시 자동으로 경매대기 상태로 설정 (DB에서 넣어줘도 될 듯함)
        auction.setAuctionStatus(AuctionStatus.READY);
        // DB에 넣어서 ID 자동 채우기
        Auction savedAuction = auctionRepository.save(auction);
        try {
            // 시작 종료 타이머 걸기
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
    public List<AuctionResDto> getAllAuctions() {
        return auctionRepository.findAll().stream()
                .map(AuctionMapper::convertEntityToAuctionDto)
                        .collect(Collectors.toList());
    }

    // READ (단건 조회)
    public Auction getAuctionById(Long id) {
        // 캐시에서 먼저 찾고, 없으면 DB에서 조회 후 캐시에 넣어줌
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

    public AuctionResDto getAuctionDtoById(Long id) {
        Auction auction = getAuctionById(id);
        if (auction != null) return AuctionMapper.convertEntityToAuctionDto(auction);
        else return null;
    }

    // cache UPDATE
    public Boolean updateAuction(Auction auction) {
        cache.put(auction.getId(), auction);
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