package org.maatram;

import org.maatram.config.DotenvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MaatramApplication {
    public static void main(String[] args) {
        DotenvLoader.load();
        SpringApplication.run(MaatramApplication.class, args);
    }
}
