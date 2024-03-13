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
package io.xream.acku.remote.acku;

import io.xream.acku.bean.dto.AckuDto;
import io.xream.acku.bean.dto.ConsumedAckuDto;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;


/**
 * @author Sim
 */
@HttpExchange("http://${acku.app}/message" )
public interface AckuServiceRemote {

    @PostExchange("/create")
    AckuDto create(@RequestBody AckuDto dto);

    @PostExchange("/produce")
    boolean produce(@RequestBody AckuDto dto);

    @PostExchange("/consume")
    boolean consume(@RequestBody ConsumedAckuDto dto);

    @PostExchange("/tryToConfirm")
    boolean tryToConfirm(String msgId);

    @PostExchange("/cancel")
    boolean cancel(String msgId);

}
