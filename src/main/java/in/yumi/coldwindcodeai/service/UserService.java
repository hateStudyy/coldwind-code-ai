package in.yumi.coldwindcodeai.service;

import com.mybatisflex.core.service.IService;
import in.yumi.coldwindcodeai.model.entity.User;
import in.yumi.coldwindcodeai.model.vo.LoginUserVO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户 服务层。
 *
 * @author coldwind
 */
public interface UserService extends IService<User> {
    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户注册
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return 登录用户
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request
     * @return 是否注销成功
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取加密密码
     *
     * @param userPassword
     * @return 加密后的密码
     */
    String getEncryptPassword(String userPassword);

    /**
     * 获取脱敏的登录用户信息
     *
     * @param user
     * @return 脱敏后的用户信息
     */
    LoginUserVO getLoginUserVO(User user);
}
