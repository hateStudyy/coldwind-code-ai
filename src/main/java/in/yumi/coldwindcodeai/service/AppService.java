package in.yumi.coldwindcodeai.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
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

    String deployApp(Long appId, User loginUser);

    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

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
