/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.xream.acku.controller;

import io.xream.acku.TCCTopic;
import io.xream.acku.api.acku.AckuMessageService;
import io.xream.acku.api.acku.MessageResultService;
import io.xream.acku.bean.constant.MessageStatus;
import io.xream.acku.bean.dto.AckuDto;
import io.xream.acku.bean.entity.AckuMessage;
import io.xream.acku.bean.entity.MessageResult;
import io.xream.acku.produce.Producer;
import io.xream.internal.util.JsonX;
import io.xream.internal.util.StringUtil;
import io.xream.sqli.builder.Q;
import io.xream.sqli.builder.QB;
import io.xream.sqli.builder.QrB;
import io.xream.x7.base.GenericObject;
import jakarta.annotation.Resource;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/schedule")

/**
 * @author Sim
 */
public class ScheduleAckuController {

    @Autowired
    private AckuMessageService AckuMessageService;
    @Autowired
    private MessageResultService messageResultService;

    @Autowired
    private Producer producer;

    @Resource(name = "nextProducer")
    private Producer nextProducer;

    @Autowired
    private TccBusiness tccBusiness;

    @Autowired
    private NextBusiness nextBusiness;


    @Value("${acku.retry.duration:5000}")
    private long ackuRetryDuration;

    private long checkStatusDuration = 1400;


    @RequestMapping(value = "/tryToProduceNext",method = RequestMethod.GET)
    public boolean tryToProduceNext(){

        QB builder = QB.of(AckuMessage.class);
        builder.eq("status",MessageStatus.NEXT);

        Q q = builder.build();

        List<AckuMessage> list = this.AckuMessageService.listByCond(q);

        Map<String,List<AckuMessage>> map = new HashMap<>();
        for (AckuMessage AckuMessage : list) {
            String parentId = AckuMessage.getParentId();
            List<AckuMessage> valueList = map.get(parentId);
            if (valueList == null) {
                valueList = new ArrayList<>();
                map.put(parentId,valueList);
            }
            valueList.add(AckuMessage);
        }

        for (String parentId : map.keySet()){
            AckuMessage AckuMessage = this.AckuMessageService.get(parentId);
            if (AckuMessage.getStatus().equals(MessageStatus.OK.name())){
                this.nextBusiness.produce(parentId,AckuMessageService,nextProducer);
            }
        }

        return true;
    }

    @RequestMapping(value = "/tryToFinish", method = RequestMethod.GET)
    public boolean tryToFinish() {

        Date createAt = new Date(System.currentTimeMillis() - checkStatusDuration);

        QB.X builder = QB.x();
        builder.select("id","svcDone","svcList","retryCount","retryMax","tcc","body");
        builder.from(AckuMessage.class);
        builder.eq("status", MessageStatus.SEND);
        builder.lt("createAt", createAt);

        Q.X x = builder.build();

        List<Map<String, Object>> list = this.AckuMessageService.listByX(x);

        if (list.isEmpty())
            return true;

        Date date = new Date();

        for (Map<String, Object> map : list) {

            List<String> svcList = (List<String>)MapUtils.getObject(map, "svcList");
            String tcc = MapUtils.getString(map, "tcc");
            Object bodyObj = MapUtils.getObject(map, "body");
            GenericObject go = (GenericObject) bodyObj;

            AckuMessage AckuMessage = new AckuMessage();
            AckuMessage.setId(MapUtils.getString(map, "id"));
            AckuMessage.setSvcDone(MapUtils.getString(map, "svcDone"));
            AckuMessage.setSvcList(svcList);
            AckuMessage.setBody(go);
            AckuMessage.setTcc(tcc);
            AckuMessage.setRetryCount(MapUtils.getLongValue(map, "retryCount"));
            AckuMessage.setRetryMax(MapUtils.getIntValue(map, "retryMax"));

            String svcDone = AckuMessage.getSvcDone();
            if (StringUtil.isNullOrEmpty(svcDone))
                continue;

            boolean flag = true;

            for (String svc : svcList) {
                flag &= svcDone.contains(svc);
            }


            if (AckuMessage.getTcc().equals(TCCTopic._TCC_TRY.name())) {
                if (!flag) {
                    if (AckuMessage.getRetryCount() >= AckuMessage.getRetryMax()) {
                        cancel(AckuMessage.getId());
                    }
                }
            } else {
                if (flag) {
                    this.AckuMessageService.refresh(
                            QrB.of(AckuMessage.class)
                                    .refresh("status", MessageStatus.OK)
                                    .refresh("refreshAt", date)
                                    .eq("id", AckuMessage.getId()).build()
                    );

                    try {
                        this.nextBusiness.produce(AckuMessage.getId(),AckuMessageService,nextProducer);
                    }catch (Exception e) {

                    }
                }
            }

        }

        return true;
    }


