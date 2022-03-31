package io.xream.acku.config;

import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;


/**
 * @author Sim
 */
@EnableConfigurationProperties
@Configuration
@ConfigurationProperties(prefix = "next.kafka")
public class NextKafkaProperties {

    private final KafkaProperties.Producer producer = new KafkaProperties.Producer();

    public KafkaProperties.Producer getProducer() {
        return producer;
    }

}
