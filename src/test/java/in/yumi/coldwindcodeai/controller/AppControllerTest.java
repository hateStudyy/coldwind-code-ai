package in.yumi.coldwindcodeai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import in.yumi.coldwindcodeai.common.DeleteRequest;
import in.yumi.coldwindcodeai.constant.UserConstant;
import in.yumi.coldwindcodeai.exception.ErrorCode;
import in.yumi.coldwindcodeai.exception.GlobalExceptionHandler;
import in.yumi.coldwindcodeai.model.dto.app.AppAddRequest;
import in.yumi.coldwindcodeai.model.dto.app.AppAdminUpdateRequest;
import in.yumi.coldwindcodeai.model.dto.app.AppDeployRequest;
import in.yumi.coldwindcodeai.model.dto.app.AppQueryRequest;
import in.yumi.coldwindcodeai.model.dto.app.AppUpdateRequest;
import in.yumi.coldwindcodeai.model.entity.App;
import in.yumi.coldwindcodeai.model.entity.User;
import in.yumi.coldwindcodeai.model.enums.UserRoleEnum;
import in.yumi.coldwindcodeai.model.vo.AppVO;
import in.yumi.coldwindcodeai.service.AppService;
import in.yumi.coldwindcodeai.service.ProjectDownloadService;
import in.yumi.coldwindcodeai.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Flux;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class AppControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AppService appService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ProjectDownloadService projectDownloadService;

    // ==================== 测试数据 ====================

    private User mockUser() {
        return User.builder()
                .id(1L)
                .userAccount("testuser")
                .userRole(UserRoleEnum.USER.getValue())
                .build();
    }

    private User mockAdmin() {
        return User.builder()
                .id(2L)
                .userAccount("admin")
                .userRole(UserRoleEnum.ADMIN.getValue())
                .build();
    }

    private App mockApp() {
        return App.builder()
                .id(10L)
                .appName("测试应用")
                .userId(1L)
                .initPrompt("生成一个个人主页")
                .build();
    }

    private AppVO mockAppVO() {
        AppVO vo = new AppVO();
        vo.setId(10L);
        vo.setAppName("测试应用");
        vo.setUserId(1L);
        return vo;
    }

    // ==================== 创建应用 ====================

    @Test
    void addApp_shouldSucceed() throws Exception {
        User user = mockUser();
        when(userService.getLoginUser(any())).thenReturn(user);

        AppAddRequest request = new AppAddRequest();
        request.setInitPrompt("生成一个个人主页");

        when(appService.createApp(any(), eq(user))).thenReturn(100L);

        mockMvc.perform(post("/app/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(100L));
    }

    // ==================== 部署应用 ====================

    @Test
    void deployApp_shouldSucceed() throws Exception {
        User user = mockUser();
        when(userService.getLoginUser(any())).thenReturn(user);
        when(appService.deployApp(eq(10L), eq(user))).thenReturn("https://example.com/app/10");

        AppDeployRequest request = new AppDeployRequest();
        request.setAppId(10L);

        mockMvc.perform(post("/app/deploy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value("https://example.com/app/10"));
    }

    // ==================== SSE 对话生成代码 ====================

    @Test
    void chatToGenCode_shouldReturnEventStream() throws Exception {
        User user = mockUser();
        when(userService.getLoginUser(any())).thenReturn(user);

        Flux<String> mockFlux = Flux.just("data1", "data2", "data3");
        when(appService.chatToGenCode(eq(10L), eq("做个网站"), eq(user)))
                .thenReturn(mockFlux);

        mockMvc.perform(get("/app/chat/gen/code")
                        .param("appId", "10")
                        .param("message", "做个网站")
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM));
    }

    // ==================== 更新应用 ====================

    @Test
    void updateApp_shouldSucceed_whenOwner() throws Exception {
        User user = mockUser();
        when(userService.getLoginUser(any())).thenReturn(user);

        App app = mockApp();
        app.setUserId(1L); // 属于当前用户
        when(appService.getById(10L)).thenReturn(app);
        when(appService.updateById(any())).thenReturn(true);

        AppUpdateRequest request = new AppUpdateRequest();
        request.setId(10L);
        request.setAppName("新名字");

        mockMvc.perform(post("/app/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));
    }

    // ==================== 删除应用 ====================

    @Test
    void deleteApp_shouldSucceed_whenOwner() throws Exception {
        User user = mockUser();
        when(userService.getLoginUser(any())).thenReturn(user);

        App app = mockApp();
        app.setUserId(1L);
        when(appService.getById(10L)).thenReturn(app);
        when(appService.removeById(10L)).thenReturn(true);

        DeleteRequest request = new DeleteRequest();
        request.setId(10L);

        mockMvc.perform(post("/app/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));
    }

    // ==================== 获取应用详情 ====================

    @Test
    void getAppVOById_shouldSucceed() throws Exception {
        App app = mockApp();
        when(appService.getById(10L)).thenReturn(app);

        AppVO vo = mockAppVO();
        when(appService.getAppVO(app)).thenReturn(vo);

        mockMvc.perform(get("/app/get/vo?id=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.appName").value("测试应用"));
    }

    @Test
    void getAppVOById_shouldFail_whenNotFound() throws Exception {
        when(appService.getById(999L)).thenReturn(null);

        mockMvc.perform(get("/app/get/vo?id=999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.NOT_FOUND_ERROR.getCode()));
    }

    // ==================== 我的应用列表 ====================

    @Test
    void listMyAppVOByPage_shouldSucceed() throws Exception {
        User user = mockUser();
        when(userService.getLoginUser(any())).thenReturn(user);

        when(appService.getQueryWrapper(any())).thenReturn(QueryWrapper.create());
        when(appService.page(any(Page.class), any(QueryWrapper.class))).thenReturn(new Page<>());
        when(appService.getAppVOList(any())).thenReturn(java.util.Collections.emptyList());

        AppQueryRequest request = new AppQueryRequest();
        request.setPageNum(1);
        request.setPageSize(10);

        mockMvc.perform(post("/app/my/list/page/vo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    // ==================== 精选应用列表 ====================

    @Test
    void listGoodAppVOByPage_shouldSucceed() throws Exception {
        when(appService.getQueryWrapper(any())).thenReturn(QueryWrapper.create());
        when(appService.page(any(Page.class), any(QueryWrapper.class))).thenReturn(new Page<>());
        when(appService.getAppVOList(any())).thenReturn(java.util.Collections.emptyList());

        AppQueryRequest request = new AppQueryRequest();
        request.setPageNum(1);
        request.setPageSize(10);

        mockMvc.perform(post("/app/good/list/page/vo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    // ==================== 管理员删除应用 ====================

    @Test
    void deleteAppByAdmin_shouldSucceed() throws Exception {
        when(userService.getLoginUser(any())).thenReturn(mockAdmin());
        App app = mockApp();
        when(appService.getById(10L)).thenReturn(app);
        when(appService.removeById(10L)).thenReturn(true);

        DeleteRequest request = new DeleteRequest();
        request.setId(10L);

        mockMvc.perform(post("/app/admin/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));
    }

    // ==================== 管理员更新应用 ====================

    @Test
    void updateAppByAdmin_shouldSucceed() throws Exception {
        when(userService.getLoginUser(any())).thenReturn(mockAdmin());
        App app = mockApp();
        when(appService.getById(10L)).thenReturn(app);
        when(appService.updateById(any())).thenReturn(true);

        AppAdminUpdateRequest request = new AppAdminUpdateRequest();
        request.setId(10L);
        request.setAppName("管理员更新的名字");
        request.setPriority(1);

        mockMvc.perform(post("/app/admin/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));
    }

    // ==================== 管理员分页查询应用 ====================

    @Test
    void listAppVOByPageByAdmin_shouldSucceed() throws Exception {
        when(userService.getLoginUser(any())).thenReturn(mockAdmin());
        when(appService.getQueryWrapper(any())).thenReturn(QueryWrapper.create());
        when(appService.page(any(Page.class), any(QueryWrapper.class))).thenReturn(new Page<>());
        when(appService.getAppVOList(any())).thenReturn(java.util.Collections.emptyList());

        AppQueryRequest request = new AppQueryRequest();
        request.setPageNum(1);
        request.setPageSize(10);

        mockMvc.perform(post("/app/admin/list/page/vo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    // ==================== 管理员获取应用详情 ====================

    @Test
    void getAppVOByIdByAdmin_shouldSucceed() throws Exception {
        when(userService.getLoginUser(any())).thenReturn(mockAdmin());
        App app = mockApp();
        when(appService.getById(10L)).thenReturn(app);

        AppVO vo = mockAppVO();
        when(appService.getAppVO(app)).thenReturn(vo);

        mockMvc.perform(get("/app/admin/get/vo?id=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.appName").value("测试应用"));
    }

    // ==================== 下载应用代码 ====================

    @Test
    void downloadAppCode_shouldSucceed() throws Exception {
        User user = mockUser();
        when(userService.getLoginUser(any())).thenReturn(user);

        App app = App.builder()
                .id(10L)
                .userId(1L) // 属于当前用户
                .codeGenType("HTML")
                .build();
        when(appService.getById(10L)).thenReturn(app);

        mockMvc.perform(get("/app/download/10"))
                .andExpect(status().isOk());
    }
}
