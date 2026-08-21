package com.traceops.traceops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TraceopsApplication {

    public static void main(String[] args) {
        SpringApplication.run(TraceopsApplication.class, args);
        System.err.println("Application started");
    }

}
