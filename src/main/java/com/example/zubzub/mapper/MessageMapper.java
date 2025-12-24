package com.example.zubzub.mapper;

import com.example.zubzub.dto.MessageCreateDto;
import com.example.zubzub.dto.MessageResDto;
import com.example.zubzub.entity.Member;
import com.example.zubzub.entity.Message;

public class MessageMapper {

    public static Message convertMessageDtoToEntity(MessageCreateDto dto, Member receiver) {
        return Message.builder()
                .receiver(receiver)
                .title(dto.getTitle())
                .content(dto.getContent())
                .build();
    }

    public static MessageResDto convertEntityToMessageDto(Message message) {
        return MessageResDto.builder()
                .id(message.getId())
                .title(message.getTitle())
                .content(message.getContent())
                .isRead(message.isRead())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
