package in.yumi.coldwindcodeai.benchmark;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 验证 AI 测速功能正常、配置生效
 */
@SpringBootTest
class SpeedBenchmarkTest {

    @Resource
    private SpeedBenchmark speedBenchmark;

    @Test
    void testAll() throws Exception {
        Assertions.assertNotNull(speedBenchmark);
        speedBenchmark.run();  // 会打印测速结果
        System.out.println("✅ 测速完成，配置正常！");
    }
}
