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
package io.xream.acku.schedule;

import io.xream.acku.codetemplate.ScheduleTemplate;
import io.xream.acku.remote.ScheduledAckuServiceRemote;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;


/**
 * @author Sim
 */
@Configuration
public class CleanSchedule {

    private final static Logger logger = LoggerFactory.getLogger(CleanSchedule.class);

    public CleanSchedule(){
        logger.info("Clean Schedule Started");
    }

    @Resource
    private ScheduledAckuServiceRemote remote;

    @Autowired
    private ScheduleTemplate scheduleTemplate;


    @Scheduled(cron = "0 0 0/1 * * ?")
    public void clean(){

        scheduleTemplate.schedule(CleanSchedule.class, () -> remote.clean());
    }
}
