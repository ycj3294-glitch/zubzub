package com.example.zubzub.component;

import com.example.zubzub.entity.Auction;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

// 브로드캐스터 컴포넌트 (클래스 대신 컴포넌트로 랩핑)
@Component
@RequiredArgsConstructor
public class AuctionBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcast(Auction auction) {
        messagingTemplate.convertAndSend("/topic/auction." + auction.getId(), auction);
    }
}
