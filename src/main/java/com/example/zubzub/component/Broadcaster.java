package com.example.zubzub.component;

import com.example.zubzub.dto.ChatMessage;
import com.example.zubzub.entity.Auction;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentLinkedQueue;

// 브로드캐스터 컴포넌트 (클래스 대신 컴포넌트로 랩핑)
@Component
@RequiredArgsConstructor
public class Broadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    // 경매 알림
    public void broadcastAuction(Auction auction) {
        messagingTemplate.convertAndSend("/topic/auction." + auction.getId(), auction);
    }

    // 채팅 알림
    public void broadcastChat(Long id, ChatMessage chatMessage) {
        messagingTemplate.convertAndSend("/topic/chat." + id, chatMessage);
    }


}
