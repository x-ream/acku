package io.xream.acku.repository;

import io.xream.acku.bean.Cat;
import io.xream.sqli.api.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository

/**
 * @author Sim
 */
public interface CatRepository extends BaseRepository<Cat> {
}
