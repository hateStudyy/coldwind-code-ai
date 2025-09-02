package in.yumi.coldwindcodeai.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import in.yumi.coldwindcodeai.model.entity.ChatHistory;
import in.yumi.coldwindcodeai.mapper.ChatHistoryMapper;
import in.yumi.coldwindcodeai.service.ChatHistoryService;
import org.springframework.stereotype.Service;

/**
 * 对话历史 服务层实现。
 *
 * @author coldwind
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>  implements ChatHistoryService{

}
