package in.yumi.coldwindcodeai.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import in.yumi.coldwindcodeai.model.entity.App;
import in.yumi.coldwindcodeai.mapper.AppMapper;
import in.yumi.coldwindcodeai.service.AppService;
import org.springframework.stereotype.Service;

/**
 * 应用 服务层实现。
 *
 * @author coldwind
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService {

}
