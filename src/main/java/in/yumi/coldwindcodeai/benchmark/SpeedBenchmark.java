package in.yumi.coldwindcodeai.benchmark;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AI 代码生成速度基准测试
 * <p>
 * 测试 DeepSeek API 在不同配置下的响应速度，测量流式 TTFB、吞吐量、prompt 长度影响等指标。
 * <p>
 * 不需要 Spring 上下文，可直接运行 main 方法。
 */
@Slf4j
public class SpeedBenchmark {

    private static final String API_KEY = "sk-7f6f353d033f464baeca23bb566e4542";
    private static final String BASE_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int WARMUP = 1;
    private static final int ITERATIONS = 3;

    private final HttpClient httpClient;

    public SpeedBenchmark() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    // ========================================================================
    //  测试项目
    // ========================================================================

    /** 1. 基础延迟 — 1 token 回复，测 API 往返延迟 */
    public BenchmarkResult testPingLatency() throws Exception {
        return runTest("Ping（极小回复 - 1 token）", ITERATIONS, () -> {
            long start = System.nanoTime();
            String body = buildBody("deepseek-chat",
                    List.of(msg("user", "回复一个字：好")), 5, 0);
            HttpResponse<String> resp = postJson(body);
            long elapsed = msSince(start);
            return Map.of("耗时(ms)", (double) elapsed);
        });
    }

    /** 2. 短 prompt（模拟路由分类，约 180 tokens） */
    public BenchmarkResult testShortPrompt() throws Exception {
        return runTest("短 Prompt 路由分类（~180 tokens）", ITERATIONS, () -> {
            long start = System.nanoTime();
            String body = buildBody("deepseek-chat",
                    List.of(
                            msg("system", "你是一个专业的代码生成方案路由器，根据用户需求返回最合适的代码生成类型。可选类型：HTML、MULTI_FILE、VUE_PROJECT"),
                            msg("user", "外科常用药网站")
                    ), 100, 0.1);
            HttpResponse<String> resp = postJson(body);
            long elapsed = msSince(start);
            int completionTokens = parseCompletionTokens(resp.body());
            return Map.of("耗时(ms)", (double) elapsed, "生成tokens", (double) completionTokens);
        });
    }

    /** 3. 完整 HTML 代码生成（非流式，模拟当前真实场景） */
    public BenchmarkResult testFullGeneration() throws Exception {
        String systemPrompt = readPrompt("prompt/codegen-html-system-prompt.txt");
        return runTest("完整 HTML 代码生成（~500 tokens）", ITERATIONS, () -> {
            long start = System.nanoTime();
            String body = buildBody("deepseek-chat",
                    List.of(msg("system", systemPrompt), msg("user", "外科常用药网站")),
                    2048, 0.3);
            HttpResponse<String> resp = postJson(body);
            long elapsed = msSince(start);
            return Map.of("耗时(ms)", (double) elapsed,
                    "生成tokens", (double) parseCompletionTokens(resp.body()),
                    "总tokens", (double) parseTotalTokens(resp.body()));
        });
    }

