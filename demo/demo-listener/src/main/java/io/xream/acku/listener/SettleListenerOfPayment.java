package io.xream.acku.listener;

import io.xream.acku.api.AckuOnConsumed;
import io.xream.acku.api.acku.DtoConverter;
import io.xream.acku.bean.CatSettle;
import io.xream.acku.bean.CatStatement;
import io.xream.acku.bean.dto.AckuDto;
import io.xream.acku.controller.SettleController;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;


/**
 * @author Sim
 */
@Configuration
public class SettleListenerOfPayment {

    @Autowired
    private SettleController settleController;
    @Autowired
    private DtoConverter dtoConverter;

    @AckuOnConsumed(svc = "cat-settle", nextTopic = "CAT_SETTLE_CREATED", nextRetryMax = 2, nextSvcs = {"cat-statement"})
    @KafkaListener(topics = "CAT_PAID")
    public CatStatement onCatPaid(ConsumerRecord<String, String> record) {

        AckuDto dto = dtoConverter.convertOnConsumed(record);
        //--------------

        CatSettle catSettle = new CatSettle();
        catSettle.setId("CAT_SETTLE_TEST");
        this.settleController.create(catSettle);

        CatStatement catStatement = new CatStatement();
        catStatement.setId("TEST_STATEMENT");
        catStatement.setTest("CAT_SETTLE_CREATED");

        return catStatement;
    }

    @AckuOnConsumed(svc = "cat-settle",nextTopic = "CAT_SETTLE_CREATED", nextRetryMax = 2, nextSvcs = {"cat-statement"})
    @KafkaListener(topics = "CAT_PAID_TCC_TRY")
    public CatStatement onCatPaid_TCC_TRY(ConsumerRecord<String, String> record) {

        AckuDto dto = dtoConverter.convertOnConsumed(record);
        //--------------

        CatSettle catSettle = new CatSettle();
        catSettle.setId("CAT_SETTLE_TEST");
        catSettle.setName("TRY");

        settleController.create(catSettle);

        CatStatement catStatement = new CatStatement();
        catStatement.setId("TEST_STATEMENT");
        catStatement.setTest("CAT_SETTLE_CREATED");

        return catStatement;

    }

    @AckuOnConsumed(svc = "cat-settle")
    @KafkaListener(topics = "CAT_PAID_TCC_CONFIRM")
    public void onCatPaid_TCC_CONFIRM(ConsumerRecord<String, String> record) {

        AckuDto dto = dtoConverter.convertOnConsumed(record);
        //--------------

        CatSettle catsettle = new CatSettle();
        catsettle.setId("CAT_SETTLE_TEST");
        catsettle.setName("CONFIRM");

        settleController.confirm(catsettle);

    }

    @AckuOnConsumed(svc = "cat-settle")
    @KafkaListener(topics = "CAT_PAID_TCC_CANCEL")
    public void onCatPaid_TCC_CANCEL(ConsumerRecord<String, String> record) {

        AckuDto dto = dtoConverter.convertOnConsumed(record);
        //--------------

        CatSettle catsettle = new CatSettle();
        catsettle.setId("CAT_SETTLE_TEST");
        catsettle.setName("CANCEL");

        settleController.cancel(catsettle);

    }


}
