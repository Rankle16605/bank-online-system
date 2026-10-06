package com.icbc.online.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.icbc.online.dto.ApiResponse;
import com.icbc.online.dto.request.AiChatRequest;
import com.icbc.online.dto.request.AiImageRequest;
import com.icbc.online.dto.response.AiChatResponse;
import com.icbc.online.dto.response.AiSessionDTO;
import com.icbc.online.mapper.UserMapper;
import com.icbc.online.model.User;
import com.icbc.online.service.AiChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * AI智能客服控制器 - 小工
 * 对接Doubao大模型，支持流式输出与图片生成
 */
@Slf4j
@Tag(name = "AI智能客服", description = "小工智能客服，支持流式对话、图片生成、会话管理")
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;
    private final UserMapper userMapper;

    /** 获取当前用户名 */
    private String getUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    /** 获取当前用户ID */
    private Long getUserId() {
        String username = getUsername();
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user.getId();
    }

    // ==================== 会话管理 ====================

    @Operation(summary = "获取会话列表")
    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<AiSessionDTO>>> getSessions() {
        List<AiSessionDTO> sessions = aiChatService.getSessions(getUserId());
        return ResponseEntity.ok(ApiResponse.success(sessions));
    }

    @Operation(summary = "删除会话")
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<ApiResponse<Void>> deleteSession(
            @Parameter(description = "会话ID") @PathVariable String sessionId) {
        aiChatService.deleteSession(getUserId(), sessionId);
        return ResponseEntity.ok(ApiResponse.success("会话已删除", null));
    }

    @Operation(summary = "获取会话历史消息")
    @GetMapping("/sessions/{sessionId}/history")
    public ResponseEntity<ApiResponse<List<AiChatResponse>>> getSessionHistory(
            @Parameter(description = "会话ID") @PathVariable String sessionId) {
        List<AiChatResponse> history = aiChatService.getSessionHistory(getUserId(), sessionId);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @Operation(summary = "创建新会话")
    @PostMapping("/session")
    public ResponseEntity<ApiResponse<String>> createSession() {
        String sessionId = aiChatService.createSession(getUserId());
        return ResponseEntity.ok(ApiResponse.success("会话创建成功", sessionId));
    }

    // ==================== 文本对话 ====================

    @Operation(summary = "发送消息(非流式)")
    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<AiChatResponse>> chat(
            @RequestBody @Valid AiChatRequest request) {
        String username = getUsername();
        AiChatResponse response = aiChatService.chat(username, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "流式对话(SSE)")
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody @Valid AiChatRequest request) {
        String username = getUsername();
        return aiChatService.chatStream(username, request);
    }

    // ==================== 图片生成 ====================

    @Operation(summary = "生成图片")
    @PostMapping("/image/generate")
    public ResponseEntity<ApiResponse<AiChatResponse>> generateImage(
            @RequestBody @Valid AiImageRequest request) {
        String username = getUsername();
        AiChatResponse response = aiChatService.generateImage(username, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
