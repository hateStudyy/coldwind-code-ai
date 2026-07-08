package in.yumi.coldwindcodeai.ai.tools;

import cn.hutool.json.JSONObject;
import in.yumi.coldwindcodeai.constant.AppConstant;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 工具基类
 * 定义所有工具的通用接口
 */
public abstract class BaseTool {

    protected Path resolveProjectPath(String relativePath, Long appId) {
        if (appId == null || appId <= 0) {
            throw new IllegalArgumentException("应用 ID 无效");
        }
        Path requestedPath = Paths.get(relativePath == null ? "" : relativePath);
        if (requestedPath.isAbsolute()) {
            throw new IllegalArgumentException("只允许访问应用项目内的相对路径");
        }
        Path projectRoot = getProjectRoot(appId);
        Path resolvedPath = projectRoot.resolve(requestedPath).normalize();
        if (!resolvedPath.startsWith(projectRoot)) {
            throw new IllegalArgumentException("路径不安全，不能访问应用项目目录之外的文件");
        }
        return resolvedPath;
    }

    protected Path getProjectRoot(Long appId) {
        String projectDirName = "vue_project_" + appId;
        return Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, projectDirName)
                .toAbsolutePath()
                .normalize();
    }

    /**
     * 获取工具的英文名称（对应方法名）
     *
     * @return 工具英文名称
     */
    public abstract String getToolName();

    /**
     * 获取工具的中文显示名称
     *
     * @return 工具中文名称
     */
    public abstract String getDisplayName();

    /**
     * 生成工具请求时的返回值（显示给用户）
     *
     * @return 工具请求显示内容
     */
    public String generateToolRequestResponse() {
        return String.format("\n\n[选择工具] %s\n\n", getDisplayName());
    }

    /**
     * 生成工具执行结果格式（保存到数据库）
     *
     * @param arguments 工具执行参数
     * @return 格式化的工具执行结果
     */
    public abstract String generateToolExecutedResult(JSONObject arguments);
} 
