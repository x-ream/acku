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
package io.xream.acku.service.acku;

import io.xream.acku.api.acku.MessageResultService;
import io.xream.acku.bean.entity.MessageResult;
import io.xream.acku.repository.acku.MessageResultRepository;
import io.xream.sqli.api.NativeRepository;
import io.xream.sqli.builder.Q;
import io.xream.sqli.builder.Qr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Sim
 */
@Service
public class MessageResultServiceImpl implements MessageResultService {

    @Autowired
    private MessageResultRepository repository;
    @Autowired
    private NativeRepository nativeRepository;

    public boolean create(MessageResult result) {
        this.repository.create(result);
        return true;
    }

    public boolean refresh(Qr<MessageResult> condition) {
        return this.repository.refresh(condition);
    }

    public boolean remove(String id) {
        return this.repository.remove(id);
    }

    @Override
    public boolean removeByMessageId(String id) {
        String sql = "delete from messageResult where msgId = ?";
        return nativeRepository.execute(sql ,id);
    }

    @Override
    public List<MessageResult> listByCond(Q q) {
        return this.repository.list(q);
    }


}
