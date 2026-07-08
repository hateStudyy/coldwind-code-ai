package in.yumi.coldwindcodeai.service.impl;

import cn.hutool.core.util.StrUtil;

/**
 * 压缩加载到 AI memory 的历史回复，避免把完整代码反复塞回模型上下文。
 */
final class ChatHistoryMemoryCompressor {

    private static final int MAX_AI_MEMORY_MESSAGE_LENGTH = 5000;
    private static final int HEAD_LENGTH = 3000;
    private static final int TAIL_LENGTH = 1000;

    private ChatHistoryMemoryCompressor() {
    }

    static String compactAiMessage(String message) {
        if (StrUtil.isBlank(message) || message.length() <= MAX_AI_MEMORY_MESSAGE_LENGTH) {
            return message;
        }

        String head = message.substring(0, HEAD_LENGTH);
        String tail = message.substring(message.length() - TAIL_LENGTH);
        return "[历史 AI 回复已压缩，原长度: " + message.length() + " 字符]\n"
                + "完整内容保存在聊天历史中，用于前端展示；这里仅保留首尾片段供 AI 记忆。\n\n"
                + "--- 前 " + HEAD_LENGTH + " 字符 ---\n"
                + head
                + "\n\n--- 后 " + TAIL_LENGTH + " 字符 ---\n"
                + tail;
    }
}
