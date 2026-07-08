package in.yumi.coldwindcodeai.controller;

import in.yumi.coldwindcodeai.constant.AppConstant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class DeployResourceControllerTest {

    private final DeployResourceController controller = new DeployResourceController();
    private final Path deployRoot = Paths.get(AppConstant.CODE_DEPLOY_ROOT_DIR);

    @AfterEach
    void tearDown() throws Exception {
        Path testDir = deployRoot.resolve("testDeployKey");
        if (Files.exists(testDir)) {
            try (var paths = Files.walk(testDir)) {
                paths.sorted((left, right) -> right.compareTo(left))
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (Exception ignored) {
                            }
                        });
            }
        }
    }

    @Test
    void serveDeployResource_shouldReturnIndexFromDeployDirectory() throws Exception {
        Path indexFile = deployRoot.resolve("testDeployKey/index.html");
        Files.createDirectories(indexFile.getParent());
        Files.writeString(indexFile, "<h1>deployed</h1>");

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/deploy/testDeployKey/");
        request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, "/deploy/testDeployKey/");

        ResponseEntity<Resource> response = controller.serveDeployResource("testDeployKey", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getFirst("Content-Type")).isEqualTo("text/html; charset=UTF-8");
        assertThat(response.getBody()).isNotNull();
        assertThat(new String(response.getBody().getInputStream().readAllBytes(), StandardCharsets.UTF_8))
                .isEqualTo("<h1>deployed</h1>");
    }

    @Test
    void redirectToDeployRoot_shouldAppendTrailingSlash() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/deploy/testDeployKey");

        ResponseEntity<Resource> response = controller.redirectToDeployRoot("testDeployKey", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.MOVED_PERMANENTLY);
        assertThat(response.getHeaders().getFirst("Location")).isEqualTo("/deploy/testDeployKey/");
    }

    @Test
    void serveDeployResource_shouldRejectPathTraversal() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/deploy/testDeployKey/../secret.txt");
        request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, "/deploy/testDeployKey/../secret.txt");

        ResponseEntity<Resource> response = controller.serveDeployResource("testDeployKey", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
