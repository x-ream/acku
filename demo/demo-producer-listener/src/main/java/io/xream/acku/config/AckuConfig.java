package io.xream.acku.config;

import io.xream.internal.util.JsonX;
import io.xream.acku.api.acku.DtoConverter;
import io.xream.acku.bean.dto.AckuDto;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration

/**
 * @author Sim
 */
public class AckuConfig {


    @Bean
    public DtoConverter dtoConverter() {

        return message -> {

            String body = ((ConsumerRecord<String, String>) message).value(); //KAFKA
            AckuDto dto = JsonX.toObject(body, AckuDto.class);

            return dto;
        };
    }
}
