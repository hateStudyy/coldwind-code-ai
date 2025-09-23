package in.yumi.coldwindcodeai.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import in.yumi.coldwindcodeai.model.dto.app.AppAddRequest;
import in.yumi.coldwindcodeai.model.dto.app.AppQueryRequest;
import in.yumi.coldwindcodeai.model.entity.App;
import in.yumi.coldwindcodeai.model.entity.User;
import in.yumi.coldwindcodeai.model.vo.AppVO;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author coldwind
 */
public interface AppService extends IService<App> {

    /**
     * 创建应用
     *
     * @param appId
     * @param loginUser
     * @return
     */
    String deployApp(Long appId, User loginUser);

    Long createApp(AppAddRequest appAddRequest, User loginUser);

    /**
     * 聊天生成代码
     *
     * @param appId
     * @param message
     * @param loginUser
     * @return
     */
    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    /**
     * 异步生成应用截图
     *
     * @param appId
     * @param appUrl
     */
    void generateAppScreenshotAsync(Long appId, String appUrl);

    /**
     * 获取应用封装类
     *
     * @param app
     * @return
     */
    AppVO getAppVO(App app);

    /**
     * 获取应用封装类列表
     *
     * @param appList
     * @return
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 构造应用查询条件
     *
     * @param appQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

}
