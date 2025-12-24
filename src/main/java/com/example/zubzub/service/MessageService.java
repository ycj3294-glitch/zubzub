package com.example.zubzub.service;

import com.example.zubzub.dto.MessageCreateDto;
import com.example.zubzub.dto.MessageResDto;
import com.example.zubzub.entity.Member;
import com.example.zubzub.entity.Message;
import com.example.zubzub.mapper.MessageMapper;
import com.example.zubzub.repository.MemberRepository;
import com.example.zubzub.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final MemberRepository memberRepository;

    public void create(MessageCreateDto dto) {
        Member receiver = memberRepository.findById(dto.getReceiverId())
                .orElseThrow(() -> new NoSuchElementException("받는 사람을 찾을 수 없습니다"));
        Message message = MessageMapper.convertMessageDtoToEntity(dto, receiver);
        message.setCreatedAt(LocalDateTime.now());
        messageRepository.save(message);
    }

    public Page<MessageResDto> getReceivedMessages(Long id, Pageable pageable) {
        Page<Message> messages = messageRepository.findByReceiverId(id, pageable);
        return messages.map(MessageMapper::convertEntityToMessageDto);
    }

    public MessageResDto getMessage(Long id, Long receiverId) {
        Message message =  messageRepository.findByIdAndReceiverId(id, receiverId)
                .orElseThrow(() -> new NoSuchElementException("메시지를 찾을 수 없습니다"));
        return MessageMapper.convertEntityToMessageDto(message);
    }

    public void readMessage(Long id, Long receiverId) {
        Message message =  messageRepository.findByIdAndReceiverId(id, receiverId)
                .orElseThrow(() -> new NoSuchElementException("메시지를 찾을 수 없습니다"));
        message.setRead(true);
        messageRepository.save(message);
    }

    public void deleteMessage(Long id, Long receiverId) {
        Message message = messageRepository.findByIdAndReceiverId(id, receiverId)
                .orElseThrow(() -> new NoSuchElementException("메시지를 찾을 수 없습니다"));
        message.setDeleted(true);
        messageRepository.save(message);

    }
}
