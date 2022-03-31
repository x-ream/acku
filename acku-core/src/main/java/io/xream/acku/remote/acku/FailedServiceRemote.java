package io.xream.acku.remote.acku;

import io.xream.acku.bean.entity.AckuMessage;
import io.xream.rey.annotation.ReyClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;


/**
 * @author Sim
 */
@ReyClient("http://${reliable.app}/failed" )
public interface FailedServiceRemote {


    @RequestMapping(value = "/find", method = RequestMethod.GET)
    List<AckuMessage> findFailed();

    @RequestMapping(value = "/find/{topic}", method = RequestMethod.GET)
    List<AckuMessage> findFailedByTopic();

    @RequestMapping(value = "/retryAll", method = RequestMethod.GET)
    boolean retryAll();

    @RequestMapping(value = "/retry/{messageId}", method = RequestMethod.GET)
    boolean retry(String messageId);

}
