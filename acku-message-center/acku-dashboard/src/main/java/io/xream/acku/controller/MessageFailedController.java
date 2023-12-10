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


import io.xream.acku.bean.entity.AckuMessage;
import io.xream.acku.remote.acku.FailedServiceRemote;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * @author Sim
 */
@RestController
@RequestMapping("/message/failed")
public class MessageFailedController {

    private Logger logger = LoggerFactory.getLogger(MessageFailedController.class);

    @Resource
    private FailedServiceRemote failedServiceRemote;

    @Autowired
    private AuthorizationBusiness authorizationBusiness;

    @RequestMapping(value = "/find",method = RequestMethod.GET)
    public ResponseEntity find() {
        return this.find(null,null);
    }
    @RequestMapping(value = "/retry/all",method = RequestMethod.GET)
    public ResponseEntity retryAll() {
        return this.retryAll(null,null);
    }
    @RequestMapping(value = "/retry/{messageId}",method = RequestMethod.GET)
    public ResponseEntity retry(@PathVariable String messageId) {
        return this.retry(messageId,null,null);
    }


    @RequestMapping(value = "/find/{token}/{userId}",method = RequestMethod.GET)
    public ResponseEntity find(@PathVariable String token, @PathVariable String userId) {

        if (! authorizationBusiness.isAccessble(token,userId)) {
            String redirect = this.authorizationBusiness.getRedirect();
            return ResponseEntity.ok(redirect);
        }

        List<AckuMessage> list = this.failedServiceRemote.findFailed();

        return ResponseEntity.ok(list);
    }


    @RequestMapping(value = "/retry/all/{token}/{userId}",method = RequestMethod.GET)
    public ResponseEntity retryAll(@PathVariable String token, @PathVariable String userId) {

        logger.info("retry all failed message");

        if (! authorizationBusiness.isAccessble(token,userId)) {
            String redirect = this.authorizationBusiness.getRedirect();
            return ResponseEntity.ok(redirect);
        }

        this.failedServiceRemote.retryAll();

        return ResponseEntity.ok("all");
    }


    @RequestMapping(value = "/retry/{messageId}/{token}/{userId}",method = RequestMethod.GET)
    public ResponseEntity retry(@PathVariable String messageId, @PathVariable String token, @PathVariable String userId) {

        logger.info("retry failed message: " + messageId);

        if (! authorizationBusiness.isAccessble(token,userId)) {
            String redirect = this.authorizationBusiness.getRedirect();
            return ResponseEntity.ok(redirect);
        }

        this.failedServiceRemote.retry(messageId);

        return ResponseEntity.ok(messageId);
    }
}
