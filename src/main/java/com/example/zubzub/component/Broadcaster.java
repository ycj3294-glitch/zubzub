package com.example.zubzub.component;

import com.example.zubzub.dto.AuctionResDto;
import com.example.zubzub.dto.ChatMessage;
import com.example.zubzub.entity.Auction;
import com.example.zubzub.mapper.AuctionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

// 브로드캐스터 컴포넌트 (클래스 대신 컴포넌트로 랩핑)
@Slf4j
@Component
@RequiredArgsConstructor
public class Broadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    // 경매 알림
    public void broadcastAuction(Auction auction) {
        AuctionResDto dto = AuctionMapper.convertEntityToAuctionDto(auction);
        log.info("경매 알림 : {}", dto);
        messagingTemplate.convertAndSend("/topic/auction." + auction.getId(), dto);
    }

    // 채팅 알림
    public void broadcastChat(Long id, ChatMessage chatMessage) {
        log.info("채팅 알림 : {}", chatMessage);
        messagingTemplate.convertAndSend("/topic/chat." + id, chatMessage);
    }


}
