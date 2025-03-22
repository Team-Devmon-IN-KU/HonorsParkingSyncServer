package org.example.honorsparkingsyncserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HonorsparkingsyncserverApplication {

  public static void main(String[] args) {
    SpringApplication.run(HonorsparkingsyncserverApplication.class, args);
  }

}
