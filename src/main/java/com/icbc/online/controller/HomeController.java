package com.icbc.online.controller;

import com.icbc.online.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 首页/系统信息控制器
 */
@Tag(name = "系统信息", description = "系统状态和首页")
@RestController
@RequestMapping("/api/v1")
public class HomeController {

    @Operation(summary = "系统信息")
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> systemInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "ICBC智能在线银行系统");
        info.put("version", "V1.0.0");
        info.put("team", "ICBC Online System");
        info.put("description", "基于Spring Boot + Doubao大模型的在线银行系统");
        info.put("apiBasePath", "/api/v1");
        info.put("documentation", "/swagger-ui.html");
        return ResponseEntity.ok(ApiResponse.success(info));
    }
}
