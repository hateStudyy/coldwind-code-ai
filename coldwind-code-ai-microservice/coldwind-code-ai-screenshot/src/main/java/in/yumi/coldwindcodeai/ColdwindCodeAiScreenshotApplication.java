package in.yumi.coldwindcodeai;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class ColdwindCodeAiScreenshotApplication {

    public static void main(String[] args) {
        SpringApplication.run(ColdwindCodeAiScreenshotApplication.class, args);
    }

}
