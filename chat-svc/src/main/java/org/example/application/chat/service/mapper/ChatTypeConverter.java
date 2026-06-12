package org.example.application.chat.service.mapper;

import org.example.application.chat.dto.ChatTypeDTO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ChatTypeConverter implements Converter<String, ChatTypeDTO> {
    @Override
    public ChatTypeDTO convert(String value) {
        return ChatTypeDTO.valueOf(value.toUpperCase(Locale.ROOT));
    }
}