    @RequestMapping(value = "/listForRetry", method = RequestMethod.GET)
    public List<AckuMessage> listForRetry() {

        long rrd = ackuRetryDuration < 5000 ? 5000 : ackuRetryDuration;

        long now = System.currentTimeMillis();
        final long sendAt = now - rrd;

        QB.X builder = QB.x();
        builder.select("id","svcList","svcDone","retryCount","retryMax","tcc","topic","body");
        builder.from(AckuMessage.class);
        builder.eq("status", MessageStatus.SEND);
//        builder.x("retryCount < retryMax"); //需要人工补单
        builder.lt("sendAt", sendAt);

        List<Map<String, Object>> list = this.AckuMessageService.listByX(builder.build());

        List<AckuMessage> rmList = new ArrayList<>();

        if (list.isEmpty())
            return rmList;

        for (Map<String, Object> map : list) {

            Object bodyObj = MapUtils.getObject(map, "body");
            GenericObject go = (GenericObject) bodyObj;

            List<String> svcList = (List<String>) MapUtils.getObject(map, "svcList");

            AckuMessage AckuMessage = new AckuMessage();
            AckuMessage.setId(MapUtils.getString(map, "id"));
            AckuMessage.setTopic(MapUtils.getString(map, "topic"));
            AckuMessage.setSvcList(svcList);
            AckuMessage.setSvcDone(MapUtils.getString(map, "svcDone"));
            AckuMessage.setRetryCount(MapUtils.getLongValue(map, "retryCount"));
            AckuMessage.setRetryMax(MapUtils.getIntValue(map, "retryMax"));
            AckuMessage.setBody(go);
            AckuMessage.setTcc(MapUtils.getString(map, "tcc"));
            AckuMessage.setSendAt(sendAt);

            rmList.add(AckuMessage);
        }

        return rmList;
    }

    @Transactional
    @RequestMapping(value = "/retry")
    public boolean retry(@RequestBody AckuMessage AckuMessage) {

        Date date = new Date();

        if (AckuMessage.getRetryCount() < AckuMessage.getRetryMax()) {

            QB builder = QB.of(MessageResult.class);
            builder.eq("msgId", AckuMessage.getId());

            Q q = builder.build();

            List<MessageResult> list = this.messageResultService.listByCond(q);

            AckuDto dto = new AckuDto();
            for (MessageResult messageResult : list) {
                if (MessageStatus.BLANK.toString().equals(messageResult.getStatus())) {
                    dto.getResultList().add(messageResult);
                }
            }

            dto.setMessage(AckuMessage);

            AckuMessage.setRetryCount(AckuMessage.getRetryCount() + 1);
            AckuMessage.setSendAt(date.getTime());// IMPORTANT
            AckuMessage.setRefreshAt(date);

            this.AckuMessageService.refresh(
                    QrB.of(AckuMessage.class)
                            .refresh("retryCount", AckuMessage.getRetryCount() + 1)
                            .refresh("sendAt", AckuMessage.getSendAt())
                            .refresh("refreshAt", AckuMessage.getRefreshAt())
                            .eq("id", AckuMessage.getId()).build()
            );

            /*
             * MQ
             */
            String topic = AckuMessage.getTopic();
            producer.send(topic, JsonX.toJson(dto));

        } else {

            if (AckuMessage.getTcc().equals(TCCTopic._TCC_TRY.name())) {
                // TODO: confirm step, exception no rollback
                cancel(AckuMessage.getId());
            } else {
                //进入人工补单审核流程

                this.AckuMessageService.refresh(
                        QrB.of(AckuMessage.class)
                                .refresh("status", MessageStatus.FAIL)
                                .refresh("refreshAt", date)
                                .eq("id", AckuMessage.getId()).build()
                );
            }

        }

        return true;
    }

    @RequestMapping(value = "/clean", method = RequestMethod.GET)
    public boolean clean() {

        List<String> cleanStatusList = new ArrayList<>();
        cleanStatusList.add(MessageStatus.OK.toString());
        cleanStatusList.add(MessageStatus.BLANK.toString());

        QB.X builder = QB.x();
        builder.select("id").from(AckuMessage.class);
        builder.eq("underConstruction", false);
        builder.in("status", cleanStatusList);


        List<Map<String, Object>> list = this.AckuMessageService.listByX(builder.build());


        for (Map<String, Object> map : list) {
            String id = MapUtils.getString(map, "id");
            if (StringUtil.isNullOrEmpty(id))
                continue;

            this.messageResultService.removeByMessageId(id);
            this.AckuMessageService.remove(id);
        }

        return true;
    }

    @Transactional
    public boolean cancel(String id) {
        AckuMessage AckuMessage = this.AckuMessageService.get(id);
        return this.tccBusiness.cancel(AckuMessage, AckuMessageService, producer);
    }

}
