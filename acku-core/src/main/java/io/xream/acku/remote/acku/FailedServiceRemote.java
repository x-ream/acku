package io.xream.acku.remote.acku;

import io.xream.acku.bean.entity.AckuMessage;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;


/**
 * @author Sim
 */
@HttpExchange("http://${acku.app}/failed" )
public interface FailedServiceRemote {


    @GetExchange(value = "/find")
    List<AckuMessage> findFailed();

    @GetExchange(value = "/find/{topic}")
    List<AckuMessage> findFailedByTopic(@PathVariable("topic") String topic);

    @GetExchange(value = "/retryAll")
    boolean retryAll();

    @GetExchange(value = "/retry/{messageId}")
    boolean retry(@PathVariable("messageId") String messageId);

}
