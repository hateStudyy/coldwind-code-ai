package in.yumi.coldwindcodeai.service.impl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatHistoryMemoryCompressorTest {

    @Test
    void compactAiMessage_shouldKeepShortMessage() {
        String message = "短回复";

        String result = ChatHistoryMemoryCompressor.compactAiMessage(message);

        assertThat(result).isEqualTo(message);
    }

    @Test
    void compactAiMessage_shouldCompactLongMessageForMemoryOnly() {
        String head = "h".repeat(3000);
        String middle = "m".repeat(2500);
        String tail = "t".repeat(1000);
        String message = head + middle + tail;

        String result = ChatHistoryMemoryCompressor.compactAiMessage(message);

        assertThat(result).contains("历史 AI 回复已压缩");
        assertThat(result).contains("原长度: " + message.length() + " 字符");
        assertThat(result).contains(head);
        assertThat(result).contains(tail);
        assertThat(result).doesNotContain(middle);
    }
}
