package io.github.lgp547.anydoor.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages = "io.github.lgp547.anydoor.test.*")
class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

}
