package com.cpms;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        System.out.println("---------------------------------------");
        System.out.println("");
        System.out.println("Post service is started");
        System.out.println("");
        System.out.println("---------------------------------------");

    }

}
