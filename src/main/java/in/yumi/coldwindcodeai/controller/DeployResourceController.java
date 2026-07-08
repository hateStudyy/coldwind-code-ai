package in.yumi.coldwindcodeai.controller;

import in.yumi.coldwindcodeai.constant.AppConstant;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/deploy")
public class DeployResourceController {

    private static final Path DEPLOY_ROOT_DIR = Paths.get(AppConstant.CODE_DEPLOY_ROOT_DIR)
            .toAbsolutePath()
            .normalize();

    /**
     * 访问已部署作品，访问格式：http://localhost:8999/api/deploy/{deployKey}/
     */
    @GetMapping("/{deployKey}")
    public ResponseEntity<Resource> redirectToDeployRoot(
            @PathVariable String deployKey,
            HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", request.getRequestURI() + "/");
        return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
    }

    @GetMapping("/{deployKey}/**")
    public ResponseEntity<Resource> serveDeployResource(
            @PathVariable String deployKey,
            HttpServletRequest request) {
        try {
            String resourcePath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            resourcePath = resourcePath.substring(("/deploy/" + deployKey).length());

            if (resourcePath.isEmpty()) {
                HttpHeaders headers = new HttpHeaders();
                headers.add("Location", request.getRequestURI() + "/");
                return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
            }
            if (resourcePath.equals("/")) {
                resourcePath = "/index.html";
            }

            Path deployDir = DEPLOY_ROOT_DIR.resolve(deployKey)
                    .toAbsolutePath()
                    .normalize();
            Path targetPath = DEPLOY_ROOT_DIR.resolve(deployKey + resourcePath)
                    .toAbsolutePath()
                    .normalize();
            if (!deployDir.startsWith(DEPLOY_ROOT_DIR) || !targetPath.startsWith(deployDir)) {
                return ResponseEntity.notFound().build();
            }

            File file = targetPath.toFile();
            if (!file.exists() || !file.isFile()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .header("Content-Type", getContentTypeWithCharset(file.getName()))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String getContentTypeWithCharset(String fileName) {
        if (fileName.endsWith(".html")) return "text/html; charset=UTF-8";
        if (fileName.endsWith(".css")) return "text/css; charset=UTF-8";
        if (fileName.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".svg")) return "image/svg+xml";
        if (fileName.endsWith(".webp")) return "image/webp";
        return "application/octet-stream";
    }
}
