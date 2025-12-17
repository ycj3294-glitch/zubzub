package com.example.zubzub.service;

import com.example.zubzub.component.Broadcaster;
import com.example.zubzub.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {
    private final ConcurrentHashMap<Long, ConcurrentLinkedQueue<ChatMessage>> chatRooms = new ConcurrentHashMap<>();
    private final Broadcaster broadcaster;

    public List<ChatMessage> getMessages(Long roomId) {
        return new ArrayList<>(chatRooms.getOrDefault(roomId, new ConcurrentLinkedQueue<>()));
    }

    public void sendMessage(Long roomId, ChatMessage message) {
        chatRooms.computeIfAbsent(roomId, k-> new ConcurrentLinkedQueue<>()).add(message);
        broadcaster.broadcastChat(roomId, message);
    }
}
