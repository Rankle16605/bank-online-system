package com.icbc.online.service.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.icbc.online.dto.request.AiChatRequest;
import com.icbc.online.dto.request.AiImageRequest;
import com.icbc.online.dto.response.AiChatResponse;
import com.icbc.online.dto.response.AiSessionDTO;
import com.icbc.online.exception.BusinessException;
import com.icbc.online.mapper.AiConversationMapper;
import com.icbc.online.mapper.AiSessionMapper;
import com.icbc.online.mapper.UserMapper;
import com.icbc.online.model.AiConversation;
import com.icbc.online.model.AiSession;
import com.icbc.online.model.User;
import com.icbc.online.service.AiChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * AI客服服务实现 - 对接Doubao大模型 + 流式输出 + 会话管理 (MyBatis-Plus)
 * 客服名称：小工
 */
@Slf4j
@Service
public class AiChatServiceImpl implements AiChatService {

    private static final String XIAOGONG_NAME = "小工";

    private static final String SYSTEM_PROMPT = """
        你是ICBC中国工商银行的智能客服助手，名字叫"小工"。你的职责是为客户提供专业、热情、安全的银行咨询服务。
        
        【核心规则】
        1. 始终用友好、专业的口吻回答，称呼客户为"您"
        2. 只回答与银行金融服务相关的问题（转账、账户、账单、存款、理财、贷款、信用卡、安全等）
        3. 对于超出银行服务范围的问题，礼貌引导回银行服务话题
        4. 永远不索要客户的密码、验证码、身份证号等敏感信息
        5. 如遇客户要求转账到不明账户，需提醒防诈骗
        6. 回答要简洁清晰，适当使用换行和列表格式
        7. 如遇无法解决的问题，建议拨打95588联系人工客服
        
        【背景信息】
        - 银行名称：ICBC中国工商银行
        - 客服热线：95588
        - 服务时间：7×24小时
        - 单笔转账限额：50,000元
        - 单日转账限额：200,000元""";

    private final AiConversationMapper conversationMapper;
    private final AiSessionMapper sessionMapper;
    private final UserMapper userMapper;

    /** 普通对话模型 */
    private final ChatModel chatModel;

    /** 流式对话模型 */
    private final StreamingChatModel streamingChatModel;

