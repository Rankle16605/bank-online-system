package com.icbc.online.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 业务工具类 - 生成各种编号
 */
public class BizNoGenerator {

    private static final AtomicInteger SEQUENCE = new AtomicInteger(0);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Random RANDOM = new Random();

    /**
     * 生成账户号 (19位)
     */
    public static String generateAccountNo() {
        return "6222" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
                + String.format("%04d", RANDOM.nextInt(10000));
    }

    /**
     * 生成交易流水号
     */
    public static String generateTransactionNo() {
        int seq = SEQUENCE.incrementAndGet() % 10000;
        return "TXN" + DATE_FORMAT.format(LocalDateTime.now())
                + String.format("%04d", seq)
                + String.format("%02d", RANDOM.nextInt(100));
    }

    /**
     * 生成账单编号
     */
    public static String generateBillNo(Long userId, String billMonth, Long accountId) {
        return "BILL-" + userId + "-" + billMonth.replace("-", "") + "-" + accountId;
    }

    /**
     * 生成会话ID
     */
    public static String generateSessionId(Long userId) {
        return "SESS-" + userId + "-" + System.currentTimeMillis()
                + "-" + RANDOM.nextInt(10000);
    }

    /**
     * 生成借款编号
     */
    public static String generateLoanNo() {
        int seq = SEQUENCE.incrementAndGet() % 10000;
        return "LOAN" + DATE_FORMAT.format(LocalDateTime.now())
                + String.format("%04d", seq);
    }

    /**
     * 生成还款编号
     */
    public static String generateRepaymentNo() {
        int seq = SEQUENCE.incrementAndGet() % 10000;
        return "REP" + DATE_FORMAT.format(LocalDateTime.now())
                + String.format("%04d", seq);
    }
}
