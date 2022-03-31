package io.xream.acku.config;

import io.xream.acku.produce.Producer;
import org.springframework.beans.FatalBeanException;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * @author Sim
 */
public class MQConfig implements
        ApplicationListener<ApplicationStartedEvent> {
    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {

        ProducerWrapper wrapper = event.getApplicationContext().getBean(ProducerWrapper.class);
        ProducerCustomizer customizer = null;

        try {
            customizer = event.getApplicationContext().getBean(ProducerCustomizer.class);
        } catch (Exception e) {
        }


        Producer producer = null;
        if (customizer != null){
            producer = customizer.customize();
        }

        if (producer == null) {
            KafkaTemplate<String, String> kafkaTemplate = null;
            try {
                kafkaTemplate = event.getApplicationContext().getBean(KafkaTemplate.class);
            } catch (Exception e) {
                throw new FatalBeanException("can't find any producer, no config like Kafka");
            }

            producer = new DefaultKafkaProducer(kafkaTemplate);
        }

        wrapper.setProducer(producer);

    }
}
