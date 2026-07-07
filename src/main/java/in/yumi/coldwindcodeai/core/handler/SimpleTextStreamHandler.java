package in.yumi.coldwindcodeai.core.handler;

import in.yumi.coldwindcodeai.model.entity.User;
import in.yumi.coldwindcodeai.model.enums.ChatHistoryMessageTypeEnum;
import in.yumi.coldwindcodeai.service.ChatHistoryService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * 简单文本流处理器
 * 处理 HTML 和 MULTI_FILE 类型的流式响应
 */
@Slf4j
public class SimpleTextStreamHandler {

    /**
     * 处理传统流（HTML, MULTI_FILE）
     * 直接收集完整的文本响应
     *
     * @param originFlux         原始流
     * @param chatHistoryService 聊天历史服务
     * @param appId              应用ID
     * @param loginUser          登录用户
     * @return 处理后的流
     */
    public Flux<String> handle(Flux<String> originFlux,
                               ChatHistoryService chatHistoryService,
                               long appId, User loginUser) {
        StringBuilder aiResponseBuilder = new StringBuilder();
        return originFlux
                .map(chunk -> {
                    // 收集AI响应内容
                    aiResponseBuilder.append(chunk);
                    return chunk;
                })
                .doOnComplete(() -> {
                    // 流式响应完成后，添加AI消息到对话历史
                    // 生成代码的完整内容已在磁盘上，chat_history 只存摘要供 AI 记忆用
                    try {
                        String aiResponse = aiResponseBuilder.toString();
                        String savedMessage = truncateIfNeeded(aiResponse);
                        chatHistoryService.addChatMessage(appId, savedMessage, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                    } catch (Exception e) {
                        log.error("保存对话历史失败，appId={}", appId, e);
                    }
                })
                .doOnError(error -> {
                    // 如果AI回复失败，也要记录错误消息（不抛异常，避免破坏 SSE 流）
                    log.error("AI 流式响应异常，appId={}", appId, error);
                    try {
                        String errorMessage = "AI回复失败: " + error.getMessage();
                        chatHistoryService.addChatMessage(appId, errorMessage, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                    } catch (Exception e) {
                        log.error("保存错误消息失败，appId={}", appId, e);
                    }
                });
    }

    /**
     * 截断过长消息，避免 chat_history 存储完整生成代码。
     * 完整代码已在磁盘 tmp/code_output/ 下，chat_history 只需摘要供 AI 对话记忆。
     */
    private String truncateIfNeeded(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        int maxLength = 5000;
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength)
                + "\n\n... [内容已截断，完整代码文件已保存至磁盘，原长度: "
                + content.length() + " 字符]";
    }
}
