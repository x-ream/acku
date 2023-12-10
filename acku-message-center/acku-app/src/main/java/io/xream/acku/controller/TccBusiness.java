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
import io.xream.acku.bean.constant.MessageStatus;
import io.xream.acku.bean.dto.AckuDto;
import io.xream.acku.bean.entity.AckuMessage;
import io.xream.acku.produce.Producer;
import io.xream.internal.util.JsonX;
import io.xream.sqli.builder.QrB;
import io.xream.x7.base.GenericObject;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component

/**
 * @author Sim
 */
public class TccBusiness {

    public final static String SVC_DONE_PREFIX = "&";


    public boolean confirm(AckuMessage AckuMessage, AckuMessageService ackuMessageService, Producer producer) {

        Date date = new Date();

        boolean flag = ackuMessageService.refresh(
                QrB.of(AckuMessage.class)
                        .refresh("status", MessageStatus.OK)
                        .refresh("refreshAt", date)
                        .refresh("tcc", TCCTopic._TCC_CONFIRM)
                        .eq("id", AckuMessage.getId())
                        .eq("tcc", TCCTopic._TCC_TRY).build()
        ); //STEP 1

        if (!flag)
            return flag;

        GenericObject body = AckuMessage.getBody();
        String topic = AckuMessage.getTopic();
        String tracingId = AckuMessage.getId();
        AckuMessage AckuMessageConfirm = new AckuMessage(tracingId,topic,body,AckuMessage.getSvcList());
        AckuMessageConfirm.setRetryMax(3 * 2);
        AckuMessageConfirm.resetTopic(TCCTopic._TCC_CONFIRM);

        String messageId = UUID.randomUUID().toString();
        messageId = messageId.replace("-","");

        AckuMessageConfirm.setId(messageId);
        AckuMessageConfirm.setCreateAt(date);
        AckuMessageConfirm.setRefreshAt(date);
        AckuMessageConfirm.setSendAt(date.getTime());
        AckuMessageConfirm.setSvcDone(SVC_DONE_PREFIX);
        AckuMessageConfirm.setStatus(MessageStatus.SEND.toString());//初始化为已发送
        AckuMessageConfirm.setTcc(TCCTopic._TCC_CONFIRM.name());

        boolean b = ackuMessageService.create(AckuMessageConfirm); //STEP 2
        if (b){
            AckuDto dto = new AckuDto(AckuMessageConfirm);
            b &= producer.send(AckuMessageConfirm.getTopic(), JsonX.toJson(dto)); //STEP 3
        }

        return b;
    }

    public boolean cancel(AckuMessage AckuMessage, AckuMessageService ackuMessageService, Producer producer) {

        Date date = new Date();

        boolean flag = ackuMessageService.refresh(
                QrB.of(AckuMessage.class)
                        .refresh("status", MessageStatus.FAIL)
                        .refresh("refreshAt", date)
                        .refresh("tcc", TCCTopic._TCC_CANCEL)
                        .eq("id", AckuMessage.getId())
                        .eq("svcDone",AckuMessage.getSvcDone())
                        .ne("tcc", TCCTopic._TCC_CANCEL).build()
        ); //STEP 1

        if (!flag)
            return flag;

        GenericObject body = AckuMessage.getBody();
        String topic = AckuMessage.getTopic();
        String tracingId = AckuMessage.getId();
        AckuMessage AckuMessageCancel = new AckuMessage(tracingId,topic,body,AckuMessage.getSvcList());
        AckuMessageCancel.setRetryMax(3 * 2);
        AckuMessageCancel.resetTopic(TCCTopic._TCC_CANCEL);

        String messageId = UUID.randomUUID().toString();
        messageId = messageId.replace("-","");

        AckuMessageCancel.setId(messageId);
        AckuMessageCancel.setCreateAt(date);
        AckuMessageCancel.setRefreshAt(date);
        AckuMessageCancel.setSendAt(date.getTime());
        AckuMessageCancel.setSvcDone(SVC_DONE_PREFIX);
        AckuMessageCancel.setStatus(MessageStatus.SEND.toString());//初始化为已发送
        AckuMessageCancel.setTcc(TCCTopic._TCC_CANCEL.name());

        boolean b = ackuMessageService.create(AckuMessageCancel); //STEP 2

        if (b){
            AckuDto dto = new AckuDto(AckuMessageCancel);
            b &= producer.send(AckuMessageCancel.getTopic(), JsonX.toJson(dto)); //STEP 3
        }

        return b;
    }
}
