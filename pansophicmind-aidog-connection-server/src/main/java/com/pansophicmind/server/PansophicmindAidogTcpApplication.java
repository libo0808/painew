package com.pansophicmind.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan({"com.pansophicmind.server.aidog.**.mapper"})
public class PansophicmindAidogTcpApplication {

    public static void main(String[] args) {
        SpringApplication.run(PansophicmindAidogTcpApplication.class, args);
    }

    static {
        System.setProperty("druid.mysql.usePingMethod", "false");
    }

}
