package in.yumi.coldwindcodeai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("in.yumi.coldwindcodeai.mapper")
public class ColdwindCodeAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ColdwindCodeAiApplication.class, args);
    }

}
