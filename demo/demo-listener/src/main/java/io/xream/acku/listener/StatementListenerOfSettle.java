package io.xream.acku.listener;


import io.xream.acku.api.AckuOnConsumed;
import io.xream.acku.api.acku.DtoConverter;
import io.xream.acku.bean.CatStatement;
import io.xream.acku.bean.dto.AckuDto;
import io.xream.acku.controller.StatementController;
import io.xream.x7.base.GenericObject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;



/**
 * @author Sim
 */
@Configuration
public class StatementListenerOfSettle {

    private static final Logger logger = LoggerFactory.getLogger(StatementListenerOfSettle.class);

    @Autowired
    private StatementController statementController;
    @Autowired
    private DtoConverter dtoConverter;

    @AckuOnConsumed(svc = "cat-statement")
    @KafkaListener(topics = "CAT_SETTLE_CREATED")
    public void onSettleCreated(ConsumerRecord<String, String> record) {

        AckuDto dto = dtoConverter.convertOnConsumed(record);

        GenericObject<CatStatement> go =  dto.getMessage().getBody();
        CatStatement catStatement = go.get();

        this.statementController.create(catStatement);

        logger.info("------------------------------------>>>>>>>>>>>>>>>>>>>>>>");
    }

}
