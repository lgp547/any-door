package com.github.lgp.anydoor;

import com.github.lgp.anydoor.autoconfig.AnyDoorAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackageClasses = {AnyDoorAutoConfiguration.class, Test.class, TestRun.class})
class AnyDoorApplicationTests {

    public static void main(String[] args) {
        SpringApplication.run(AnyDoorApplicationTests.class, args);
    }

}
