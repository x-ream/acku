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

import io.xream.acku.api.acku.FailedService;
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
public class FailedServiceImpl implements FailedService {

    @Autowired
    private AckuMessageRepository ackuMessageRepository;

    @Override
    public boolean refresh(RefreshCondition<AckuMessage> condition) {
        return this.ackuMessageRepository.refresh(condition);
    }

    @Override
    public boolean refreshUnSafe(RefreshCondition<AckuMessage> condition) {
        return this.ackuMessageRepository.refreshUnSafe(condition);
    }

    @Override
    public List<Map<String, Object>> listByResultMap(Criteria.ResultMapCriteria ResultMapCriteria) {
        return this.ackuMessageRepository.list(ResultMapCriteria);
    }
}
