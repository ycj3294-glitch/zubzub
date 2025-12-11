package com.example.zubzub.security.listener;

import com.example.zubzub.service.AuctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.security.core.session.SessionDestroyedEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SessionExpiredListener implements ApplicationListener<SessionDestroyedEvent> {

    private final AuctionService auctionService;

    @Override
    public void onApplicationEvent(SessionDestroyedEvent event) {
        // 세션 만료 시 실행할 함수
        System.out.println("세션 만료 발생!");
        auctionService.cleanUp(event.getSecurityContexts().get(0).getAuthentication());
    }
}