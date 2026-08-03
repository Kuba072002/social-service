package org.example.application.config;

import org.example.common.Utils;
import org.example.domain.message.MessageContent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.cassandra.core.convert.CassandraCustomConversions;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

import java.util.List;

@Configuration
class MessageContentCassandraConverter {

    @Bean
    CassandraCustomConversions cassandraCustomConversions() {
        return new CassandraCustomConversions(List.of(
                new MessageContentReadingConverter(),
                new MessageContentWritingConverter()
        ));
    }

    @ReadingConverter
    static class MessageContentReadingConverter implements Converter<String, MessageContent> {
        @Override
        public org.example.domain.message.MessageContent convert(String source) {
            return Utils.readJson(source, MessageContent.class);
        }
    }

    @WritingConverter
    static class MessageContentWritingConverter implements Converter<MessageContent, String> {
        @Override
        public String convert(MessageContent source) {
            return Utils.writeToJson(source);
        }
    }
}
