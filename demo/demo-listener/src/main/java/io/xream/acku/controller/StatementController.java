package io.xream.acku.controller;


import io.xream.acku.bean.CatStatement;
import io.xream.acku.repository.CatStatementRepository;
import io.xream.x7.base.web.ViewEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/statement")

/**
 * @author Sim
 */
public class StatementController {

    @Autowired
    private CatStatementRepository catStatementRepository;

    @RequestMapping("/create")
    public ViewEntity create(@RequestBody CatStatement catStatement) {

        this.catStatementRepository.create(catStatement);

        return ViewEntity.ok();
    }

}
