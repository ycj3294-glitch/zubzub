package com.example.zubzub.component;

import com.example.zubzub.entity.Auction;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuctionBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcast(Auction auction) {
        messagingTemplate.convertAndSend("/topic/auction." + auction.getId(), auction);
    }
}