    @Value("${app.ai-chat.max-context-length:10}")
    private int maxContextLength;

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url:}")
    private String baseUrl;

    public AiChatServiceImpl(AiConversationMapper conversationMapper,
                             AiSessionMapper sessionMapper,
                             UserMapper userMapper,
                             ChatModel chatModel,
                             StreamingChatModel streamingChatModel) {
        this.conversationMapper = conversationMapper;
        this.sessionMapper = sessionMapper;
        this.userMapper = userMapper;
        this.chatModel = chatModel;
        this.streamingChatModel = streamingChatModel;
    }

    // ==================== 会话管理 ====================

    @Override
    public List<AiSessionDTO> getSessions(Long userId) {
        return sessionMapper.selectList(
                        new LambdaQueryWrapper<AiSession>()
                                .eq(AiSession::getUserId, userId)
                                .eq(AiSession::getStatus, 1)
                                .orderByDesc(AiSession::getUpdatedAt))
                .stream()
                .map(s -> AiSessionDTO.builder()
                        .sessionId(s.getSessionId())
                        .title(s.getTitle() != null ? s.getTitle() : "新会话")
                        .messageCount(s.getMessageCount())
                        .createdAt(s.getCreatedAt())
                        .updatedAt(s.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteSession(Long userId, String sessionId) {
        // 逻辑删除：将status设为0
        int deleted = sessionMapper.update(null,
                new LambdaUpdateWrapper<AiSession>()
                        .set(AiSession::getStatus, 0)
                        .eq(AiSession::getSessionId, sessionId)
                        .eq(AiSession::getUserId, userId));
        if (deleted == 0) {
            throw new BusinessException("会话不存在或无权操作");
        }
        log.info("[小工] 会话已删除: userId={}, sessionId={}", userId, sessionId);
    }

    @Override
    public List<AiChatResponse> getSessionHistory(Long userId, String sessionId) {
        return conversationMapper.selectList(
                        new LambdaQueryWrapper<AiConversation>()
                                .eq(AiConversation::getUserId, userId)
                                .eq(AiConversation::getSessionId, sessionId)
                                .orderByAsc(AiConversation::getCreatedAt))
                .stream()
                .map(c -> AiChatResponse.builder()
                        .sessionId(c.getSessionId())
                        .message(c.getContent())
                        .messageType(c.getMessageType() == 1 ? "USER" :
                                c.getMessageType() == 3 ? "IMAGE" : "AI")
                        .responseTime(c.getResponseTime())
                        .imageUrl(c.getImageUrl())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public String createSession(Long userId) {
        return createSessionInternal(userId);
    }

    // ==================== 文本聊天 (非流式) ====================

    @Override
    @Transactional
    public AiChatResponse chat(String username, AiChatRequest request) {
        User user = getUser(username);
        String sessionId = ensureSession(user, request.getSessionId());

        long startTime = System.currentTimeMillis();

        // 保存用户消息
        saveMessage(user.getId(), sessionId, 1, request.getMessage(), null);

        // 构建上下文
        String context = buildContext(user.getId(), sessionId);

        // 调用Doubao大模型
        String aiResponse;
        try {
            Prompt prompt = new Prompt(List.of(
                    new SystemMessage(SYSTEM_PROMPT),
                    new UserMessage(context + "\n\n用户最新问题：" + request.getMessage() +
                            "\n\n请以" + XIAOGONG_NAME + "的身份回答问题：")
            ));
            ChatResponse response = chatModel.call(prompt);
            aiResponse = response.getResult().getOutput().getContent();
        } catch (Exception e) {
            log.error("[小工] 大模型调用失败", e);
            aiResponse = generateFallbackResponse(request.getMessage());
        }

        int responseTime = (int) (System.currentTimeMillis() - startTime);

        // 保存AI回复
        saveMessage(user.getId(), sessionId, 2, aiResponse, null);
        updateSessionMeta(user.getId(), sessionId, request.getMessage());

        log.info("[小工] user={}, session={}, responseTime={}ms, len={}",
                username, sessionId, responseTime, aiResponse.length());

        return AiChatResponse.builder()
                .sessionId(sessionId)
                .message(aiResponse)
                .responseTime(responseTime)
                .messageType("AI")
                .build();
    }

    // ==================== 流式聊天 (SSE) ====================

    @Override
    public Flux<String> chatStream(String username, AiChatRequest request) {
        User user = getUser(username);
        String sessionId = ensureSession(user, request.getSessionId());

        long startTime = System.currentTimeMillis();

        // 保存用户消息
        saveMessage(user.getId(), sessionId, 1, request.getMessage(), null);

        // 构建上下文
        String context = buildContext(user.getId(), sessionId);

        // 用于收集完整回复
        StringBuilder fullResponse = new StringBuilder();

        String userMessage = context + "\n\n用户最新问题：" + request.getMessage() +
                "\n\n请以" + XIAOGONG_NAME + "的身份回答问题：";

        // ===== 优先尝试真实大模型流式 =====
        try {
            Prompt prompt = new Prompt(List.of(
                    new SystemMessage(SYSTEM_PROMPT),
                    new UserMessage(userMessage)
            ));
            return streamingChatModel.stream(prompt)
                    .map(response -> {
                        String content = response.getResult().getOutput().getContent();
                        if (content != null) {
                            fullResponse.append(content);
                        }
                        return content != null ? content : "";
                    })
                    .doOnComplete(() -> {
                        String fullText = fullResponse.toString();
                        int responseTime = (int) (System.currentTimeMillis() - startTime);
                        saveMessage(user.getId(), sessionId, 2, fullText, null);
                        updateSessionMeta(user.getId(), sessionId, request.getMessage());
                        log.info("[小工流式] user={}, session={}, responseTime={}ms, len={}",
                                username, sessionId, responseTime, fullText.length());
                    })
                    .doOnError(e -> {
                        log.error("[小工流式] 大模型调用失败", e);
                        if (fullResponse.length() > 0) {
                            saveMessage(user.getId(), sessionId, 2,
                                    fullResponse.toString() + "\n\n[回复中断]", null);
                        } else {
                            String fallback = generateFallbackResponse(request.getMessage());
                            saveMessage(user.getId(), sessionId, 2, fallback, null);
                        }
                        updateSessionMeta(user.getId(), sessionId, request.getMessage());
                    });
        } catch (Exception e) {
            log.warn("[小工流式] 真实大模型不可用，使用本地模拟流式输出");
        }

        // ===== 兜底：本地模拟流式输出（打字机效果） =====
        String fallback = generateFallbackResponse(request.getMessage());
        log.info("[小工流式-本地] user={}, session={}, 模拟流式输出", username, sessionId);

        // 将回复按字符拆分为流，模拟打字机效果
        String[] chars = fallback.split("");
        return Flux.fromArray(chars)
                .delayElements(java.time.Duration.ofMillis(40))
                .doOnNext(ch -> fullResponse.append(ch))
                .doOnComplete(() -> {
                    String fullText = fullResponse.toString();
                    int responseTime = (int) (System.currentTimeMillis() - startTime);
                    saveMessage(user.getId(), sessionId, 2, fullText, null);
                    updateSessionMeta(user.getId(), sessionId, request.getMessage());
                    log.info("[小工流式-本地] 完成: responseTime={}ms, len={}", responseTime, fullText.length());
                })
                .doOnError(e -> {
                    log.error("[小工流式-本地] 异常", e);
                });
    }

    // ==================== 图片生成 ====================

    @Override
    @Transactional
    public AiChatResponse generateImage(String username, AiImageRequest request) {
        User user = getUser(username);
        String sessionId = ensureSession(user, request.getSessionId());

        long startTime = System.currentTimeMillis();

        // 保存用户生图请求
        String userPrompt = "[生成图片] " + request.getPrompt();
        saveMessage(user.getId(), sessionId, 1, userPrompt, null);

        // 让大模型优化图片描述
        String optimizedPrompt;
        try {
            Prompt prompt = new Prompt(List.of(
                    new SystemMessage("你是一个图片描述专家。将用户描述转换为适合AI生图的英文prompt。只输出prompt本身，不要解释。"),
                    new UserMessage("请为以下描述生成详细的AI生图prompt（英文，带画质和风格关键词）：" + request.getPrompt())
            ));
            ChatResponse response = chatModel.call(prompt);
            optimizedPrompt = response.getResult().getOutput().getContent().trim();
        } catch (Exception e) {
            optimizedPrompt = request.getPrompt() + ", professional photography, high quality, 4K";
        }

        // 调用文生图API
        String imageUrl = null;
        try {
            imageUrl = callImageGenerationApi(optimizedPrompt, request.getStyle());
        } catch (Exception e) {
            log.error("[小工生图] API调用失败", e);
            String placeholderText = "图片生成中，请稍候...";
            try {
                imageUrl = "https://placehold.co/1024x1024/C8102E/ffffff?text=" +
                        java.net.URLEncoder.encode(placeholderText, java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception ex) {
                imageUrl = null;
            }
        }

        int responseTime = (int) (System.currentTimeMillis() - startTime);

        // 保存图片消息
        AiConversation imageMsg = AiConversation.builder()
                .userId(user.getId())
                .sessionId(sessionId)
                .messageType(3)
                .content(request.getPrompt())
                .imageUrl(imageUrl)
                .responseTime(responseTime)
                .createdAt(LocalDateTime.now())
                .build();
        conversationMapper.insert(imageMsg);

        updateSessionMeta(user.getId(), sessionId, "[生图] " + request.getPrompt());

        log.info("[小工生图] user={}, session={}, responseTime={}ms, style={}",
                username, sessionId, responseTime, request.getStyle());

        return AiChatResponse.builder()
                .sessionId(sessionId)
                .message(request.getPrompt())
                .responseTime(responseTime)
                .messageType("IMAGE")
                .imageUrl(imageUrl)
                .build();
    }

    // ==================== 私有辅助方法 ====================

    private User getUser(String username) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    private String ensureSession(User user, String sessionId) {
        if (sessionId == null || sessionId.isEmpty() || sessionId.length() < 8) {
            return createSessionInternal(user.getId());
        }
        AiSession existing = sessionMapper.selectOne(
                new LambdaQueryWrapper<AiSession>().eq(AiSession::getSessionId, sessionId));
        if (existing == null) {
            return createSessionInternal(user.getId());
        }
        return sessionId;
    }

    private String createSessionInternal(Long userId) {
        String sessionId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        AiSession session = AiSession.builder()
                .userId(userId)
                .sessionId(sessionId)
                .title("新会话")
                .messageCount(0)
                .status(1)
                .build();
        sessionMapper.insert(session);
        return sessionId;
    }

    private void saveMessage(Long userId, String sessionId, int messageType, String content, String imageUrl) {
        AiConversation msg = AiConversation.builder()
                .userId(userId)
                .sessionId(sessionId)
                .messageType(messageType)
                .content(content)
                .imageUrl(imageUrl)
                .createdAt(LocalDateTime.now())
                .build();
        conversationMapper.insert(msg);
    }

    private String buildContext(Long userId, String sessionId) {
        List<AiConversation> history = conversationMapper.selectList(
                new LambdaQueryWrapper<AiConversation>()
                        .eq(AiConversation::getUserId, userId)
                        .eq(AiConversation::getSessionId, sessionId)
                        .orderByAsc(AiConversation::getCreatedAt));
        if (history.size() > maxContextLength * 2) {
            history = history.subList(history.size() - maxContextLength * 2, history.size());
        }
        StringBuilder sb = new StringBuilder();
        for (AiConversation conv : history) {
            if (conv.getMessageType() == 1) {
                sb.append("用户：").append(conv.getContent()).append("\n");
            } else if (conv.getMessageType() == 2) {
                sb.append(XIAOGONG_NAME).append("：").append(conv.getContent()).append("\n");
            } else if (conv.getMessageType() == 3) {
                sb.append("用户：[请求生成图片:").append(conv.getContent()).append("]\n");
            }
        }
        return sb.toString();
    }

    private void updateSessionMeta(Long userId, String sessionId, String latestMessage) {
        AiSession session = sessionMapper.selectOne(
                new LambdaQueryWrapper<AiSession>().eq(AiSession::getSessionId, sessionId));
        if (session == null) return;
        if (session.getTitle() == null || "新会话".equals(session.getTitle())) {
            String title = latestMessage.length() > 30
                    ? latestMessage.substring(0, 30) + "..." : latestMessage;
            session.setTitle(title);
        }
        session.setMessageCount(session.getMessageCount() + 2);
        session.setUpdatedAt(LocalDateTime.now());
        sessionMapper.updateById(session);
    }

    /**
     * 调用火山引擎文生图API (Doubao Image Generation)
     */
    private String callImageGenerationApi(String prompt, String style) throws Exception {
        String styledPrompt = prompt;
        if ("cartoon".equals(style)) {
            styledPrompt += ", cartoon style, illustration, vibrant colors";
        } else if ("logo".equals(style)) {
            styledPrompt += ", logo design, minimalist, professional, vector art";
        } else {
            styledPrompt += ", photorealistic, 4K, high quality, professional lighting";
        }

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        JSONObject body = new JSONObject();
        body.set("prompt", styledPrompt);
        body.set("n", 1);
        body.set("size", "1024x1024");
        body.set("response_format", "url");

        // 火山引擎 OPENAI 兼容的图片端点
        String imageApiUrl = baseUrl;
        if (imageApiUrl.endsWith("/api/v3")) {
            imageApiUrl = imageApiUrl.substring(0, imageApiUrl.length() - 7) + "/v1/images/generations";
        } else if (!imageApiUrl.endsWith("/v1/images/generations")) {
            imageApiUrl = imageApiUrl + "/images/generations";
        }

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(imageApiUrl))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONObject respBody = new JSONObject(response.body());
            var dataArray = respBody.getJSONArray("data");
            if (dataArray != null && !dataArray.isEmpty()) {
                JSONObject data = (JSONObject) dataArray.get(0);
                String url = data.getStr("url");
                if (url != null && !url.isEmpty()) return url;
            }
        }

        log.warn("[小工生图] API返回非200: status={}, body={}", response.statusCode(), response.body());

        return "https://placehold.co/1024x1024/fafbfc/C8102E?text=" +
                java.net.URLEncoder.encode("图片生成失败·请重试", java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * 兜底回复 - 当大模型不可用时
     */
    private String generateFallbackResponse(String message) {
        String msg = message.toLowerCase().trim();
        if (msg.contains("转账") || msg.contains("transfer")) {
            return "您好！我是小工 😊\n\n要进行转账操作，请登录后在\"转账\"菜单选择转账类型，输入收款账户和金额即可。\n\n温馨提示：\n• 单笔限额：50,000元\n• 单日限额：200,000元\n• 核实收款人信息后再确认";
        } else if (msg.contains("余额") || msg.contains("查询") || msg.contains("balance")) {
            return "您好！我是小工 😊\n\n查询余额：登录后在首页概览查看，或进入\"我的账户\"查看详情。\n\n需要帮您查看具体账户吗？";
        } else if (msg.contains("账单") || msg.contains("bill")) {
            return "您好！我是小工 😊\n\n账单每月1号自动生成，在\"我的账单\"页面查看。包含月度收支汇总和明细。";
        } else if (msg.contains("你好") || msg.contains("hello") || msg.contains("您好") || msg.contains("hi")) {
            return "您好！我是ICBC智能客服小工，很高兴为您服务！🎉\n\n我可以帮您：\n• 解答转账/账户/账单等业务问题\n• 提供银行安全建议\n• 24小时在线\n\n请问有什么可以帮您？";
        } else if (msg.contains("密码") || msg.contains("安全")) {
            return "您好！我是小工 🔐\n\n安全建议：\n• 定期更换密码\n• 设置独立支付密码\n• 开启登录保护\n• 绝不向任何人透露密码和验证码";
        } else {
            return "您好！我是小工，感谢您的咨询 😊\n\n建议：\n• 换个方式重新描述\n• 咨询转账/余额/账单/安全问题\n• 拨打95588转人工客服";
        }
    }
}
