package in.yumi.coldwindcodeai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import in.yumi.coldwindcodeai.exception.GlobalExceptionHandler;
import in.yumi.coldwindcodeai.model.dto.chathistory.ChatHistoryQueryRequest;
import in.yumi.coldwindcodeai.model.entity.ChatHistory;
import in.yumi.coldwindcodeai.model.entity.User;
import in.yumi.coldwindcodeai.model.enums.UserRoleEnum;
import in.yumi.coldwindcodeai.service.ChatHistoryService;
import in.yumi.coldwindcodeai.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class ChatHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChatHistoryService chatHistoryService;

    @MockitoBean
    private UserService userService;

    private User mockUser() {
        return User.builder()
                .id(1L)
                .userAccount("testuser")
                .userRole(UserRoleEnum.USER.getValue())
                .build();
    }

    @Test
    void listAppChatHistory_shouldSucceed() throws Exception {
        User user = mockUser();
        when(userService.getLoginUser(any())).thenReturn(user);

        Page<ChatHistory> mockPage = new Page<>();
        when(chatHistoryService.listAppChatHistoryByPage(
                eq(10L), eq(20), isNull(), eq(user)))
                .thenReturn(mockPage);

        mockMvc.perform(get("/chatHistory/app/10")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void listAllChatHistoryByPageForAdmin_shouldSucceed() throws Exception {
        when(userService.getLoginUser(any())).thenReturn(User.builder().id(1L).userRole(UserRoleEnum.ADMIN.getValue()).build());
        when(chatHistoryService.getQueryWrapper(any())).thenReturn(QueryWrapper.create());
        when(chatHistoryService.page(any(Page.class), any(QueryWrapper.class))).thenReturn(new Page<>());

        ChatHistoryQueryRequest request = new ChatHistoryQueryRequest();
        request.setPageNum(1);
        request.setPageSize(10);

        mockMvc.perform(post("/chatHistory/admin/list/page/vo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }
}
