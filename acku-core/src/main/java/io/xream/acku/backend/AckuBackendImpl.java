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
package io.xream.acku.backend;

import io.xream.acku.TCCTopic;
import io.xream.acku.api.acku.DtoConverter;
import io.xream.acku.bean.constant.MessageStatus;
import io.xream.acku.bean.dto.AckuDto;
import io.xream.acku.bean.dto.ConsumedAckuDto;
import io.xream.acku.bean.entity.AckuMessage;
import io.xream.acku.bean.entity.MessageResult;
import io.xream.acku.bean.exception.AckuExceptioin;
import io.xream.acku.interner.AckuBackend;
import io.xream.acku.interner.MessageTraceable;
import io.xream.acku.remote.acku.AckuServiceRemote;
import io.xream.internal.util.ExceptionUtil;
import io.xream.x7.base.GenericObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Sim
 */
public class AckuBackendImpl implements AckuBackend {

    @Autowired
    private AckuServiceRemote ackuServiceRemote;

    @Autowired
    private DtoConverter dtoConverter;

    @Override
    @Transactional
    public Object produceReliably(Boolean useTcc, String id, int retryMax, boolean underConstruction, String topic, Object body, MessageTraceable MessageTraceable, String[] svcs, Callable callable) {

        String tracingId = null;
        if (MessageTraceable != null){
            tracingId = MessageTraceable.getTracingId();
        }
        AckuMessage reliableMessage = new AckuMessage(id,retryMax,underConstruction,tracingId,topic, body, Arrays.asList(svcs));
        reliableMessage.setTcc(useTcc ? TCCTopic._TCC_TRY.name() : TCCTopic._TCC_NONE.name());
        if (useTcc) {
            reliableMessage.resetTopic(TCCTopic._TCC_TRY);
        }
        AckuDto dto = new AckuDto(reliableMessage);
        dto = this.ackuServiceRemote.create(dto); //STEP 1

        Object result = null;
        try {
            result = callable.call(); //STEP 2
        } catch (Exception e) {
            throw new AckuExceptioin(ExceptionUtil.getMessage(e));
        }

        this.ackuServiceRemote.produce(dto); //STEP 3

        return result;
    }


    @Override
    @Transactional
    public void onConsumed(String svc, Object message, Runnable runnable) {

        AckuDto dto = this.dtoConverter.convertOnConsumed(message);

        if (dto.isConsumed(svc))
            return;

        List<MessageResult> list = dto.getResultList();
        MessageResult mr = null;
        for (MessageResult messageResult : list) {
            if (messageResult.getSvc().equals(svc)) { //可以是spring.application.name
                mr = messageResult;
                break;
            }
        }

        try {
            runnable.run(); //STEP 1
        } catch (Exception e) {
            throw new AckuExceptioin(ExceptionUtil.getMessage(e));
        }

        ConsumedAckuDto cdto = dto.consume(svc,dto.getMessage().getTcc(), mr);
        ackuServiceRemote.consume(cdto); //STEP 2

    }

    @Override
    public boolean createNext(String id, int retryMax, String nextTopic, Object nextBody,Object preMessage,String[] svcs) {

        AckuDto dto = this.dtoConverter.convertOnConsumed(preMessage);

        String parentId = dto.getParentId();//先get parentId

        GenericObject go = new GenericObject(nextBody);
        Date date = new Date();
        AckuMessage reliableMessage = dto.getMessage();
        reliableMessage.setId(id);
        reliableMessage.setTracingId(dto.getTracingId());
        reliableMessage.setParentId(parentId);
        reliableMessage.setTopic(nextTopic);
        reliableMessage.setBody(go);
        reliableMessage.setTcc(TCCTopic._TCC_NONE.name());
        reliableMessage.setRetryMax(retryMax);
        reliableMessage.setUnderConstruction(false);
        reliableMessage.setSvcDone("&");
        reliableMessage.setSvcList(Arrays.asList(svcs));
        reliableMessage.setSendAt(0);
        reliableMessage.setRetryCount(0L);
        reliableMessage.setCreateAt(date);
        reliableMessage.setRefreshAt(date);
        reliableMessage.setStatus(MessageStatus.NEXT.name());

        dto.setMessage(reliableMessage);
        dto.setResultList(new ArrayList<>());

        this.ackuServiceRemote.create(dto);
        return true;
    }

    @Override
    public boolean tryToConfirm(String msgId) {
        return this.ackuServiceRemote.tryToConfirm(msgId);
    }

    @Override
    public boolean cancel(String msgId) {
        return this.ackuServiceRemote.cancel(msgId);
    }


}
