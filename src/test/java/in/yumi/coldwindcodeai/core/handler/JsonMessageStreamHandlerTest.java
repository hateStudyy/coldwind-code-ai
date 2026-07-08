package in.yumi.coldwindcodeai.core.handler;

import cn.hutool.json.JSONUtil;
import in.yumi.coldwindcodeai.ai.model.message.AiResponseMessage;
import in.yumi.coldwindcodeai.model.entity.User;
import in.yumi.coldwindcodeai.model.enums.ChatHistoryMessageTypeEnum;
import in.yumi.coldwindcodeai.service.ChatHistoryService;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JsonMessageStreamHandlerTest {

    @Test
    void handle_shouldSaveFullAiResponseWithoutTruncating() {
        CapturingChatHistoryService capturingService = new CapturingChatHistoryService();
        ChatHistoryService chatHistoryService = capturingService.createProxy();
        User loginUser = new User();
        loginUser.setId(1001L);
        String firstChunk = "b".repeat(3500);
        String secondChunk = "c".repeat(3000) + "完整结尾";
        String fullResponse = firstChunk + secondChunk;

        List<String> chunks = new JsonMessageStreamHandler()
                .handle(Flux.just(
                                JSONUtil.toJsonStr(new AiResponseMessage(firstChunk)),
                                JSONUtil.toJsonStr(new AiResponseMessage(secondChunk))
                        ),
                        chatHistoryService, 2002L, loginUser)
                .collectList()
                .block();

        assertThat(chunks).containsExactly(firstChunk, secondChunk);
        assertThat(capturingService.appId).isEqualTo(2002L);
        assertThat(capturingService.message).isEqualTo(fullResponse);
        assertThat(capturingService.message).doesNotContain("内容已截断");
        assertThat(capturingService.messageType).isEqualTo(ChatHistoryMessageTypeEnum.AI.getValue());
        assertThat(capturingService.userId).isEqualTo(1001L);
    }

    private static class CapturingChatHistoryService implements InvocationHandler {

        private Long appId;
        private String message;
        private String messageType;
        private Long userId;

        private ChatHistoryService createProxy() {
            return (ChatHistoryService) Proxy.newProxyInstance(
                    ChatHistoryService.class.getClassLoader(),
                    new Class[]{ChatHistoryService.class},
                    this
            );
        }

        @Override
        public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) {
            if ("addChatMessage".equals(method.getName())) {
                appId = (Long) args[0];
                message = (String) args[1];
                messageType = (String) args[2];
                userId = (Long) args[3];
                return true;
            }
            if (method.getReturnType().equals(boolean.class)) {
                return false;
            }
            if (method.getReturnType().equals(int.class)) {
                return 0;
            }
            return null;
        }
    }
}