    /** 4. 流式 TTFB + 吞吐量（使用 InputStream 读取流式响应） */
    public BenchmarkResult testStreaming() throws Exception {
        String systemPrompt = readPrompt("prompt/codegen-html-system-prompt.txt");
        return runTest("流式生成（TTFB + 吞吐量）", ITERATIONS, () -> {
            ObjectNode root = MAPPER.createObjectNode();
            root.put("model", "deepseek-chat");
            root.put("stream", true);
            root.put("max_tokens", 2048);
            root.put("temperature", 0.3);
            root.put("stream_options", MAPPER.createObjectNode().put("include_usage", true));
            ArrayNode msgs = root.putArray("messages");
            msgs.addObject().put("role", "system").put("content", systemPrompt);
            msgs.addObject().put("role", "user").put("content", "外科常用药网站");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .header("Accept", "text/event-stream")
                    .POST(HttpRequest.BodyPublishers.ofString(root.toString(), StandardCharsets.UTF_8))
                    .timeout(Duration.ofSeconds(120))
                    .build();

            long start = System.nanoTime();
            AtomicLong firstTokenAt = new AtomicLong(0);
            StringBuilder content = new StringBuilder();
            int lineCount = 0;

            // 使用 InputStream 读取流式响应，逐行处理
            HttpResponse<java.io.InputStream> resp = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofInputStream());

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resp.body(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lineCount++;
                    if (line.startsWith("data: ") && !line.equals("data: [DONE]")) {
                        firstTokenAt.compareAndSet(0, System.nanoTime());
                        String json = line.substring(6);
                        try {
                            var node = MAPPER.readTree(json);
                            String delta = node.path("choices").get(0).path("delta").path("content").asText("");
                            content.append(delta);
                        } catch (Exception ignored) {
                        }
                    }
                }
            }

            long totalElapsed = msSince(start);
            long ttfb = firstTokenAt.get() > 0
                    ? (firstTokenAt.get() - start) / 1_000_000
                    : totalElapsed;

            return Map.of("TTFB(ms)", (double) ttfb,
                    "总耗时(ms)", (double) totalElapsed,
                    "生成字符数", (double) content.length(),
                    "接收行数", (double) lineCount);
        });
    }

    /** 5. Prompt 长度对延迟的影响 */
    public BenchmarkResult testPromptSize() throws Exception {
        Map<String, Double> results = new LinkedHashMap<>();
        int[][] cases = {{10, 0}, {200, 50}, {800, 200}};
        String[] labels = {"短 prompt (~10 tokens)", "中 prompt (~200 tokens)", "长 prompt (~800 tokens)"};

        for (int ci = 0; ci < cases.length; ci++) {
            String prompt = "回复一个字：好" + "测试。".repeat(cases[ci][1]);
            double avgTime = 0;
            for (int i = 0; i < ITERATIONS; i++) {
                long start = System.nanoTime();
                String body = buildBody("deepseek-chat",
                        List.of(msg("system", prompt), msg("user", "你好")), 10, 0);
                postJson(body);
                avgTime += msSince(start);
            }
            results.put(labels[ci], avgTime / ITERATIONS);
        }
        return new BenchmarkResult("Prompt 长度对延迟影响", results);
    }

    /** 6. max_tokens 对速度的影响 */
    public BenchmarkResult testMaxTokens() throws Exception {
        Map<String, Double> results = new LinkedHashMap<>();
        for (int maxTokens : new int[]{256, 512, 1024, 2048}) {
            double avgTime = 0;
            for (int i = 0; i < ITERATIONS; i++) {
                long start = System.nanoTime();
                String body = buildBody("deepseek-chat",
                        List.of(
                                msg("system", "你是资深前端工程师。生成一个完整的 HTML 页面。"),
                                msg("user", "一个美观的外科常用药介绍页面，包含导航和卡片布局")
                        ), maxTokens, 0.3);
                postJson(body);
                avgTime += msSince(start);
            }
            results.put("max_tokens=" + maxTokens, avgTime / ITERATIONS);
        }
        return new BenchmarkResult("max_tokens 对生成速度影响", results);
    }

    /** 7. 并发吞吐量 */
    public BenchmarkResult testConcurrent() throws Exception {
        Map<String, Double> results = new LinkedHashMap<>();
        for (int concurrency : new int[]{1, 3, 5}) {
            ExecutorService executor = Executors.newFixedThreadPool(concurrency);
            List<Callable<Long>> tasks = new ArrayList<>();
            for (int i = 0; i < concurrency; i++) {
                tasks.add(() -> {
                    long start = System.nanoTime();
                    String body = buildBody("deepseek-chat",
                            List.of(msg("user", "回复一个字：好")), 5, 0);
                    postJson(body);
                    return msSince(start);
                });
            }
            List<Future<Long>> futures = executor.invokeAll(tasks);
            long totalTime = 0;
            for (Future<Long> f : futures) totalTime += f.get();
            executor.shutdown();
            results.put(concurrency + " 并发平均(ms)", (double) totalTime / concurrency);
        }
        return new BenchmarkResult("并发吞吐量测试", results);
    }

    // ========================================================================
    //  报告生成
    // ========================================================================

    public String runAll() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append("=".repeat(70)).append("\n");
        sb.append("  🔬 Coldwind Code AI — 速度基准测试报告\n");
        sb.append("  📅 ").append(new Date()).append("\n");
        sb.append("=".repeat(70)).append("\n\n");

        try {
            sb.append("🔄 预热中...\n");
            warmup();
            sb.append("✅ 预热完成\n\n");

            appendResult(sb, testPingLatency());
            appendResult(sb, testShortPrompt());
            appendResult(sb, testFullGeneration());
            appendResult(sb, testStreaming());
            appendResult(sb, testPromptSize());
            appendResult(sb, testMaxTokens());
            appendResult(sb, testConcurrent());

            sb.append("─".repeat(70)).append("\n");
            sb.append("  📊 分析与优化建议\n");
            sb.append("─".repeat(70)).append("\n");
            sb.append(generateAdvice());
            sb.append("\n");

        } catch (Exception e) {
            sb.append("❌ 测试出错：").append(e.getMessage()).append("\n");
            log.error("基准测试失败", e);
        }
        sb.append("=".repeat(70)).append("\n");
        return sb.toString();
    }

    private void warmup() throws Exception {
        for (int i = 0; i < WARMUP; i++) {
            String body = buildBody("deepseek-chat",
                    List.of(msg("user", "回复：好")), 5, 0);
            postJson(body);
        }
    }

    private void appendResult(StringBuilder sb, BenchmarkResult r) {
        sb.append("  ▸ ").append(r.name()).append("\n");
        if (r.isSingle()) {
            sb.append(String.format("    %s: %.0f\n", r.singleLabel(), r.singleVal()));
        } else {
            for (var e : r.values().entrySet()) {
                sb.append(String.format("    %-32s %8.0f\n", e.getKey() + ":", e.getValue()));
            }
        }
        sb.append("\n");
    }

    private String generateAdvice() {
        return """
                ⚡ 行业标杆参考（Claude Code 水平）：
                  • TTFB（首 token 延迟）: < 2s
                  • 代码生成吞吐量: > 30 tokens/s
                  • 简单问答: < 1s

                🔧 优化方向：
                  1. 精简 system prompt（当前 HTML prompt ~500 tokens，精简可省 30% 时间）
                  2. 降低 max_tokens（8192 → 4096 已改）
                  3. 输出流式优先，首 token 到达即展示
                  4. 预加载/缓存 system prompt，避免重复读取文件
                  5. 考虑升级模型 deepseek-chat-v4（新模型更快）
                """;
    }

    // ========================================================================
    //  工具方法
    // ========================================================================

    private BenchmarkResult runTest(String name, int iterations, Callable<Map<String, Double>> fn) throws Exception {
        Map<String, List<Double>> all = new LinkedHashMap<>();
        for (int i = 0; i < iterations; i++) {
            Map<String, Double> r = fn.call();
            for (var e : r.entrySet())
                all.computeIfAbsent(e.getKey(), k -> new ArrayList<>()).add(e.getValue());
        }
        Map<String, Double> avg = new LinkedHashMap<>();
        for (var e : all.entrySet())
            avg.put(e.getKey(), e.getValue().stream().mapToDouble(d -> d).average().orElse(0));
        return new BenchmarkResult(name, avg);
    }

    private static long msSince(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }

    private String buildBody(String model, List<Map<String, String>> messages, int maxTokens, double temperature) throws Exception {
        ObjectNode root = MAPPER.createObjectNode();
        root.put("model", model);
        root.put("max_tokens", maxTokens);
        root.put("temperature", temperature);
        root.put("stream", false);
        ArrayNode arr = root.putArray("messages");
        for (Map<String, String> m : messages) {
            arr.addObject().put("role", m.get("role")).put("content", m.get("content"));
        }
        return MAPPER.writeValueAsString(root);
    }

    private static Map<String, String> msg(String role, String content) {
        return Map.of("role", role, "content", content);
    }

    private HttpResponse<String> postJson(String jsonBody) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .timeout(Duration.ofSeconds(60))
                .build();
        return httpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private String readPrompt(String path) {
        try {
            var is = getClass().getClassLoader().getResourceAsStream(path);
            if (is == null) return "(未找到:" + path + ")";
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "(读取失败:" + e.getMessage() + ")";
        }
    }

    private int parseCompletionTokens(String body) {
        try {
            return MAPPER.readTree(body).path("usage").path("completion_tokens").asInt();
        } catch (Exception e) {
            return 0;
        }
    }

    private int parseTotalTokens(String body) {
        try {
            return MAPPER.readTree(body).path("usage").path("total_tokens").asInt();
        } catch (Exception e) {
            return 0;
        }
    }

    // ========================================================================
    //  入口
    // ========================================================================

    public static void main(String[] args) {
        System.out.println(new SpeedBenchmark().runAll());
    }

    // ========================================================================
    //  数据模型
    // ========================================================================

    public record BenchmarkResult(String name, Map<String, Double> values) {
        public boolean isSingle() {
            return values.size() == 1;
        }

        public String singleLabel() {
            return values.keySet().iterator().next();
        }

        public double singleVal() {
            return values.values().iterator().next();
        }
    }
}
