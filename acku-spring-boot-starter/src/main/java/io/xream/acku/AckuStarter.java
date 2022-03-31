package io.xream.acku;


import io.xream.rey.EnableReyClient;
import org.springframework.context.annotation.Configuration;


/**
 * @author Sim
 */
@Configuration
@EnableReyClient(basePackages = ("io.xream.acku.remote.reliable"))
public class AckuStarter {


}
