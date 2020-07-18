package com.contactservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class);

        System.out.println("-----------------");
        System.out.println("");
        System.out.println("CONTACT APPLICATION IS RUNNING");
        System.out.println("");
    }
}
