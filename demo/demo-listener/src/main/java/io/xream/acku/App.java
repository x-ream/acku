package io.xream.acku;

import io.xream.acku.api.EnableAckuManagement;
import io.xream.x7.EnableCorsConfig;
import io.xream.x7.EnableDateToLongForJackson;
import io.xream.rey.EnableReyClient;
import io.xream.x7.EnableX7Repository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@SpringBootApplication
@EnableTransactionManagement
@EnableAckuManagement
@EnableX7Repository(baseTypeSupported = true)
@EnableDateToLongForJackson
@EnableReyClient
@EnableCorsConfig

/**
 * @author Sim
 */
public class App {

	
	public static void main(String[] args) {

		SpringApplication.run(App.class);
		
    }


}



