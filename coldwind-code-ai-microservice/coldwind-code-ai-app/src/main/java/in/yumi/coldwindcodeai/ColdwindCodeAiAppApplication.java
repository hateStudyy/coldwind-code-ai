package in.yumi.coldwindcodeai;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
@MapperScan("in.yumi.coldwindcodeai.mapper")
@EnableCaching
public class ColdwindCodeAiAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(ColdwindCodeAiAppApplication.class, args);
    }

}
