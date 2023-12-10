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
package io.xream.acku.remote;

import io.xream.acku.bean.entity.AckuMessage;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

/**
 * @author Sim
 */
@HttpExchange("http://${acku.app}/schedule" )
public interface ScheduledAckuServiceRemote {


    @RequestMapping(value = "/retry")
    boolean retry(AckuMessage message);

    @RequestMapping(value = "/listForRetry", method = RequestMethod.GET)
    List<AckuMessage> listForRetry();

    @RequestMapping(value = "/tryToFinish",method = RequestMethod.GET)
    boolean tryToFinish();

    @RequestMapping(value = "/tryToProduceNext",method = RequestMethod.GET)
    boolean tryToProduceNext();

    @RequestMapping(value = "/clean",method = RequestMethod.GET)
    boolean clean();
}
