package in.yumi.coldwindcodeai.benchmark;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * AI 速度测试 —— 直接用你的配置测 DeepSeek API 快不快。
 *
 * <pre>
 * 测试项：
 *   1. ping         — API 通不通？往返多快？
 *   2. 生成代码      — 真实 HTML 代码生成要多久？
 *   3. 流式首字      — 用户多久能看到第一个字？
 * </pre>
 */
@Slf4j
@Component
public class SpeedBenchmark {

    // 从你的配置文件中读取（application-local.yml / application-model.yml）
    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String apiKey;

    @Value("${langchain4j.open-ai.chat-model.base-url}")
    private String baseUrl;

    @Value("${langchain4j.open-ai.chat-model.model-name}")
    private String modelName;

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper json = new ObjectMapper();

    @PostConstruct
    public void autoTest() {
        log.info("==============================================");
        log.info("🔬 速度测试启动 (模型: {})", modelName);
        log.info("==============================================");
    }

    /** 跑全部测试，打印结果 */
    public void run() throws Exception {
        System.out.println("\n========== 🚀 速度测试 ==========");
        System.out.println("模型: " + modelName);
        System.out.println();

        testPing();
        testGenerateCode();
        testStreamTtfb();
    }

    // ---- 1. ping -------------------------------------------------

    public void testPing() throws Exception {
        long t = System.nanoTime();
        String body = chatBody("回复一个字：好", 5, 0);
        HttpResponse<String> resp = post(body);
        long ms = (System.nanoTime() - t) / 1_000_000;

        int out = json.readTree(resp.body()).path("usage").path("completion_tokens").asInt();
        System.out.printf("  ping   → %dms (输出了 %d 个字)\n", ms, out);
    }

    // ---- 2. 真实代码生成 -----------------------------------------

    public void testGenerateCode() throws Exception {
        long t = System.nanoTime();
        String body = chatBody("做一个「外科常用药介绍」的 HTML 页面", 2048, 0.3);
        HttpResponse<String> resp = post(body);
        long ms = (System.nanoTime() - t) / 1_000_000;

        var usage = json.readTree(resp.body()).path("usage");
        System.out.printf("  生成代码 → %dms (生成 %d tokens，总 %d tokens)\n",
                ms, usage.get("completion_tokens").asInt(), usage.get("total_tokens").asInt());
    }

    // ---- 3. 流式首字延迟 -----------------------------------------

    public void testStreamTtfb() throws Exception {
        ObjectNode root = json.createObjectNode();
        root.put("model", modelName);
        root.put("stream", true);
        root.put("max_tokens", 2048);
        root.put("temperature", 0.3);
        ArrayNode msgs = root.putArray("messages");
        msgs.addObject().put("role", "user").put("content", "做一个「外科常用药介绍」的 HTML 页面");

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/chat/completions"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("Accept", "text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(root.toString(), StandardCharsets.UTF_8))
                .timeout(Duration.ofSeconds(120))
                .build();

        long start = System.nanoTime();
        long firstTokenAt = 0;
        StringBuilder content = new StringBuilder();

        try (var reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(
                        client.send(req, HttpResponse.BodyHandlers.ofInputStream()).body(),
                        StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("data: ") && !line.equals("data: [DONE]")) {
                    if (firstTokenAt == 0) firstTokenAt = System.nanoTime();
                    String delta = json.readTree(line.substring(6))
                            .path("choices").get(0).path("delta").path("content").asText("");
                    content.append(delta);
                }
            }
        }

        long allMs = (System.nanoTime() - start) / 1_000_000;
        long ttfbMs = (firstTokenAt - start) / 1_000_000;
        System.out.printf("  流式首字 → %dms (总耗时 %dms，收到 %d 个字)\n", ttfbMs, allMs, content.length());
    }

    // ---- 工具 ---------------------------------------------------

    private String chatBody(String userMessage, int maxTokens, double temperature) throws Exception {
        ObjectNode root = json.createObjectNode();
        root.put("model", modelName);
        root.put("max_tokens", maxTokens);
        root.put("temperature", temperature);
        root.put("stream", false);
        ArrayNode msgs = root.putArray("messages");
        msgs.addObject().put("role", "user").put("content", userMessage);
        return json.writeValueAsString(root);
    }

    private HttpResponse<String> post(String body) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/chat/completions"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .timeout(Duration.ofSeconds(60))
                .build();
        return client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }
}
