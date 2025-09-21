package in.yumi.coldwindcodeai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("in.yumi.coldwindcodeai.mapper")
@ComponentScan("in.yumi")
public class ColdwindCodeAiUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(ColdwindCodeAiUserApplication.class, args);
    }
}
