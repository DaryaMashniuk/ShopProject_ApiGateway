package com.innowise.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.config.GatewayNoLoadBalancerClientAutoConfiguration;

@SpringBootApplication(exclude = {
				GatewayNoLoadBalancerClientAutoConfiguration.class
})
public class ApigatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApigatewayApplication.class, args);
	}

}
