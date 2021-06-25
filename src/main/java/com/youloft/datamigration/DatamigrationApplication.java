package com.youloft.datamigration;

import com.spring4all.mongodb.EnableMongoPlus;
import com.youloft.datamigration.tools.DDRobot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableMongoPlus
@EnableScheduling
@SpringBootApplication
public class DatamigrationApplication {

    public static void main(String[] args) {
        System.out.println("START OK!");
        SpringApplication.run(DatamigrationApplication.class, args);
    }

}
