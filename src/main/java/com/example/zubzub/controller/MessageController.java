package com.example.zubzub.controller;

import com.example.zubzub.dto.MessageCreateDto;
import com.example.zubzub.dto.MessageResDto;
import com.example.zubzub.entity.Member;
import com.example.zubzub.security.CustomUserDetails;
import com.example.zubzub.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.InputMismatchException;
import java.util.NoSuchElementException;

@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    // 쪽지 생성
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Void> create(
            @RequestBody MessageCreateDto dto
    ) {
        try{
            messageService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (InputMismatchException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // 받은 쪽지함 조회
    @GetMapping("/received")
    public ResponseEntity<Page<MessageResDto>> received(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            Pageable pageable
    ) {
        return ResponseEntity.ok(messageService.getReceivedMessages(customUserDetails.getId(), pageable));
    }

    // 특정 메시지 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<MessageResDto> getMessage(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        try {
            MessageResDto dto = messageService.getMessage(id, customUserDetails.getId());
            return ResponseEntity.ok(dto);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

    }

    // 메시지 읽음 처리
    @PostMapping("/{id}/read")
    public ResponseEntity<Void> read(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        try {
            messageService.readMessage(id, customUserDetails.getId());
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // 메시지 삭제
    @PostMapping("/{id}/delete")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        messageService.deleteMessage(id, customUserDetails.getId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}