package io.xream.acku.controller;

import io.xream.acku.api.acku.FailedService;
import io.xream.acku.bean.constant.MessageStatus;
import io.xream.sqli.builder.Criteria;
import io.xream.sqli.builder.CriteriaBuilder;
import io.xream.sqli.builder.Direction;
import io.xream.sqli.builder.RefreshBuilder;
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

        CriteriaBuilder.ResultMapBuilder builder = CriteriaBuilder.resultMapBuilder();
        builder.resultKey("id").resultKey("status").resultKey("retryMax").resultKey("topic");
        builder.and().eq("status", MessageStatus.FAIL);
        builder.and().gt("retryMax", 0);
        builder.sort("topic", Direction.DESC);

        Criteria.ResultMapCriteria criteria = builder.build();

        List<Map<String,Object>> mapList = this.failedService.listByResultMap(criteria);

        return mapList;
    }

    @RequestMapping(value = "/find/{topic}", method = RequestMethod.GET)
    public List<Map<String,Object>> findFailedByTopic(@PathVariable String topic) {

        CriteriaBuilder.ResultMapBuilder builder = CriteriaBuilder.resultMapBuilder();
        builder.resultKey("id").resultKey("status").resultKey("retryMax").resultKey("topic");
        builder.and().eq("status", MessageStatus.FAIL);
        builder.and().eq("topic",topic);
        builder.and().gt("retryMax", 0);

        Criteria.ResultMapCriteria criteria = builder.build();

        List<Map<String,Object>> mapList = this.failedService.listByResultMap(criteria);

        return mapList;
    }

    @RequestMapping(value = "/retryAll", method = RequestMethod.GET)
    public boolean retryAll(){

        return this.failedService.refreshUnSafe(
                RefreshBuilder.builder().refresh("status",MessageStatus.SEND).refresh("retryCount",0)
                        .eq("status",MessageStatus.FAIL).gt("retryMax",0).build()
        );
    }

    @RequestMapping(value = "/retry/{messageId}", method = RequestMethod.GET)
    public boolean retry(@PathVariable String messageId){

        return this.failedService.refresh(
                RefreshBuilder.builder().refresh("status",MessageStatus.SEND)
                        .refresh("retryCount",0)
                        .eq("id",messageId).build()
        );
    }

}
