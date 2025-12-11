package com.example.zubzub.security.handler;

import com.example.zubzub.service.AuctionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CustomLogoutHandler implements LogoutHandler {

    private final AuctionService auctionService;

    public CustomLogoutHandler(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @Override
    public void logout(HttpServletRequest request,
                       HttpServletResponse response,
                       Authentication authentication) {
        // 로그아웃 시 cleanup 실행
        auctionService.cleanUp(authentication);
    }
}
