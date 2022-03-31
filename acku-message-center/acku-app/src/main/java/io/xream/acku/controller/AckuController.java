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

import io.xream.internal.util.JsonX;
import io.xream.internal.util.StringUtil;
import io.xream.acku.TCCTopic;
import io.xream.acku.api.acku.MessageResultService;
import io.xream.acku.api.acku.AckuMessageService;
import io.xream.acku.bean.constant.MessageStatus;
import io.xream.acku.bean.dto.ConsumedAckuDto;
import io.xream.acku.bean.dto.AckuDto;
import io.xream.acku.bean.entity.MessageResult;
import io.xream.acku.bean.entity.AckuMessage;
import io.xream.acku.bean.exception.AckuExceptioin;
import io.xream.acku.produce.Producer;
import io.xream.sqli.builder.RefreshBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Transactional
@RestController
@RequestMapping("/message")

/**
 * @author Sim
 */
public class AckuController {


    @Autowired
    private AckuMessageService AckuMessageService;
    @Autowired
    private MessageResultService messageResultService;

    @Autowired
    private Producer producer;

    @Resource(name = "nextProducer")
    private Producer nextProducer;

    @Autowired
    private io.xream.acku.controller.TccBusiness tccBusiness;

    @Autowired
    private io.xream.acku.controller.NextBusiness nextBusiness;


    @RequestMapping("/create")
    public AckuDto create(@RequestBody AckuDto dto) {

        AckuMessage AckuMessage = dto.getMessage();
        if (AckuMessage == null)
            throw new AckuExceptioin("AckuMessage == null");

        Date date = new Date();

        String messageId = AckuMessage.getId();
        if (messageId == null) {
            messageId = UUID.randomUUID().toString();
            messageId = messageId.replace("-", "");
        }

        AckuMessage.setId(messageId);
        AckuMessage.setCreateAt(date);
        AckuMessage.setSendAt(date.getTime());
        AckuMessage.setSvcDone(io.xream.acku.controller.TccBusiness.SVC_DONE_PREFIX);

        if (StringUtil.isNullOrEmpty(AckuMessage.getTracingId())) {
            AckuMessage.setTracingId(messageId);
        }

        if (StringUtil.isNullOrEmpty(AckuMessage.getStatus())){
            AckuMessage.setStatus(MessageStatus.BLANK.toString());
        }

        List<MessageResult> resultList = dto.getResultList();

        this.AckuMessageService.create(AckuMessage);

        for (MessageResult result : resultList) {

            result.setId(result.getSvc() + "_" + messageId);
            result.setMsgId(messageId);
            result.setStatus(MessageStatus.BLANK.toString());
            result.setCreateAt(date);

            this.messageResultService.create(result);
        }

        return dto;
    }


    @RequestMapping("/produce")
    public boolean produce(@RequestBody  AckuDto dto) {

        AckuMessage AckuMessage = dto.getMessage();
        if (AckuMessage == null)
            throw new AckuExceptioin("AckuMessage == null");

        Date date = new Date();
        AckuMessage.setRefreshAt(date);
        AckuMessage.setStatus(MessageStatus.SEND.toString());

        boolean flag = this.AckuMessageService.refresh(
                RefreshBuilder.builder()
                        .refresh("status",AckuMessage.getStatus())
                        .refresh("sendAt", AckuMessage.getSendAt())
                        .refresh("refreshAt", AckuMessage.getRefreshAt())
                        .eq("id", AckuMessage.getId()).build()
        );

        if (!flag)
            throw new AckuExceptioin("AckuMessage refresh persist failed");

        /*
         * MQ
         */
        String topic = AckuMessage.getTopic();
        return producer.send(topic, JsonX.toJson(dto));
    }


    @RequestMapping("/consume")
    public boolean consume(@RequestBody  ConsumedAckuDto dto) {

        String msgId = dto.getMsgId();
        String svc = dto.getSvc();
        if (StringUtil.isNullOrEmpty(msgId))
            throw new AckuExceptioin("ConsumedAckuDto lack of msgId: " + dto);

        if (StringUtil.isNullOrEmpty(svc))
            return true;

        Date date = new Date();

        String resultId = dto.getResultId();
        if (StringUtil.isNotNull(resultId)) {

            boolean flag = this.messageResultService.refresh(
                    RefreshBuilder.builder()
                            .refresh("status", dto.getTcc())
                            .refresh("refreshAt", date)
                            .eq("id", resultId).eq("status", MessageStatus.BLANK).build()
            );
            if (!flag)
                throw new AckuExceptioin("Problem with refresh resultMessage, id = " + resultId);
        }else {
            String id = svc + "_" + msgId;
            if (id.length() > 55) {
                id = id.substring(0,55);
            }
            MessageResult messageResult = new MessageResult();
            messageResult.setId(id);
            messageResult.setMsgId(msgId);
            messageResult.setStatus(dto.getTcc());
            messageResult.setSvc(svc);
            messageResult.setCreateAt(date);
            messageResult.setRefreshAt(date);
            try {
                boolean flag = this.messageResultService.create(messageResult);
                if (!flag)
                    throw new AckuExceptioin("Problem with create resultMessage, id = " + id);
            }catch (Exception e){
                throw new AckuExceptioin("Problem with create resultMessage, id = " + id);
            }
        }

        return this.AckuMessageService.refresh(
                RefreshBuilder.builder()
                        .refresh("svcDone = CONCAT(svcDone, ? , '" + io.xream.acku.controller.TccBusiness.SVC_DONE_PREFIX +"' )", svc)
                        .refresh("refreshAt", date)
                        .eq("id", msgId)
                        .eq("tcc", dto.getUseTcc() ? dto.getTcc() : null).build()
        );

    }


    @RequestMapping("/tryToConfirm")
    public boolean tryToConfirm(@RequestBody String msgId) {
        AckuMessage message = this.AckuMessageService.get(msgId);
        if (message == null)
            return true;

        for (Object svc : message.getSvcList()){
            if (!message.getSvcDone().contains(svc.toString()))
                return false;
        }

        if (message.getTcc().equals(TCCTopic._TCC_NONE.name()))
            return true;

        boolean flag = this.tccBusiness.confirm(message,AckuMessageService,producer);

        if (!flag)
            throw new RuntimeException("ERROR, at AckuProducer TCC confirm");

        try {
            this.nextBusiness.produce(message.getId(), AckuMessageService, nextProducer);
        }catch (Exception e) {
            // 需要任务补偿
        }

        return flag;
    }


    @RequestMapping("/cancel")
    public boolean cancel(@RequestBody String msgId) {

        AckuMessage message = this.AckuMessageService.get(msgId);
        if (message == null)
            return false;
        if (message.getTcc().equals(TCCTopic._TCC_NONE.name()))
            return false;

        return this.tccBusiness.cancel(message,AckuMessageService,producer);
    }


}
