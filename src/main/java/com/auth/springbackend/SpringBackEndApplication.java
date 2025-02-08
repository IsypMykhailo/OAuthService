package com.auth.springbackend;

import com.auth.springbackend.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class SpringBackEndApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBackEndApplication.class, args);
    }

}
