package com.tka.patientmanagementsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PatientManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(PatientManagementSystemApplication.class, args);
        System.err.println("Patient app started");
    }

}
