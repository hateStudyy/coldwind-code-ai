package in.yumi.coldwindcodeai;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("in.yumi.coldwindcodeai.mapper")
@EnableCaching
public class ColdwindCodeAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ColdwindCodeAiApplication.class, args);
    }

}
// todo 用ai尝试spring security
// todo 用cc在idea里生成一个完整的企业项目应该是微服务项目有crm 定时任务 调度任务等等系统的
