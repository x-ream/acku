package io.xream.acku.controller;


import io.xream.acku.api.AckuProducer;
import io.xream.acku.bean.Cat;
import io.xream.acku.repository.CatRepository;
import io.xream.x7.base.web.ViewEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Transactional
@RestController
@RequestMapping("/payment")

/**
 * @author Sim
 */
public class PaymentController  {

    @Autowired
    private CatRepository repository;

    @AckuProducer(useTcc=true,topic = "CAT_PAID",  svcs = {"cat-order","cat-settle"})
    @RequestMapping("/pay")
    public ViewEntity pay(@RequestBody Cat cat) {

        repository.create(cat);

        return ViewEntity.ok();
    }
}
