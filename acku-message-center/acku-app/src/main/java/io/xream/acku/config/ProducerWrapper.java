package io.xream.acku.config;

import io.xream.acku.produce.Producer;

/**
 * @author Sim
 */
public class ProducerWrapper implements Producer {

    private Producer producer;
    public void setProducer(Producer producer) {
        this.producer = producer;
    }
    @Override
    public boolean send(String topic, String message) {
        return producer.send(topic, message);
    }
}
