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
package io.xream.acku.interner;

import io.xream.acku.api.AckuOnConsumed;
import io.xream.internal.util.ExceptionUtil;
import io.xream.internal.util.StringUtil;
import io.xream.internal.util.VerifyUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author Sim
 */
@Aspect
public class AckuOnConsumedAspect {

    private final static Logger logger = LoggerFactory.getLogger(AckuBackend.class);


    public AckuOnConsumedAspect() {
        logger.info("Acku OnConsumed Enabled");
    }

    @Autowired
    private AckuBackend backend;

    @Pointcut("@annotation(io.xream.acku.api.AckuOnConsumed))")
    public void cut() {

    }

    @Around("cut() && @annotation(ackuOnConsumed) ")
    public void around(ProceedingJoinPoint proceedingJoinPoint, AckuOnConsumed ackuOnConsumed) {

        Object[] args = proceedingJoinPoint.getArgs();
        Object message = args[0];

        Signature signature = proceedingJoinPoint.getSignature();
        String logStr = signature.getDeclaringTypeName() + "." + signature.getName();

        String nextTopic = ackuOnConsumed.nextTopic();
        String[] svcs = ackuOnConsumed.nextSvcs();
        if (StringUtil.isNotNull(nextTopic)){
            if (svcs == null || svcs.length == 0){
                throw new IllegalArgumentException(logStr + ", if config nextTopic, svcs of io.xream.x7.acku.AckuOnConsumed can not null, nextTopic: " + nextTopic);
            }
        }

        String svc = ackuOnConsumed.svc();
        if (StringUtil.isNullOrEmpty(svc)){
            svc = VerifyUtil.toMD5(logStr).substring(0,10);
        }

        this.backend.onConsumed(svc, message,
                () -> {
                    try {
                        MethodSignature ms = ((MethodSignature) signature);
                        if (ms.getReturnType() == void.class) {
                            proceedingJoinPoint.proceed();
                        } else {
                            Object nextBody = proceedingJoinPoint.proceed();
                            String id = MessageIdGenerator.get();
                            int maxTry = ackuOnConsumed.nextRetryMax();
                            if (StringUtil.isNotNull(nextTopic)){
                                boolean flag = this.backend.createNext(id,maxTry,nextTopic,nextBody,message,svcs);
                                if (!flag){
                                    throw new RuntimeException(logStr + ", produce next topic failed: topic: " + nextTopic + ", message:"+ message + ",next body: " + nextBody);
                                }
                            }
                        }
                    } catch (Throwable e) {
                        if (e instanceof RuntimeException){
                            throw (RuntimeException) e;
                        }
                        throw new RuntimeException(ExceptionUtil.getMessage(e));
                    }
                }
        );

    }

}
