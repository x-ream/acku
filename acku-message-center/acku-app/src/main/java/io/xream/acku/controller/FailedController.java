package io.xream.acku.controller;

import io.xream.acku.api.acku.FailedService;
import io.xream.acku.bean.constant.MessageStatus;
import io.xream.acku.bean.entity.AckuMessage;
import io.xream.sqli.builder.Direction;
import io.xream.sqli.builder.Q;
import io.xream.sqli.builder.QB;
import io.xream.sqli.builder.QrB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/failed")

/**
 * @author Sim
 */
public class FailedController {

    @Autowired
    private FailedService failedService;

    @RequestMapping(value = "/find", method = RequestMethod.GET)
    public List<Map<String,Object>> findFailed() {

        QB.X builder = QB.x();
        builder.select("id","status","retryMax","topic").from(AckuMessage.class);
        builder.eq("status", MessageStatus.FAIL).gt("retryMax", 0);
        builder.sort("topic", Direction.DESC);

        Q.X x = builder.build();

        List<Map<String,Object>> mapList = this.failedService.listByX(x);

        return mapList;
    }

    @RequestMapping(value = "/find/{topic}", method = RequestMethod.GET)
    public List<Map<String,Object>> findFailedByTopic(@PathVariable String topic) {

        QB.X builder = QB.x();
        builder.select("id","status","retryMax","topic").from(AckuMessage.class);
        builder.eq("status", MessageStatus.FAIL).eq("topic",topic).gt("retryMax", 0);

        Q.X x = builder.build();

        List<Map<String,Object>> mapList = this.failedService.listByX(x);

        return mapList;
    }

    @RequestMapping(value = "/retryAll", method = RequestMethod.GET)
    public boolean retryAll(){

        return this.failedService.refreshUnSafe(
                QrB.of(AckuMessage.class).refresh("status",MessageStatus.SEND)
                        .refresh("retryCount",0)
                        .eq("status",MessageStatus.FAIL).gt("retryMax",0).build()
        );
    }

    @RequestMapping(value = "/retry/{messageId}", method = RequestMethod.GET)
    public boolean retry(@PathVariable String messageId){

        return this.failedService.refresh(
                QrB.of(AckuMessage.class).refresh("status",MessageStatus.SEND)
                        .refresh("retryCount",0)
                        .eq("id",messageId).build()
        );
    }

}
