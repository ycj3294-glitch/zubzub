package com.example.zubzub.controller;

import com.example.zubzub.dto.ChatMessage;
import com.example.zubzub.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // 채팅방 내역 보기
    @GetMapping("/{id}/messages")
    public ResponseEntity<List<ChatMessage>> getMessages(@PathVariable Long id) {
        return ResponseEntity.ok(chatService.getMessages(id));
    }

    // 채팅 추가하기
    @PostMapping("/{id}/messages")
    public ResponseEntity<Void> sendMessage(@PathVariable Long id, @RequestBody ChatMessage chatMessage) {
        chatService.sendMessage(id, chatMessage);
        return ResponseEntity.ok().build();
    }

}
