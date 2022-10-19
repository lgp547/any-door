package io.github.lgp547.anydoor;

import io.github.lgp547.anydoor.autoconfig.AnyDoorAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackageClasses = {AnyDoorAutoConfiguration.class, Test.class, TestRun.class})
class AnyDoorApplicationTests {

    public static void main(String[] args) {
        SpringApplication.run(AnyDoorApplicationTests.class, args);
    }

}
