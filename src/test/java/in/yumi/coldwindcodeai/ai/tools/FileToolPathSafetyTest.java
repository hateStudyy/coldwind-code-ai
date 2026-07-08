package in.yumi.coldwindcodeai.ai.tools;

import in.yumi.coldwindcodeai.constant.AppConstant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

class FileToolPathSafetyTest {

    private final Long appId = 987654321L;
    private final Path projectRoot = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, "vue_project_" + appId);

    @AfterEach
    void tearDown() throws IOException {
        if (!Files.exists(projectRoot)) {
            return;
        }
        try (var paths = Files.walk(projectRoot)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                        }
                    });
        }
    }

    @Test
    void writeFileShouldStayInsideProjectRoot() {
        FileWriteTool tool = new FileWriteTool();

        String result = tool.writeFile("src/App.vue", "<template />", appId);

        Assertions.assertTrue(result.contains("文件写入成功"));
        Assertions.assertTrue(Files.exists(projectRoot.resolve("src/App.vue")));
    }

    @Test
    void writeFileShouldRejectPathTraversal() {
        FileWriteTool tool = new FileWriteTool();
        Path escapedPath = projectRoot.getParent().resolve("escape.txt");

        String result = tool.writeFile("../escape.txt", "bad", appId);

        Assertions.assertTrue(result.contains("路径不安全"));
        Assertions.assertFalse(Files.exists(escapedPath));
    }

    @Test
    void deleteFileShouldRejectAbsolutePath() throws IOException {
        FileDeleteTool tool = new FileDeleteTool();
        Path file = Files.createTempFile("coldwind-tool-", ".txt");

        try {
            String result = tool.deleteFile(file.toString(), appId);

            Assertions.assertTrue(result.contains("只允许访问应用项目内的相对路径"));
            Assertions.assertTrue(Files.exists(file));
        } finally {
            Files.deleteIfExists(file);
        }
    }
}
