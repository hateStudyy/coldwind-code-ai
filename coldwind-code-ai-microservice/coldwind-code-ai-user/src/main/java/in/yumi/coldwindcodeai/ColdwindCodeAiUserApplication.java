package in.yumi.coldwindcodeai;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("in.yumi.coldwindcodeai.mapper")
@ComponentScan("in.yumi")
@EnableDubbo
public class ColdwindCodeAiUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(ColdwindCodeAiUserApplication.class, args);
    }
}
