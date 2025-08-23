package in.yumi.coldwindcodeai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
public class ColdwindCodeAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ColdwindCodeAiApplication.class, args);
    }

}
