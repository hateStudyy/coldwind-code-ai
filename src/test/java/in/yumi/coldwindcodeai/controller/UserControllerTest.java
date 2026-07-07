package in.yumi.coldwindcodeai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import in.yumi.coldwindcodeai.common.BaseResponse;
import in.yumi.coldwindcodeai.common.DeleteRequest;
import in.yumi.coldwindcodeai.common.ResultUtils;
import in.yumi.coldwindcodeai.constant.UserConstant;
import in.yumi.coldwindcodeai.exception.BusinessException;
import in.yumi.coldwindcodeai.exception.ErrorCode;
import in.yumi.coldwindcodeai.exception.GlobalExceptionHandler;
import in.yumi.coldwindcodeai.model.dto.user.UserAddRequest;
import in.yumi.coldwindcodeai.model.dto.user.UserLoginRequest;
import in.yumi.coldwindcodeai.model.dto.user.UserQueryRequest;
import in.yumi.coldwindcodeai.model.dto.user.UserRegisterRequest;
import in.yumi.coldwindcodeai.model.dto.user.UserUpdateRequest;
import in.yumi.coldwindcodeai.model.entity.User;
import in.yumi.coldwindcodeai.model.enums.UserRoleEnum;
import in.yumi.coldwindcodeai.model.vo.LoginUserVO;
import in.yumi.coldwindcodeai.model.vo.UserVO;
import in.yumi.coldwindcodeai.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    // ==================== 测试数据工厂 ====================

    private User mockUser() {
        return User.builder()
                .id(1L)
                .userAccount("testuser")
                .userName("测试用户")
                .userRole(UserRoleEnum.USER.getValue())
                .build();
    }

    private User mockAdmin() {
        return User.builder()
                .id(2L)
                .userAccount("admin")
                .userName("管理员")
                .userRole(UserRoleEnum.ADMIN.getValue())
                .build();
    }

    private LoginUserVO mockLoginUserVO() {
        LoginUserVO vo = new LoginUserVO();
        vo.setId(1L);
        vo.setUserAccount("testuser");
        vo.setUserName("测试用户");
        vo.setUserRole(UserRoleEnum.USER.getValue());
        return vo;
    }

    // ==================== 登录 ====================

    @Test
    void userLogin_shouldSucceed() throws Exception {
        UserLoginRequest request = new UserLoginRequest();
        request.setUserAccount("testuser");
        request.setUserPassword("12345678");

        when(userService.userLogin(eq("testuser"), eq("12345678"), any()))
                .thenReturn(mockLoginUserVO());

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.userAccount").value("testuser"));
    }

    @Test
    void userLogin_shouldFailWhenPasswordWrong() throws Exception {
        UserLoginRequest request = new UserLoginRequest();
        request.setUserAccount("testuser");
        request.setUserPassword("wrongpass");

        when(userService.userLogin(eq("testuser"), eq("wrongpass"), any()))
                .thenThrow(new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误"));

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.PARAMS_ERROR.getCode()))
                .andExpect(jsonPath("$.message").value("用户不存在或密码错误"));
    }

    // ==================== 注册 ====================

    @Test
    void userRegister_shouldSucceed() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setUserAccount("newuser");
        request.setUserPassword("12345678");
        request.setCheckPassword("12345678");

        when(userService.userRegister(eq("newuser"), eq("12345678"), eq("12345678")))
                .thenReturn(3L);

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(3L));
    }

    @Test
    void userRegister_shouldFailWhenPasswordMismatch() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setUserAccount("newuser");
        request.setUserPassword("12345678");
        request.setCheckPassword("87654321");

        when(userService.userRegister(any(), any(), any()))
                .thenThrow(new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致"));

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.PARAMS_ERROR.getCode()));
    }

    // ==================== 获取当前登录用户 ====================

    @Test
    void getLoginUser_shouldReturnUserWhenLoggedIn() throws Exception {
        User loginUser = mockUser();
        LoginUserVO loginUserVO = mockLoginUserVO();

        when(userService.getLoginUser(any())).thenReturn(loginUser);
        when(userService.getLoginUserVO(loginUser)).thenReturn(loginUserVO);

        mockMvc.perform(get("/user/get/login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.userAccount").value("testuser"));
    }

    @Test
    void getLoginUser_shouldFailWhenNotLoggedIn() throws Exception {
        when(userService.getLoginUser(any()))
                .thenThrow(new BusinessException(ErrorCode.NOT_LOGIN_ERROR));

        mockMvc.perform(get("/user/get/login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.NOT_LOGIN_ERROR.getCode()));
    }

    // ==================== 注销 ====================

    @Test
    void userLogout_shouldSucceed() throws Exception {
        when(userService.userLogout(any())).thenReturn(true);

        mockMvc.perform(post("/user/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));
    }

    // ==================== 管理员：添加用户 ====================

    @Test
    void addUser_shouldSucceed_whenAdmin() throws Exception {
        User admin = mockAdmin();
        when(userService.getLoginUser(any())).thenReturn(admin);

        UserAddRequest request = new UserAddRequest();
        request.setUserAccount("newstaff");
        request.setUserRole(UserRoleEnum.USER.getValue());

        User userToSave = new User();
        when(userService.save(any())).thenReturn(true);
        when(userService.getEncryptPassword(anyString())).thenReturn("encrypted");

        mockMvc.perform(post("/user/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    // ==================== 管理员：获取用户 ====================

    @Test
    void getUserById_shouldSucceed_whenAdmin() throws Exception {
        User admin = mockAdmin();
        when(userService.getLoginUser(any())).thenReturn(admin);

        User found = mockUser();
        when(userService.getById(1L)).thenReturn(found);

        mockMvc.perform(get("/user/get?id=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.userAccount").value("testuser"));
    }

    @Test
    void getUserById_shouldFail_whenNotFound() throws Exception {
        User admin = mockAdmin();
        when(userService.getLoginUser(any())).thenReturn(admin);

        when(userService.getById(999L)).thenReturn(null);

        mockMvc.perform(get("/user/get?id=999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.NOT_FOUND_ERROR.getCode()));
    }

    // ==================== 获取用户 VO ====================

    @Test
    void getUserVOById_shouldSucceed() throws Exception {
        User admin = mockAdmin();
        when(userService.getLoginUser(any())).thenReturn(admin);

        User user = mockUser();
        when(userService.getById(1L)).thenReturn(user);

        UserVO vo = new UserVO();
        vo.setId(1L);
        vo.setUserAccount("testuser");
        vo.setUserName("测试用户");
        when(userService.getUserVO(user)).thenReturn(vo);

        mockMvc.perform(get("/user/get/vo?id=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.userAccount").value("testuser"));
    }

    // ==================== 管理员：删除用户 ====================

    @Test
    void deleteUser_shouldSucceed_whenAdmin() throws Exception {
        User admin = mockAdmin();
        when(userService.getLoginUser(any())).thenReturn(admin);

        DeleteRequest request = new DeleteRequest();
        request.setId(1L);
        when(userService.removeById(1L)).thenReturn(true);

        mockMvc.perform(post("/user/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));
    }

    // ==================== 管理员：更新用户 ====================

    @Test
    void updateUser_shouldSucceed_whenAdmin() throws Exception {
        User admin = mockAdmin();
        when(userService.getLoginUser(any())).thenReturn(admin);

        UserUpdateRequest request = new UserUpdateRequest();
        request.setId(1L);
        request.setUserName("新名字");

        when(userService.updateById(any())).thenReturn(true);

        mockMvc.perform(post("/user/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));
    }

    // ==================== 管理员：分页查询用户 ====================

    @Test
    void listUserVOByPage_shouldSucceed_whenAdmin() throws Exception {
        User admin = mockAdmin();
        when(userService.getLoginUser(any())).thenReturn(admin);

        UserQueryRequest request = new UserQueryRequest();
        request.setPageNum(1);
        request.setPageSize(10);

        // Mock 分页查询
        when(userService.getQueryWrapper(any())).thenReturn(QueryWrapper.create());
        when(userService.page(any(Page.class), any(QueryWrapper.class))).thenReturn(new Page<>());
        when(userService.getUserVOList(any())).thenReturn(java.util.Collections.emptyList());

        mockMvc.perform(post("/user/list/page/vo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }
}
