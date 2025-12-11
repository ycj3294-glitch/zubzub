package com.example.zubzub.listener;

import com.example.zubzub.service.AuctionService;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomHttpSessionListener implements HttpSessionListener {

    private final AuctionService auctionService;

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        System.out.println("세션 만료 발생! (비로그인 포함)");
    }
}