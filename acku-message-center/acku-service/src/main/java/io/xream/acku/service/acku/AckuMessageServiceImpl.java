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

import io.xream.acku.api.acku.AckuMessageService;
import io.xream.acku.bean.entity.AckuMessage;
import io.xream.acku.repository.acku.AckuMessageRepository;
import io.xream.sqli.builder.Criteria;
import io.xream.sqli.builder.RefreshCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author Sim
 */
@Service
public class AckuMessageServiceImpl implements AckuMessageService {

    @Autowired
    private AckuMessageRepository repository;
    @Override
    public boolean create(AckuMessage message) {
        this.repository.create(message);
        return true;
    }

    @Override
    public boolean refresh(RefreshCondition<AckuMessage> refreshCondition) {
        return this.repository.refresh(refreshCondition);
    }

    @Override
    public boolean remove(String id) {
        return this.repository.remove(id);
    }

    @Override
    public List<AckuMessage> listByCriteria(Criteria criteria) {
        return this.repository.list(criteria);
    }

    @Override
    public List<Map<String, Object>> listByResultMap(Criteria.ResultMapCriteria ResultMapCriteria) {
        return this.repository.list(ResultMapCriteria);
    }

    @Override
    public AckuMessage get(String msgId) {
        return this.repository.get(msgId);
    }
}
