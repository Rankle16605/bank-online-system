package com.icbc.online.service;

import com.icbc.online.dto.request.AiChatRequest;
import com.icbc.online.dto.request.AiImageRequest;
import com.icbc.online.dto.response.AiChatResponse;
import com.icbc.online.dto.response.AiSessionDTO;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * AI客服服务接口 - 小工智能助手
 */
public interface AiChatService {

    /** 普通聊天（非流式兜底） */
    AiChatResponse chat(String username, AiChatRequest request);

    /** 流式聊天（SSE） */
    Flux<String> chatStream(String username, AiChatRequest request);

    /** 获取用户会话列表 */
    List<AiSessionDTO> getSessions(Long userId);

    /** 删除会话（软删除） */
    void deleteSession(Long userId, String sessionId);

    /** 获取会话历史消息 */
    List<AiChatResponse> getSessionHistory(Long userId, String sessionId);

    /** 创建新会话 */
    String createSession(Long userId);

    /** 生成图片 */
    AiChatResponse generateImage(String username, AiImageRequest request);
}
