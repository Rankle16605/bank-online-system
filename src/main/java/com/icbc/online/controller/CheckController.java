package com.icbc.online.controller;

import com.icbc.online.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 支票管理控制器 - 模拟数据
 * 管理银行支票的开具、兑付、作废
 */
@Slf4j
@Tag(name = "支票管理", description = "管理员开具和管理银行支票")
@RestController
@RequestMapping("/api/v1/admin/checks")
@PreAuthorize("hasRole('ADMIN')")
public class CheckController {

    private static final Map<String, Map<String, Object>> checkStore = new ConcurrentHashMap<>();
    private static final AtomicInteger seq = new AtomicInteger(8);

    static {
        // 初始化模拟支票数据
        addMockCheck("CHK20260601001", "2026-06-01", "华为技术有限公司", 1580000.00, "设备采购款", "已兑付", "合同HT-2026-0588");
        addMockCheck("CHK20260603002", "2026-06-03", "中兴通讯股份有限公司", 820000.00, "通信设备款", "已兑付", "采购订单PO-2026-1123");
        addMockCheck("CHK20260608003", "2026-06-08", "北京建工集团", 3560000.00, "工程款", "待兑付", "基建项目二期");
        addMockCheck("CHK20260610004", "2026-06-10", "腾讯科技有限公司", 285000.00, "云服务费", "待兑付", "2026年度云服务");
        addMockCheck("CHK20260612005", "2026-06-12", "阿里巴巴集团", 520000.00, "技术服务费", "待兑付", "年度技术服务合同");
        addMockCheck("CHK20260615006", "2026-06-15", "中国石油天然气集团", 4200000.00, "货款", "已打印", "原油采购2026-Q2");
        addMockCheck("CHK20260615007", "2026-06-15", "京东集团", 168000.00, "办公设备采购", "待兑付", "");
        addMockCheck("CHK20260616008", "2026-06-16", "上海浦东发展银行", 9800000.00, "同业拆借", "已作废", "合同终止");
    }

    private static void addMockCheck(String checkNo, String issueDate, String payee,
                                      double amount, String purpose, String statusText, String remark) {
        Map<String, Object> c = new LinkedHashMap<>();
        c.put("checkNo", checkNo);
        c.put("issueDate", issueDate);
        c.put("payee", payee);
        c.put("amount", BigDecimal.valueOf(amount));
        c.put("amountCn", numberToChinese(amount));
        c.put("purpose", purpose);
        c.put("status", statusText.equals("已兑付") ? 2 : statusText.equals("已作废") ? 3 : statusText.equals("已打印") ? 1 : 0);
        c.put("statusText", statusText);
        c.put("remark", remark);
        checkStore.put(checkNo, c);
    }

    @Operation(summary = "获取所有支票")
    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getChecks() {
        List<Map<String, Object>> list = new ArrayList<>(checkStore.values());
        list.sort((a, b) -> ((String) b.get("checkNo")).compareTo((String) a.get("checkNo")));
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @Operation(summary = "开具新支票")
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createCheck(@RequestBody Map<String, Object> body) {
        String checkNo = "CHK" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + String.format("%03d", seq.incrementAndGet());
        String payee = (String) body.get("payee");
        Number amountNum = (Number) body.get("amount");
        double amount = amountNum != null ? amountNum.doubleValue() : 0;
        String purpose = (String) body.getOrDefault("purpose", "");
        String remark = (String) body.getOrDefault("remark", "");
        String issueDate = Optional.ofNullable(body.get("issueDate"))
                .map(Object::toString).orElse(LocalDate.now().toString());

        Map<String, Object> c = new LinkedHashMap<>();
        c.put("checkNo", checkNo);
        c.put("issueDate", issueDate);
        c.put("payee", payee);
        c.put("amount", BigDecimal.valueOf(amount));
        c.put("amountCn", numberToChinese(amount));
        c.put("purpose", purpose);
        c.put("status", 0);
        c.put("statusText", "待兑付");
        c.put("remark", remark);
        checkStore.put(checkNo, c);

        log.info("[支票管理] 新开支票: checkNo={}, payee={}, amount={}", checkNo, payee, amount);
        return ResponseEntity.ok(ApiResponse.success("支票开具成功", c));
    }

    @Operation(summary = "兑付支票")
    @PostMapping("/{checkNo}/cash")
    public ResponseEntity<ApiResponse<Map<String, Object>>> cashCheck(@PathVariable String checkNo) {
        Map<String, Object> c = checkStore.get(checkNo);
        if (c == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("支票不存在"));
        }
        c.put("status", 2);
        c.put("statusText", "已兑付");
        c.put("cashedDate", LocalDate.now().toString());
        log.info("[支票管理] 支票兑付: checkNo={}", checkNo);
        return ResponseEntity.ok(ApiResponse.success("兑付成功", c));
    }

    @Operation(summary = "作废支票")
    @PostMapping("/{checkNo}/void")
    public ResponseEntity<ApiResponse<Map<String, Object>>> voidCheck(@PathVariable String checkNo) {
        Map<String, Object> c = checkStore.get(checkNo);
        if (c == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("支票不存在"));
        }
        c.put("status", 3);
        c.put("statusText", "已作废");
        log.info("[支票管理] 支票作废: checkNo={}", checkNo);
        return ResponseEntity.ok(ApiResponse.success("已作废", c));
    }

    /**
     * 数字转中文大写金额
     */
    private static String numberToChinese(double num) {
        if (num <= 0) return "";
        String[] digits = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
        String[] radices = {"", "拾", "佰", "仟"};
        String[] bigRadices = {"", "万", "亿", "万亿"};

        long n = (long) num;
        StringBuilder result = new StringBuilder();

        if (n == 0) {
            result.append("零");
        } else {
            int zeroCount = 0;
            StringBuilder str = new StringBuilder();
            int unitIndex = 0;
            while (n > 0) {
                int section = (int) (n % 10000);
                StringBuilder sectionStr = new StringBuilder();
                boolean sectionZero = false;
                int temp = section;
                for (int i = 0; i < 4; i++) {
                    int digit = temp % 10;
                    if (digit == 0) {
                        sectionZero = true;
                    } else {
                        if (sectionZero && sectionStr.length() > 0)
                            sectionStr.insert(0, "零");
                        sectionStr.insert(0, digits[digit] + radices[i]);
                        sectionZero = false;
                    }
                    temp /= 10;
                    if (temp == 0) break;
                }
                if (sectionStr.length() > 0) {
                    str.insert(0, sectionStr.toString() + bigRadices[unitIndex]
                            + (zeroCount > 0 && str.length() > 0 ? "零" : ""));
                    zeroCount = 0;
                } else {
                    zeroCount = 1;
                }
                n /= 10000;
                unitIndex++;
            }
            result.append(str);
        }
        result.append("元");

        int decPart = (int) Math.round((num - Math.floor(num)) * 100);
        if (decPart == 0) {
            result.append("整");
        } else {
            int jiao = decPart / 10;
            int fen = decPart % 10;
            if (jiao > 0) result.append(digits[jiao]).append("角");
            if (fen > 0) result.append(digits[fen]).append("分");
        }
        return result.toString();
    }
}
