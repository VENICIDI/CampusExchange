// 文件路径: ...\src\main\java\org\campusmarket\exchange\CampusExchangeApplication.java
package org.campusmarket.exchange;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 主应用入口类
 */
@SpringBootApplication
@ComponentScan(basePackages = {"org.campusmarket.exchange.controller", "org.campusmarket.exchange.service", 
                              "org.campusmarket.exchange.config", "org.campusmarket.exchange"})
public class CampusExchangeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusExchangeApplication.class, args);
    }

}