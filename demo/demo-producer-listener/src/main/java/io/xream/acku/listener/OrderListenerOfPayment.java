package io.xream.acku.listener;

import io.xream.internal.util.JsonX;
import io.xream.acku.api.AckuOnConsumed;
import io.xream.acku.api.acku.DtoConverter;
import io.xream.acku.bean.CatOrder;
import io.xream.acku.bean.dto.AckuDto;
import io.xream.acku.controller.OrderController;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;


/**
 * @author Sim
 */
@Configuration
public class OrderListenerOfPayment {

    @Autowired
    private OrderController orderController;

    @Autowired
    private DtoConverter dtoConverter;

    @AckuOnConsumed(svc = "cat-order")
    @KafkaListener(topics = "CAT_PAID")
    public void onCatPaid(ConsumerRecord<String, String> record) {

        String json = record.value();
        AckuDto dto = JsonX.toObject(json,AckuDto.class);
        //--------------

        CatOrder catOrder = new CatOrder();
        catOrder.setId("CAT_ORDER_TEST");

        orderController.create(catOrder);

    }

    @AckuOnConsumed(svc = "cat-order")
    @KafkaListener(topics = "CAT_PAID_TCC_TRY")
    public void onCatPaid_TCC_TRY(ConsumerRecord<String, String> record) {

        String json = record.value();
        AckuDto dto = JsonX.toObject(json,AckuDto.class);
        //--------------

        CatOrder catOrder = new CatOrder();
        catOrder.setId("CAT_ORDER_TEST");
        catOrder.setStatus("TRY");

        orderController.create(catOrder);

    }

    @AckuOnConsumed(svc = "cat-order")
    @KafkaListener(topics = "CAT_PAID_TCC_CONFIRM")
    public void onCatPaid_TCC_CONFIRM(ConsumerRecord<String, String> record) {

        String json = record.value();
        AckuDto dto = JsonX.toObject(json,AckuDto.class);
        //--------------

        CatOrder catOrder = new CatOrder();
        catOrder.setId("CAT_ORDER_TEST");
        catOrder.setStatus("CONFIRM");

        orderController.confirm(catOrder);

    }

    @AckuOnConsumed(svc = "cat-order")
    @KafkaListener(topics = "CAT_PAID_TCC_CANCEL")
    public void onCatPaid_TCC_CANCEL(ConsumerRecord<String, String> record) {

        String json = record.value();
        AckuDto dto = JsonX.toObject(json,AckuDto.class);
        //--------------

        CatOrder catOrder = new CatOrder();
        catOrder.setId("CAT_ORDER_TEST");
        catOrder.setStatus("CANCEL");

        orderController.cancel(catOrder);

    }
}
