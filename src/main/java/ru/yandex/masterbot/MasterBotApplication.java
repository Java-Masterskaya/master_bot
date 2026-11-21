package ru.yandex.masterbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MasterBotApplication {

  public static void main(String[] args) {
    SpringApplication.run(MasterBotApplication.class, args);
  }
}
