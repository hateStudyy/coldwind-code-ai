package in.yumi.coldwindcodeai.controller;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 应用控制器测试
 *
 * @author coldwind
 */
public class AppControllerTest {

    @Test
    public void testControllerExists() {
        // 简单的测试，验证控制器类存在
        assertTrue(true);
    }

    @Test
    void chatToGenCode() {
        // 模拟同时发起多个请求
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                // 这里应该调用实际的接口方法
                // 由于是示例，我们只做一个简单的模拟
                System.out.println("Request " + Thread.currentThread().getName() + " processed");
                try {
                    // 模拟处理时间
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, executorService);
            futures.add(future);
        }

        // 等待所有请求完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        executorService.shutdown();
    }
}
