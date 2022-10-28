package io.github.lgp547.anydoor;

import io.github.lgp547.anydoor.autoconfig.AnyDoorAutoConfiguration;
import io.github.lgp547.anydoor.core.Bean;
import io.github.lgp547.anydoor.core.Controller;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackageClasses = {AnyDoorAutoConfiguration.class, Controller.class, Bean.class})
class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

}
