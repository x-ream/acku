package io.xream.acku;

import io.xream.acku.bean.Cat;
import io.xream.rey.annotation.ReyClient;
import io.xream.x7.base.web.ViewEntity;
import org.springframework.web.bind.annotation.RequestMapping;

@ReyClient(value = "${acku.demo}/payment")

/**
 * @author Sim
 */
public interface PaymentServiceRemote {

    @RequestMapping("/pay")
    ViewEntity pay(Cat cat);
}
