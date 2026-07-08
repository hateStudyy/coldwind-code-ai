package in.yumi.coldwindcodeai.core.handler;

import in.yumi.coldwindcodeai.model.entity.User;
import in.yumi.coldwindcodeai.model.enums.ChatHistoryMessageTypeEnum;
import in.yumi.coldwindcodeai.service.ChatHistoryService;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleTextStreamHandlerTest {

    @Test
    void handle_shouldSaveFullAiResponseWithoutTruncating() {
        CapturingChatHistoryService capturingService = new CapturingChatHistoryService();
        ChatHistoryService chatHistoryService = capturingService.createProxy();
        User loginUser = new User();
        loginUser.setId(1001L);
        String fullResponse = "a".repeat(6000) + "完整结尾";

        List<String> chunks = new SimpleTextStreamHandler()
                .handle(Flux.just(fullResponse.substring(0, 3000), fullResponse.substring(3000)),
                        chatHistoryService, 2002L, loginUser)
                .collectList()
                .block();

        assertThat(chunks).containsExactly(fullResponse.substring(0, 3000), fullResponse.substring(3000));
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
