package org.pss.pss_solutions;

import org.springframework.boot.SpringApplication;

public class TestPssSolutionsApplication {

    public static void main(String[] args) {
        SpringApplication.from(PssSolutionsApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
