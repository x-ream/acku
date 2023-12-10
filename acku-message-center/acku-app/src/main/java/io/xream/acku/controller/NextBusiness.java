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

import io.xream.acku.api.acku.AckuMessageService;
import io.xream.acku.bean.constant.MessageStatus;
import io.xream.acku.bean.dto.AckuDto;
import io.xream.acku.bean.entity.AckuMessage;
import io.xream.acku.bean.exception.AckuExceptioin;
import io.xream.acku.produce.Producer;
import io.xream.internal.util.JsonX;
import io.xream.sqli.builder.Q;
import io.xream.sqli.builder.QB;
import io.xream.sqli.builder.QrB;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Component

/**
 * @author Sim
 */
public class NextBusiness {

    @Transactional
    public boolean produce(String parentId, AckuMessageService AckuMessageService, Producer producer){

        QB builder = QB.of(AckuMessage.class);
        builder.eq("parentId",parentId);
        builder.eq("status",MessageStatus.NEXT);
        Q q = builder.build();

        List<AckuMessage> list = AckuMessageService.listByCond(q);

        Date date = new Date();

        for (AckuMessage AckuMessage : list) {
            AckuMessageService.refresh(
                    QrB.of(AckuMessage.class).refresh("status",MessageStatus.SEND)
                            .refresh("sendAt",date.getTime())
                            .refresh("refreshAt", date)
                            .eq("id", AckuMessage.getId()).build()
            );
        }

        for (AckuMessage AckuMessage : list) {
            AckuDto dto = new AckuDto();
            dto.setMessage(AckuMessage);
            String message = JsonX.toJson(dto);
            String topic = AckuMessage.getTopic();
            boolean flag = producer.send(topic, message);
            if (!flag) {
                throw new AckuExceptioin("Next produce failed， topic: " + topic + ", message: " + message);
            }
        }

        return true;
    }
}
